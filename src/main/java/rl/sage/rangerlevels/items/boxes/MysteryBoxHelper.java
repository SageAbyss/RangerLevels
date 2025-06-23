package rl.sage.rangerlevels.items.boxes;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.EnderChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.minecraftforge.registries.ForgeRegistries;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.capability.LevelProvider;
import rl.sage.rangerlevels.config.MysteryBoxesConfig;
import rl.sage.rangerlevels.config.MysteryBoxesConfig.MysteryBoxConfig.CommandEntry;
import rl.sage.rangerlevels.config.MysteryBoxesConfig.MysteryBoxConfig.TierBoxConfig;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.items.ItemsHelper;
import rl.sage.rangerlevels.items.Tier;
import rl.sage.rangerlevels.items.modificadores.ModificadorIVs;
import rl.sage.rangerlevels.items.modificadores.ModificadorNaturaleza;
import rl.sage.rangerlevels.items.modificadores.ModificadorShiny;
import rl.sage.rangerlevels.items.modificadores.ModificadorTamano;
import rl.sage.rangerlevels.items.randoms.LagrimaDiosaTiempo;
import rl.sage.rangerlevels.items.reliquias.SangreQuetzalMitico;
import rl.sage.rangerlevels.items.sacrificios.*;
import rl.sage.rangerlevels.items.tickets.TicketMaster;
import rl.sage.rangerlevels.items.totems.fragmentos.TotemAbismoGlacial;
import rl.sage.rangerlevels.items.totems.fragmentos.TotemLamentoDioses;
import rl.sage.rangerlevels.items.totems.fragmentos.TotemRaizPrimordial;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

import java.util.*;

@Mod.EventBusSubscriber(modid = RangerLevels.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MysteryBoxHelper {
    private static final Random RNG = new Random();
    private static void giveOrDrop(ServerPlayerEntity player, ServerWorld world, BlockPos pos, ItemStack stack) {
        // usamos copy() para no mutar el original por si acaso
        ItemStack toGive = stack.copy();
        boolean added = player.inventory.add(toGive);
        if (!added) {
            ItemEntity dropped = new ItemEntity(
                    world,
                    pos.getX() + 0.5,
                    pos.getY() + 1.0,
                    pos.getZ() + 0.5,
                    toGive
            );
            // dropped.setDefaultPickupDelay(); // si quieres retrasar la recolección
            world.addFreshEntity(dropped);
        }
    }

    /**
     * Abre la caja y ejecuta TODO lo configurado en MysteryBoxesConfig.yml para ese tier.
     */
    public static void open(ServerPlayerEntity player, String boxId, EventType event, ServerWorld world, BlockPos pos) {
        MysteryBoxesConfig.MysteryBoxConfig cfg = MysteryBoxesConfig.get().mysteryBox;
        TierBoxConfig c;
        switch(boxId) {
            case MysteryBoxComun.ID:      c = cfg.comun;      break;
            case MysteryBoxRaro.ID:       c = cfg.raro;       break;
            case MysteryBoxEpico.ID:      c = cfg.epico;      break;
            case MysteryBoxLegendario.ID: c = cfg.legendario; break;
            case MysteryBoxEstelar.ID:    c = cfg.estelar;    break;
            case MysteryBoxMitico.ID:     c = cfg.mitico;     break;
            default: return;
        }
        if (!c.enable) return;

        // 1) Dar ítems aleatorios ponderados + no-drop + peso por tier de ítem
        int count = c.randomItemMin + RNG.nextInt(c.randomItemMax - c.randomItemMin + 1);
        List<String> rewards = ItemsHelper.getRewardItemIds();
        //EXCEPTO ESTOS ITEMS
        rewards.remove(TotemRaizPrimordial.ID);
        rewards.remove(TotemLamentoDioses.ID);
        rewards.remove(TotemAbismoGlacial.ID);
        rewards.remove(LagrimaDiosaTiempo.ID);
        rewards.remove(SangreQuetzalMitico.ID);
        rewards.remove(ModificadorShiny.ID);
        rewards.remove(ModificadorIVs.ID);
        rewards.remove(ModificadorNaturaleza.ID);
        rewards.remove(ModificadorTamano.ID);
        rewards.remove(ConcentradoDeAlmas.ID);
        rewards.remove(EsenciaUltraente.ID);
        rewards.remove(EsenciaLegendaria.ID);
        rewards.remove(EsenciaBoss.ID);
        rewards.remove(CatalizadorAlmas.ID);
        rewards.remove(TicketMaster.ID);

        for (int i = 0; i < count; i++) {
            LinkedHashMap<String, Double> weights = new LinkedHashMap<>();
            if (c.randomItemsNoDropChance > 0) {
                weights.put("NONE", c.randomItemsNoDropChance);
            }
            Map<Tier, List<String>> byTier = new EnumMap<>(Tier.class);
            for (String id : rewards) {
                Tier t = ItemsHelper.getTier(id);
                if (t != null) {
                    byTier.computeIfAbsent(t, k -> new ArrayList<>()).add(id);
                }
            }
            for (Map.Entry<String, Double> te : c.randomItemTierWeights.entrySet()) {
                String tierKey = te.getKey().toUpperCase(Locale.ROOT);
                double tierWeight = te.getValue();
                try {
                    Tier tierEnum = Tier.valueOf(tierKey);
                    List<String> list = byTier.getOrDefault(tierEnum, Collections.emptyList());
                    if (!list.isEmpty()) {
                        double perItem = tierWeight / list.size();
                        for (String id : list) {
                            weights.put(id, perItem);
                        }
                    }
                } catch (IllegalArgumentException ignored) {}
            }
            double total = weights.values().stream().mapToDouble(d -> d).sum();
            double roll  = RNG.nextDouble() * total;
            double cum   = 0;
            for (Map.Entry<String, Double> e : weights.entrySet()) {
                cum += e.getValue();
                if (roll <= cum) {
                    String key = e.getKey();
                    if (!"NONE".equals(key)) {
                        giveOrDrop(player, world, pos, CustomItemRegistry.create(key, 1));
                        player.sendMessage(new StringTextComponent(
                                TextFormatting.AQUA + "✦ ¡Has recibido " +
                                        CustomItemRegistry.create(key,1).getHoverName().getString() + "!"
                        ), player.getUUID());
                    }
                    break;
                }
            }
        }

        // 2) Ejecutar comandos desde consola
        executeCommands(player, c.commands);

        // 3) Upgrade box
        if (c.upgradeBoxChance > 0 && RNG.nextDouble() * 100 < c.upgradeBoxChance) {
            List<String> upgrades = c.boxesUpgrade;
            if (upgrades != null && !upgrades.isEmpty()) {
                String nextId = upgrades.get(RNG.nextInt(upgrades.size()));
                if (CustomItemRegistry.contains(nextId)) {
                    ItemStack nextStack = CustomItemRegistry.create(nextId, 1);
                    giveOrDrop(player, world, pos, nextStack);
                    ITextComponent nameComp = nextStack.getHoverName();
                    player.sendMessage(
                            new StringTextComponent(TextFormatting.YELLOW + "¡Tu caja misteriosa se ha transformado en ")
                                    .append(nameComp)
                                    .append(new StringTextComponent(TextFormatting.GREEN + "!")),
                            player.getUUID()
                    );
                } else {
                    RangerLevels.LOGGER.warn("Intento de upgrade a caja desconocida: {}", nextId);
                }
            }
        }

        // 4) Mensaje de apertura
        ItemStack boxStack = CustomItemRegistry.create(boxId, 1);
        ITextComponent nameComp = boxStack.getHoverName();
        player.sendMessage(
                new StringTextComponent(TextFormatting.GOLD + "✦ ¡Has abierto una ")
                        .append(nameComp)
                        .append(new StringTextComponent(TextFormatting.GOLD + "!")),
                player.getUUID()
        );

        // 5) Rango de Exp
        int expGain = c.expMin + RNG.nextInt(c.expMax - c.expMin + 1);
        LevelProvider.giveExpAndNotify(player, expGain);

        // 6) Recompensas de ítems configuradas
        giveConfiguredRewards(c, player, world, pos);

        // 7) Intento de drop de caja adicional
        tryDropOneOnEvent(player, event,world, pos, c);
    }

    /**
     * Ejecuta la lista de CommandEntry siempre usando la consola (perm level 4).
     */
    private static void executeCommands(ServerPlayerEntity player, List<CommandEntry> commands) {
        if (commands == null || commands.isEmpty()) return;
        MinecraftServer server = player.getServer();
        if (server == null) {
            player.sendMessage(new StringTextComponent(
                    TextFormatting.RED + "Error interno: servidor no disponible para comandos."
            ), player.getUUID());
            return;
        }
        // Creamos un CommandSource con nivel de permiso 4 (consola)
        CommandSource consoleSource = server
                .createCommandSourceStack()
                .withPermission(4)
                .withSuppressedOutput();

        for (CommandEntry ce : commands) {
            if (ce == null || ce.command == null || ce.chancePercent <= 0) continue;
            try {
                if (RNG.nextDouble() * 100 < ce.chancePercent) {
                    String cmd = ce.command.replace("%player%", player.getName().getString());
                    server.getCommands().performCommand(consoleSource, cmd);
                }
            } catch (Exception ex) {
                RangerLevels.LOGGER.error("Error ejecutando comando de MysteryBox: {}", ce.command, ex);
            }
        }
    }

    public static void giveConfiguredRewards(TierBoxConfig c, ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
        Map<String, Integer> itemsChance = c.itemsChance;
        if (itemsChance == null || itemsChance.isEmpty()) return;
        int totalWeight = itemsChance.values().stream().mapToInt(i -> i).sum();
        int roll = RNG.nextInt(totalWeight);
        int cumulative = 0;
        for (Map.Entry<String, Integer> entry : itemsChance.entrySet()) {
            cumulative += entry.getValue();
            if (roll < cumulative) {
                String rawKey = entry.getKey();
                String modid = rawKey.startsWith("PIXELMON_") ? "pixelmon" : "minecraft";
                String path  = rawKey.startsWith("PIXELMON_")
                        ? rawKey.substring("PIXELMON_".length()).toLowerCase(Locale.ROOT)
                        : rawKey.toLowerCase(Locale.ROOT);
                ResourceLocation rl = new ResourceLocation(modid, path);
                ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(rl), 1);
                giveOrDrop(player, world, pos, stack);
                break;
            }
        }
    }

    public enum EventType {
        BEAT_BOSS, LEVEL_UP, RAID, HUNDRED, OPEN_BOX_BLOCK
    }

    public static void tryDropOneOnEvent(ServerPlayerEntity player, EventType event, ServerWorld world, BlockPos pos, TierBoxConfig sourceTier) {
        MysteryBoxesConfig.MysteryBoxConfig cfg = MysteryBoxesConfig.get().mysteryBox;
        LinkedHashMap<String, Double> weights = new LinkedHashMap<>();
        if (sourceTier.noDropChance > 0) {
            weights.put("NONE", sourceTier.noDropChance);
        }
        addWeight(cfg.comun,      MysteryBoxComun.ID,      event, weights);
        addWeight(cfg.raro,       MysteryBoxRaro.ID,       event, weights);
        addWeight(cfg.epico,      MysteryBoxEpico.ID,      event, weights);
        addWeight(cfg.legendario, MysteryBoxLegendario.ID, event, weights);
        addWeight(cfg.estelar,    MysteryBoxEstelar.ID,    event, weights);
        addWeight(cfg.mitico,     MysteryBoxMitico.ID,     event, weights);
        double total = weights.values().stream().mapToDouble(d -> d).sum();
        if (total <= 0) return;
        double roll = RNG.nextDouble() * total;
        double cumulative = 0;
        for (Map.Entry<String, Double> entry : weights.entrySet()) {
            cumulative += entry.getValue();
            if (roll <= cumulative) {
                if (!"NONE".equals(entry.getKey())) {
                    ItemStack stack = CustomItemRegistry.create(entry.getKey(), 1);
                    giveOrDrop(player, world, pos, stack);
                    player.sendMessage(new StringTextComponent(
                            TextFormatting.GOLD + "✦ ¡Has recibido x1 "
                                    + stack.getHoverName().getString()
                                    + " por " + describeEvent(event) + "!"
                    ), player.getUUID());
                }
                return;
            }
        }
    }

    private static void addWeight(TierBoxConfig c, String boxId, EventType event, LinkedHashMap<String, Double> weights) {
        if (!c.enable) return;
        double chance;
        switch (event) {
            case BEAT_BOSS:  chance = c.dropChanceBeatBoss;  break;
            case LEVEL_UP:   chance = c.dropChanceLevelUp;   break;
            case RAID:       chance = c.dropChanceRaid;      break;
            case HUNDRED:    chance = 100.0;                  break;
            default:         return;
        }
        if (chance > 0) weights.put(boxId, chance);
    }

    private static String describeEvent(EventType event) {
        switch (event) {
            case BEAT_BOSS:      return "derrotar un jefe";
            case LEVEL_UP:       return "subir de nivel";
            case RAID:           return "completar una raid";
            case HUNDRED:        return "evento especial";
            case OPEN_BOX_BLOCK: return "abrir una caja misteriosa";
            default:             return "un evento";
        }
    }

    @SubscribeEvent
    public static void onRightClickBox(RightClickBlock event) {
        if (event.getWorld().isClientSide()) return;
        if (!(event.getPlayer() instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity opener = (ServerPlayerEntity) event.getPlayer();
        ServerWorld world = (ServerWorld) event.getWorld();
        BlockPos pos = event.getPos();
        TileEntity te = world.getBlockEntity(pos);
        if (te == null) return;

        final String boxId;
        final UUID ownerUuid;
        if (te instanceof ChestTileEntity) {
            ChestTileEntity chest = (ChestTileEntity) te;
            if (!chest.getTileData().contains(NBT_BOX_ID)) return;
            boxId = chest.getTileData().getString(NBT_BOX_ID);
            ownerUuid = chest.getTileData().contains(NBT_BOX_OWNER)
                    ? chest.getTileData().getUUID(NBT_BOX_OWNER) : null;

        } else if (te instanceof EnderChestTileEntity) {
            EnderChestTileEntity chest = (EnderChestTileEntity) te;
            if (!chest.getTileData().contains(NBT_BOX_ID)) return;
            boxId = chest.getTileData().getString(NBT_BOX_ID);
            ownerUuid = chest.getTileData().contains(NBT_BOX_OWNER)
                    ? chest.getTileData().getUUID(NBT_BOX_OWNER) : null;

        } else {
            return;
        }

        if (ownerUuid == null) {
            opener.sendMessage(new StringTextComponent(TextFormatting.RED
                            + "¡Esta caja no tiene dueño asignado, no se puede abrir!"),
                    opener.getUUID()
            );
            return;
        }

        if (!ownerUuid.equals(opener.getUUID())) {
            ServerPlayerEntity owner = world.getServer().getPlayerList().getPlayer(ownerUuid);
            if (owner != null) {
                owner.sendMessage(new StringTextComponent(TextFormatting.RED
                                + opener.getName().getString()
                                + " te robó la Caja Misteriosa!"),
                        owner.getUUID()
                );
            }
            opener.sendMessage(new StringTextComponent(TextFormatting.GRAY
                            + "Poco moral pero válido... Le robaste la Caja Misteriosa a "
                            + (owner != null ? owner.getName().getString() : "alguien") + "!"),
                    opener.getUUID()
            );
        }

        event.setCanceled(true);
        event.setCancellationResult(ActionResultType.SUCCESS);
        open(opener, boxId, EventType.OPEN_BOX_BLOCK, world, pos);
        world.removeBlock(pos, false);
        PlayerSoundUtils.playSoundToPlayer(
                opener,
                SoundEvents.TOTEM_USE,
                SoundCategory.MASTER,
                1.0f,
                0.7f
        );
    }

    private static final String NBT_BOX_ID    = "RangerBoxID";
    private static final String NBT_BOX_OWNER = "RangerBoxOwner";
}
