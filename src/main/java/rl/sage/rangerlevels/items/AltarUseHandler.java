package rl.sage.rangerlevels.items;

import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Blocks;
import rl.sage.rangerlevels.util.PlayerSoundUtils;
import com.pixelmonmod.pixelmon.init.registry.SoundRegistration;
import net.minecraft.util.SoundCategory;

@Mod.EventBusSubscriber(modid = "rangerlevels")
public class AltarUseHandler {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock ev) {
        World world = ev.getWorld();
        if (world.isClientSide()) return;

        ServerPlayerEntity player = (ServerPlayerEntity) ev.getPlayer();
        Hand hand = ev.getHand();
        ItemStack stack = player.getItemInHand(hand);
        // Verificar altar
        String rid = null;
        if (stack.hasTag() && stack.getTag().contains(RangerItemDefinition.NBT_ID_KEY)) {
            rid = stack.getTag().getString(RangerItemDefinition.NBT_ID_KEY);
        }
        if (!AltarArcano.ID.equals(rid)) return;

        ev.setCancellationResult(ActionResultType.CONSUME);
        ev.setCanceled(true);

        // Posición donde se intentaría colocar
        BlockPos placePos = ev.getPos().relative(ev.getFace());

        // Verificar estructura antes de recetas
        if (!(world instanceof ServerWorld) || !checkInvocationStructure((ServerWorld) world, placePos)) {
            player.sendMessage(new StringTextComponent(
                    "§cNo puedes colocar el Altar Supremo \n§6Requiere: Estructura de Invocación"
            ), player.getUUID());
            // resincronizar slot
            int windowId = 0;
            int slotIndex = (hand == Hand.MAIN_HAND)
                    ? 36 + player.inventory.selected
                    : 45;
            ItemStack actual = (hand == Hand.MAIN_HAND)
                    ? player.inventory.getItem(player.inventory.selected)
                    : player.inventory.offhand.get(0);
            player.connection.send(new SSetSlotPacket(windowId, slotIndex, actual));
            player.inventoryMenu.broadcastChanges();
            return;
        }

        // Buscar recetas válidas
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
            ), player.getUUID());
            int windowId = 0;
            int slotIndex = (hand == Hand.MAIN_HAND)
                    ? 36 + player.inventory.selected
                    : 45;
            ItemStack actual = (hand == Hand.MAIN_HAND)
                    ? player.inventory.getItem(player.inventory.selected)
                    : player.inventory.offhand.get(0);
            player.connection.send(new SSetSlotPacket(windowId, slotIndex, actual));
            player.inventoryMenu.broadcastChanges();
            return;
        }

        // Consumir altar YA
        stack.shrink(1);

        PlayerSoundUtils.playPixelmonSoundToAllPlayers(
                player.getServer(),
                SoundRegistration.MYSTERY_BOX_OPEN,
                SoundCategory.PLAYERS,
                1.0f,
                0.5f
        );

        int windowId = 0;
        int slotIndex = (hand == Hand.MAIN_HAND)
                ? 36 + player.inventory.selected
                : 45;
        ItemStack after = (hand == Hand.MAIN_HAND)
                ? player.inventory.getItem(player.inventory.selected)
                : player.inventory.offhand.get(0);
        player.connection.send(new SSetSlotPacket(windowId, slotIndex, after));

        // Iniciar sesión de invocación
        new InvocationSession(player, stack, chosen, placePos);
    }

    private static boolean checkInvocationStructure(ServerWorld world, BlockPos placePos) {
        BlockPos centerBelow = placePos.below();
        if (world.getBlockState(centerBelow).getBlock() != Blocks.GOLD_BLOCK) {
            return false;
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos posBelow = centerBelow.offset(dx, 0, dz);
                if (dx == 0 && dz == 0) continue;
                if (world.getBlockState(posBelow).getBlock() != Blocks.OBSIDIAN) {
                    return false;
                }
                if (Math.abs(dx) == 1 && Math.abs(dz) == 1) {
                    BlockPos posTorch = posBelow.above();
                    if (world.getBlockState(posTorch).getBlock() != Blocks.SOUL_TORCH) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
