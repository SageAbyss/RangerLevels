// src/main/java/rl/sage/rangerlevels/items/altar/AltarUseHandler.java
package rl.sage.rangerlevels.items.altar;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.altar.AltarArcano;
import rl.sage.rangerlevels.items.altar.AltarRecipe;
import rl.sage.rangerlevels.items.altar.AltarCraftHelper;
import rl.sage.rangerlevels.items.altar.InvocationSession;
import rl.sage.rangerlevels.util.PlayerSoundUtils;
import com.pixelmonmod.pixelmon.init.registry.SoundRegistration;
import net.minecraft.util.SoundCategory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = RangerLevels.MODID)
public class AltarUseHandler {

    /** Cooldown de jugadores: UUID → tick (gameTime) hasta el que está en espera */
    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock ev) {
        World world = ev.getWorld();
        if (world.isClientSide()) return;

        if (!(world instanceof ServerWorld)) return;
        ServerWorld sworld = (ServerWorld) world;
        long currentTick = sworld.getGameTime();

        ServerPlayerEntity player = (ServerPlayerEntity) ev.getPlayer();
        UUID uuid = player.getUUID();

        Hand hand = ev.getHand();
        ItemStack stack = player.getItemInHand(hand);

        // 1) Solo continuamos si es el Altar Supremo
        String rid = stack.hasTag() && stack.getTag().contains(RangerItemDefinition.NBT_ID_KEY)
                ? stack.getTag().getString(RangerItemDefinition.NBT_ID_KEY)
                : null;
        if (!AltarArcano.ID.equals(rid)) return;

        // Comprobar cooldown: 10s = 200 ticks
        Long nextAllowed = COOLDOWNS.get(uuid);
        if (nextAllowed != null && currentTick < nextAllowed) {
            long remaining = nextAllowed - currentTick;
            long seconds = (remaining + 19) / 20;
            player.sendMessage(
                    new StringTextComponent("§cDebes esperar " + seconds + "s para volver a usar el Altar Arcano."),
                    uuid
            );
            // Resincronizar slot para “devolver” el altar en la mano
            ev.setCanceled(true);
            ev.setCancellationResult(ActionResultType.CONSUME);
            syncSlot(player, hand);
            return;
        }

        ev.setCanceled(true);
        ev.setCancellationResult(ActionResultType.CONSUME);

        BlockPos placePos = ev.getPos().relative(ev.getFace());

        // 2) Estructura de invocación
        if (!checkInvocationStructure(sworld, placePos)) {
            player.sendMessage(new StringTextComponent(
                    "§cNo puedes colocar el Altar Arcano\n§6Requiere: Estructura de Invocación"
            ), uuid);
            // Resincronizar slot para devolver el altar
            syncSlot(player, hand);
            return;
        }

        // 3) Buscar receta válida
        AltarRecipe chosen = null;
        for (AltarRecipe recipe : AltarCraftHelper.getAllRecipes()) {
            if (recipe.matches(player)) {
                chosen = recipe;
                break;
            }
        }
        if (chosen == null) {
            player.sendMessage(new StringTextComponent(
                    "§cNo cumples los requisitos para ninguna invocación."
            ), uuid);
            syncSlot(player, hand);
            return;
        }

        // 4) Consumir altar, reproducir sonido y reenvío de slot
        stack.shrink(1);
        PlayerSoundUtils.playPixelmonSoundToAllPlayers(
                player.getServer(),
                SoundRegistration.MYSTERY_BOX_OPEN,
                SoundCategory.PLAYERS,
                1.0f, 0.5f
        );
        syncSlot(player, hand);

        // 5) Iniciar sesión de invocación
        new InvocationSession(player, stack, chosen, placePos);

        // 6) Registrar cooldown (200 ticks = 10s)
        COOLDOWNS.put(uuid, currentTick + 200);
    }

    private static boolean checkInvocationStructure(ServerWorld world, BlockPos origin) {
        int y0 = origin.getY() - 1;
        // Layer 0: 5×5 ring at y0, center GLOWSTONE, resto RED_NETHER_BRICKS excepto esquinas
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos pos = origin.offset(dx, -1, dz);
                if (dx == 0 && dz == 0) {
                    if (world.getBlockState(pos).getBlock() != Blocks.GLOWSTONE) return false;
                }
                else if (Math.abs(dx) == 2 && Math.abs(dz) == 2) {
                    // esquinas: ignorar
                    continue;
                }
                else {
                    if (world.getBlockState(pos).getBlock() != Blocks.RED_NETHER_BRICKS) return false;
                }
            }
        }

        // Layer 1: cuatro RED_NETHER_BRICKS en (±1,0,±1)
        for (int dx : new int[]{-1, 1}) {
            for (int dz : new int[]{-1, 1}) {
                BlockPos pos = origin.offset(dx, 0, dz);
                if (world.getBlockState(pos).getBlock() != Blocks.RED_NETHER_BRICKS) return false;
            }
        }

        // Layer 2: cuatro RED_NETHER_BRICK_WALL encima de esas posiciones
        for (int dx : new int[]{-1, 1}) {
            for (int dz : new int[]{-1, 1}) {
                BlockPos pos = origin.offset(dx, 1, dz);
                if (world.getBlockState(pos).getBlock() != Blocks.RED_NETHER_BRICK_WALL) return false;
            }
        }

        return true;
    }

    private static void syncSlot(ServerPlayerEntity player, Hand hand) {
        int windowId  = 0;
        int slotIndex = hand == Hand.MAIN_HAND
                ? 36 + player.inventory.selected
                : 45;
        ItemStack current = hand == Hand.MAIN_HAND
                ? player.inventory.getItem(player.inventory.selected)
                : player.inventory.offhand.get(0);
        player.connection.send(new SSetSlotPacket(windowId, slotIndex, current));
        player.inventoryMenu.broadcastChanges();
    }
}
