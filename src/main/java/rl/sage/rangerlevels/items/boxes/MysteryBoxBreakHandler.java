// File: src/main/java/rl/sage/rangerlevels/items/boxes/MysteryBoxBreakHandler.java
package rl.sage.rangerlevels.items.boxes;

import net.minecraft.block.Blocks;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.EnderChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = RangerLevels.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MysteryBoxBreakHandler {

    private static final String NBT_BOX_ID    = "RangerBoxID";
    private static final String NBT_BOX_OWNER = "RangerBoxOwner";

    @SubscribeEvent
    public static void onBoxBroken(BreakEvent event) {
        if (event.getWorld().isClientSide()) return;
        if (!(event.getPlayer() instanceof PlayerEntity)) return;

        ServerWorld world = (ServerWorld) event.getWorld();
        PlayerEntity breaker = event.getPlayer();
        BlockPos pos = event.getPos();
        TileEntity te = world.getBlockEntity(pos);

        // Aceptar ChestTileEntity o EnderChestTileEntity
        if (!(te instanceof ChestTileEntity) && !(te instanceof EnderChestTileEntity)) {
            return;
        }
        // Obtener el CompoundNBT
        CompoundNBT data = te.getTileData();
        // Si no tiene ambas claves, salimos
        if (!data.contains(NBT_BOX_ID) || !data.contains(NBT_BOX_OWNER)) {
            return;
        }
        // Extraer NBT desde cualquiera de los dos
        String boxId;
        UUID ownerUuid;
        if (te instanceof ChestTileEntity) {
            ChestTileEntity chest = (ChestTileEntity) te;
            boxId     = chest.getTileData().getString(NBT_BOX_ID);
            ownerUuid = chest.getTileData().getUUID(NBT_BOX_OWNER);
        } else {
            EnderChestTileEntity ender = (EnderChestTileEntity) te;
            boxId     = ender.getTileData().getString(NBT_BOX_ID);
            ownerUuid = ender.getTileData().getUUID(NBT_BOX_OWNER);
        }

        if (boxId == null || boxId.isEmpty()) return;

        // Notificar robo si no es el dueño
        if (ownerUuid != null && !ownerUuid.equals(breaker.getUUID())) {
            ServerPlayerEntity owner = world.getServer()
                    .getPlayerList()
                    .getPlayer(ownerUuid);
            if (owner != null) {
                owner.sendMessage(
                        new StringTextComponent(TextFormatting.RED
                                + breaker.getName().getString()
                                + " te robó la Caja Misteriosa!"),
                        owner.getUUID()
                );
            }
            breaker.sendMessage(
                    new StringTextComponent(TextFormatting.GRAY
                            + "Poco moral pero válido... Le robaste la Caja Misteriosa a "
                            + (owner != null ? owner.getName().getString() : "alguien") + "!"),
                    breaker.getUUID()
            );
            if (owner != null) {
                PlayerSoundUtils.playSoundToPlayer(
                        owner,
                        SoundEvents.VILLAGER_NO,
                        SoundCategory.MASTER,
                        1.0f,
                        1.0f
                );
            }
            if (breaker instanceof ServerPlayerEntity) {
                PlayerSoundUtils.playSoundToPlayer(
                        (ServerPlayerEntity) breaker,
                        SoundEvents.VILLAGER_CELEBRATE,
                        SoundCategory.MASTER,
                        1.0f,
                        1.0f
                );
            }
        }

        // Evitar drop normal, quitar bloque y soltar la caja
        event.setCanceled(false);
        world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);

        ItemStack box = CustomItemRegistry.create(boxId, 1);
        world.addFreshEntity(new ItemEntity(
                world,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                box
        ));
    }
}
