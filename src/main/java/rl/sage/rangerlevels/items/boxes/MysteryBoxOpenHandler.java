// File: rl/sage/rangerlevels/items/boxes/MysteryBoxOpenHandler.java
package rl.sage.rangerlevels.items.boxes;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.EnderChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = RangerLevels.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MysteryBoxOpenHandler {

    private static final String NBT_BOX_ID    = "RangerBoxID";
    private static final String NBT_BOX_OWNER = "RangerBoxOwner";

    @SubscribeEvent
    public static void onRightClickBox(RightClickBlock event) {
        // Sólo servidor y jugadores tipo ServerPlayerEntity
        if (event.getWorld().isClientSide()) return;
        if (!(event.getPlayer() instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity opener = (ServerPlayerEntity) event.getPlayer();
        ServerWorld world = (ServerWorld) event.getWorld();
        BlockPos pos = event.getPos();

        TileEntity te = world.getBlockEntity(pos);
        if (te == null) return;

        // Obtenemos el CompoundNBT del cofre
        final String boxId;
        final UUID ownerUuid;

        if (te instanceof ChestTileEntity) {
            ChestTileEntity chest = (ChestTileEntity) te;
            if (!chest.getTileData().contains(NBT_BOX_ID)) return;
            boxId = chest.getTileData().getString(NBT_BOX_ID);

            if (chest.getTileData().contains(NBT_BOX_OWNER)) {
                ownerUuid = chest.getTileData().getUUID(NBT_BOX_OWNER);
            } else {
                ownerUuid = null;
            }

        } else if (te instanceof EnderChestTileEntity) {
            EnderChestTileEntity chest = (EnderChestTileEntity) te;
            if (!chest.getTileData().contains(NBT_BOX_ID)) return;
            boxId = chest.getTileData().getString(NBT_BOX_ID);

            if (chest.getTileData().contains(NBT_BOX_OWNER)) {
                ownerUuid = chest.getTileData().getUUID(NBT_BOX_OWNER);
            } else {
                ownerUuid = null;
            }

        } else {
            return; // no es un cofre que gestionemos
        }

        // Si no tiene dueño asignado, avisamos y salimos
        if (ownerUuid == null) {
            opener.sendMessage(
                    new StringTextComponent(TextFormatting.RED
                            + "¡Esta caja no tiene dueño asignado, no se puede abrir!"),
                    opener.getUUID()
            );
            return;
        }

        // Si el que abre no es el dueño, notificamos robo
        if (!ownerUuid.equals(opener.getUUID())) {
            ServerPlayerEntity owner = world.getServer()
                    .getPlayerList()
                    .getPlayer(ownerUuid);
            if (owner != null) {
                owner.sendMessage(
                        new StringTextComponent(TextFormatting.RED
                                + opener.getName().getString()
                                + " te robó la Caja Misteriosa!"),
                        owner.getUUID()
                );
            }
            opener.sendMessage(
                    new StringTextComponent(TextFormatting.GRAY
                            + "Poco moral pero válido... Le robaste la Caja Misteriosa a "
                            + (owner != null ? owner.getName().getString() : "alguien") + "!"),
                    opener.getUUID()
            );
        }

        // Cancelar apertura vanilla y ejecutar recompensas
        event.setCanceled(true);
        event.setCancellationResult(ActionResultType.SUCCESS);
        MysteryBoxHelper.open(opener, boxId, MysteryBoxHelper.EventType.OPEN_BOX_BLOCK, world, pos);

        // Remover bloque y reproducir sonido
        world.removeBlock(pos, false);
        PlayerSoundUtils.playSoundToPlayer(
                opener,
                SoundEvents.TOTEM_USE,
                SoundCategory.MASTER,
                1.0f,
                0.7f
        );
    }
}
