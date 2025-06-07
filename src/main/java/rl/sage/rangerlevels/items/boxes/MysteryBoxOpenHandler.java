// File: rl/sage/rangerlevels/items/boxes/MysteryBoxOpenHandler.java
package rl.sage.rangerlevels.items.boxes;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.EnderChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.RangerLevels;

@Mod.EventBusSubscriber(modid = RangerLevels.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MysteryBoxOpenHandler {

    private static final String NBT_BOX_ID = "RangerBoxID";

    @SubscribeEvent
    public static void onRightClickBox(RightClickBlock event) {
        // Sólo servidor
        if (event.getWorld().isClientSide()) return;
        // Sólo jugadores servidors
        if (!(event.getPlayer() instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

        BlockPos pos = event.getPos();
        ServerWorld world = (ServerWorld) event.getWorld();
        TileEntity te = world.getBlockEntity(pos);
        String boxId = null;

        if (te instanceof ChestTileEntity) {
            chestCheck: {
                boxId = ((ChestTileEntity) te).getTileData().getString(NBT_BOX_ID);
                if (boxId.isEmpty()) break chestCheck;
            }
        } else if (te instanceof EnderChestTileEntity) {
            enderCheck: {
                boxId = ((EnderChestTileEntity) te).getTileData().getString(NBT_BOX_ID);
                if (boxId.isEmpty()) break enderCheck;
            }
        }

        if (boxId == null || boxId.isEmpty()) return;  // no es nuestra caja

        // Cancelamos la apertura normal
        event.setCanceled(true);
        event.setCancellationResult(ActionResultType.SUCCESS);

        // Ejecuta la recompensa y elimina el cofre
        MysteryBoxHelper.open(player, boxId, MysteryBoxHelper.EventType.OPEN_BOX_BLOCK);
        world.removeBlock(pos, false);
    }
}
