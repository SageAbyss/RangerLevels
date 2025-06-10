package rl.sage.rangerlevels.items.flag;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.config.ItemsConfig;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.items.Tier;
import rl.sage.rangerlevels.items.flag.BattleBannerItem;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maneja la colocación, ruptura y expiración de Bandera de Batalla.
 */
@Mod.EventBusSubscriber(modid = RangerLevels.MODID)
public class BattleBannerHandler {

    private static class BannerState {
        final UUID owner;
        final Tier tier;
        final long placedAt;
        final BlockPos pos;
        final double radius;
        final long durationMs;
        final int usesLeft;

        BannerState(UUID owner, Tier tier, BlockPos pos, double radius, long durationMs, int usesLeft) {
            this.owner = owner;
            this.tier = tier;
            this.placedAt = System.currentTimeMillis();
            this.pos = pos;
            this.radius = radius;
            this.durationMs = durationMs;
            this.usesLeft = usesLeft;
        }
    }

    /** Mapa de estados activos por jugador (dueño). */
    private static final Map<UUID, List<BannerState>> ACTIVE = new ConcurrentHashMap<>();
    /** Para no spamear action bar: estado previo de dentro/fuera. */
    private static final Map<UUID, Boolean> PREV_INSIDE = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onPlaceBanner(PlayerInteractEvent.RightClickBlock evt) {
        if (evt.getWorld().isClientSide() || !(evt.getPlayer() instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) evt.getPlayer();
        ItemStack held = evt.getItemStack();
        String id = RangerItemDefinition.getIdFromStack(held);
        if (id == null || !id.startsWith(BattleBannerItem.ID + "_")) return;

        // Determinar tier
        Tier tier;
        try {
            tier = Tier.valueOf(id.substring(id.lastIndexOf('_') + 1).toUpperCase());
        } catch (IllegalArgumentException e) {
            return;
        }

        int before = BattleBannerItem.getUsosRestantes(held);
        if (before <= 0) {
            player.sendMessage(new StringTextComponent("§cNo te quedan usos de la Bandera."), player.getUUID());
            return;
        }

        // Decrementar uso en el mismo ItemStack
        BattleBannerItem.decrementarUso(held);
        int after = BattleBannerItem.getUsosRestantes(held);
        // Actualizar lore en mano
        RangerItemDefinition def = CustomItemRegistry.getDefinition(id);
        if (def instanceof BattleBannerItem) {
            ((BattleBannerItem) def).updateLore(held);
        }

        // Leer config de radio y duración
        ItemsConfig.BannerConfig cfg = ItemsConfig.get().battleBanner;
        double radius;
        int durationMin;
        switch (tier) {
            case EPICO:
                radius = cfg.radiusEpic;
                durationMin = cfg.durationEpic;
                break;
            case ESTELAR:
                radius = cfg.radiusStellar;
                durationMin = cfg.durationStellar;
                break;
            case MITICO:
                radius = cfg.radiusMythic;
                durationMin = cfg.durationMythic;
                break;
            default:
                return;
        }

        // Registrar estado con usesLeft = after
        BlockPos placePos = evt.getPos().relative(evt.getFace());
        BannerState bs = new BannerState(
                player.getUUID(), tier, placePos,
                radius, durationMin * 60_000L, after
        );
        ACTIVE.computeIfAbsent(player.getUUID(), k -> new ArrayList<>()).add(bs);

        player.sendMessage(new StringTextComponent(
                "§aHas colocado Bandera (" + tier.getDisplayName() + "). Usos restantes tras esta: §e" + after
        ), player.getUUID());

        // Reset previo para que en el siguiente tick se envíe “Entraste” si corresponde
        PREV_INSIDE.remove(player.getUUID());
    }

    @SubscribeEvent
    public static void onBreakBanner(BlockEvent.BreakEvent evt) {
        if (evt.getWorld().isClientSide() || !(evt.getPlayer() instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity breaker = (ServerPlayerEntity) evt.getPlayer();
        BlockPos brokenPos = evt.getPos();
        World world = (World) evt.getWorld();

        // Buscamos en todos los dueños si tienen una bandera en esa posición
        Iterator<Map.Entry<UUID, List<BannerState>>> mapIt = ACTIVE.entrySet().iterator();
        while (mapIt.hasNext()) {
            Map.Entry<UUID, List<BannerState>> entry = mapIt.next();
            UUID ownerUUID = entry.getKey();
            List<BannerState> list = entry.getValue();
            Iterator<BannerState> it = list.iterator();
            while (it.hasNext()) {
                BannerState bs = it.next();
                if (bs.pos.equals(brokenPos)) {
                    // Esta bandera se rompe
                    it.remove();
                    if (list.isEmpty()) {
                        mapIt.remove();
                    }
                    // Cancelar drop vanilla y quitar bloque
                    evt.setCanceled(true);
                    world.removeBlock(brokenPos, false);

                    // Devolver o eliminar según usesLeft
                    ServerPlayerEntity ownerPlayer = breaker.getServer()
                            .getPlayerList().getPlayer(ownerUUID);
                    if (bs.usesLeft > 0) {
                        // Crear bandera con usosLeft y dar al dueño
                        ItemStack toGive = BattleBannerItem.createWithUses(bs.tier, bs.usesLeft, 1);
                        if (ownerPlayer != null) {
                            // dueño en línea: intentar añadir a inventario
                            if (!ownerPlayer.inventory.add(toGive)) {
                                // inventario lleno: dropear en mundo
                                ItemEntity drop = new ItemEntity(
                                        world,
                                        brokenPos.getX() + 0.5,
                                        brokenPos.getY() + 0.5,
                                        brokenPos.getZ() + 0.5,
                                        toGive
                                );
                                world.addFreshEntity(drop);
                            }
                            ownerPlayer.sendMessage(new StringTextComponent(
                                    "§eTu Bandera (" + bs.tier.getDisplayName() + ") fue recogida. Usos restantes: §e" + bs.usesLeft
                            ), ownerPlayer.getUUID());
                        } else {
                            // dueño offline: dropear en mundo para que recupere luego
                            ItemEntity drop = new ItemEntity(
                                    world,
                                    brokenPos.getX() + 0.5,
                                    brokenPos.getY() + 0.5,
                                    brokenPos.getZ() + 0.5,
                                    toGive
                            );
                            world.addFreshEntity(drop);
                        }
                    } else {
                        // usesLeft == 0: eliminar definitivamente.
                        if (ownerPlayer != null) {
                            PlayerSoundUtils.playSoundToPlayer(
                                    ownerPlayer, SoundEvents.ITEM_BREAK, SoundCategory.PLAYERS, 1.0f, 1.0f
                            );
                            ownerPlayer.sendMessage(new StringTextComponent(
                                    "§cTu Bandera (" + bs.tier.getDisplayName() + ") se eliminó (último uso)."
                            ), ownerPlayer.getUUID());
                        }
                        // Si dueño offline: nada que devolver
                    }

                    // Si quien rompe no es dueño, opcional mensaje al breaker
                    if (!breaker.getUUID().equals(ownerUUID)) {
                        breaker.sendMessage(new StringTextComponent(
                                "§eHas roto la Bandera de otro jugador."
                        ), breaker.getUUID());
                    }
                    // Limpiar action bar tanto de dueño como de breaker
                    PREV_INSIDE.remove(ownerUUID);
                    PREV_INSIDE.remove(breaker.getUUID());
                    return;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent evt) {
        if (evt.phase != TickEvent.Phase.END || evt.player.level.isClientSide()) return;
        ServerPlayerEntity player = (ServerPlayerEntity) evt.player;
        UUID uuid = player.getUUID();

        List<BannerState> list = ACTIVE.getOrDefault(uuid, Collections.emptyList());
        // Si no hay banderas activas para este jugador, limpiamos prev y salimos
        if (list.isEmpty()) {
            PREV_INSIDE.remove(uuid);
            return;
        }

        // Expiración de cada bandera
        Iterator<BannerState> it = list.iterator();
        while (it.hasNext()) {
            BannerState bs = it.next();
            if (System.currentTimeMillis() - bs.placedAt > bs.durationMs) {
                it.remove();
                // Si la lista queda vacía, quitar la entrada
                if (list.isEmpty()) {
                    ACTIVE.remove(uuid);
                }
                // Procesar devolución o eliminación
                if (bs.usesLeft > 0) {
                    ItemStack toGive = BattleBannerItem.createWithUses(bs.tier, bs.usesLeft, 1);
                    if (!player.inventory.add(toGive)) {
                        // inventario lleno: dropear junto a jugador
                        ItemEntity drop = new ItemEntity(
                                (World) player.level,
                                player.getX(), player.getY(), player.getZ(),
                                toGive
                        );
                        player.level.addFreshEntity(drop);
                    }
                    player.sendMessage(new StringTextComponent(
                            "§cBandera (" + bs.tier.getDisplayName() + ") expiró. Usos restantes devueltos: §e" + bs.usesLeft
                    ), player.getUUID());
                } else {
                    // último uso expirado: eliminar
                    PlayerSoundUtils.playSoundToPlayer(
                            player, SoundEvents.ITEM_BREAK, SoundCategory.PLAYERS, 1.0f, 1.0f
                    );
                    player.sendMessage(new StringTextComponent(
                            "§cBandera (" + bs.tier.getDisplayName() + ") expiró y se eliminó (último uso)."
                    ), player.getUUID());
                }
                PREV_INSIDE.remove(uuid);
                player.sendMessage(new StringTextComponent(""), ChatType.GAME_INFO, player.getUUID());
            }
        }

        // Detectar entrar/salir del área de alguna bandera activa
        boolean inside = false;
        for (BannerState bs : list) {
            double dx = player.getX() - (bs.pos.getX() + 0.5);
            double dz = player.getZ() - (bs.pos.getZ() + 0.5);
            if (dx * dx + dz * dz <= bs.radius * bs.radius) {
                inside = true;
                break;
            }
        }
        Boolean prev = PREV_INSIDE.get(uuid);
        if (prev == null || prev != inside) {
            player.sendMessage(
                    new StringTextComponent(
                            inside
                                    ? "§eEntraste al Modo de Batalla Controlada"
                                    : "§cSaliste del Modo de Batalla Controlada"
                    ),
                    ChatType.GAME_INFO, player.getUUID()
            );
            PlayerSoundUtils.playSoundToPlayer(
                    player, SoundEvents.ITEM_PICKUP, SoundCategory.PLAYERS, 1.5f, 0.8f
            );
            PREV_INSIDE.put(uuid, inside);
        }
    }

    /**
     * Comprueba si el jugador está en área activa de alguna de sus banderas vigentes.
     */
    public static boolean isInControlledArea(ServerPlayerEntity player) {
        List<BannerState> list = ACTIVE.getOrDefault(player.getUUID(), Collections.emptyList());
        long now = System.currentTimeMillis();
        for (BannerState bs : list) {
            if (now - bs.placedAt > bs.durationMs) continue;
            double dx = player.getX() - (bs.pos.getX() + 0.5);
            double dz = player.getZ() - (bs.pos.getZ() + 0.5);
            if (dx * dx + dz * dz <= bs.radius * bs.radius) return true;
        }
        return false;
    }
}
