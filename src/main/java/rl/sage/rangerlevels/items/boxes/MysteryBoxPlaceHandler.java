// File: rl/sage/rangerlevels/items/boxes/MysteryBoxPlaceHandler.java
package rl.sage.rangerlevels.items.boxes;

import net.minecraft.block.ChestBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.EnderChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.items.RangerItemDefinition;

@Mod.EventBusSubscriber(modid = RangerLevels.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MysteryBoxPlaceHandler {

    private static final String NBT_BOX_ID = "RangerBoxID";

    @SubscribeEvent
    public static void onPlaceBox(EntityPlaceEvent event) {
        // Sólo servidor
        if (event.getWorld().isClientSide()) return;
        // Sólo jugador
        if (!(event.getEntity() instanceof PlayerEntity)) return;
        PlayerEntity player = (PlayerEntity) event.getEntity();

        // Sólo cofres normales o del End
        if (!(event.getPlacedBlock().getBlock() instanceof ChestBlock
                || event.getPlacedBlock().getBlock() instanceof EnderChestBlock)) return;

        // Averigua con qué caja colocó (main u offhand)
        ItemStack main = player.getItemInHand(Hand.MAIN_HAND);
        ItemStack off  = player.getItemInHand(Hand.OFF_HAND);
        String id = RangerItemDefinition.getIdFromStack(main);
        if (id == null) {
            id = RangerItemDefinition.getIdFromStack(off);
        }
        if (id == null) return;  // no era una MysteryBox

        // Marca el TileEntity con nuestro ID
        ServerWorld world = (ServerWorld) event.getWorld();
        BlockPos pos = event.getPos();
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof ChestTileEntity) {
            ((ChestTileEntity) te).getTileData().putString(NBT_BOX_ID, id);
        } else if (te instanceof EnderChestTileEntity) {
            ((EnderChestTileEntity) te).getTileData().putString(NBT_BOX_ID, id);
        }
    }
}
