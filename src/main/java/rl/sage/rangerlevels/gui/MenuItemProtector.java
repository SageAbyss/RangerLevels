package rl.sage.rangerlevels.gui;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class MenuItemProtector {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Solo en fase END y lado servidor
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level.isClientSide) return;

        ServerPlayerEntity player = (ServerPlayerEntity) event.player;

        // 1) Limpia el cursor y restaura slot original si hay un MenuItem en él
        ItemStack carried = player.inventory.getCarried();
        if (isMenuItem(carried)) {
            CompoundNBT tag = carried.getTag();
            if (tag != null && tag.contains("MenuSlot")) {
                int targetSlot = tag.getInt("MenuSlot");
                if (player.containerMenu instanceof MainMenuContainer
                        && targetSlot >= 0
                        && targetSlot < player.containerMenu.slots.size()) {
                    Slot slot = player.containerMenu.slots.get(targetSlot);
                    if (slot.getItem().isEmpty()) {
                        slot.set(carried.copy());
                        slot.setChanged();
                    }
                }
            }
            player.inventory.setCarried(ItemStack.EMPTY);
        }

        // 2) Escanea TODO el inventario del jugador y elimina botones que se hayan colado
        int invSize = player.inventory.getContainerSize();
        boolean removedAny = false;
        for (int i = 0; i < invSize; i++) {
            ItemStack stack = player.inventory.getItem(i);
            if (isMenuItem(stack)) {
                player.inventory.setItem(i, ItemStack.EMPTY);
                removedAny = true;
            }
        }

        // 3) Forzar sincronización
        // 3a) Si sigue abierto tu menú, actualiza el container del menú
        if (player.containerMenu instanceof MainMenuContainer) {
            player.containerMenu.broadcastChanges();
        }
        // 3b) Si hemos eliminado algo del inventario del jugador, forzamos su propio inventario
        if (removedAny) {
            player.inventoryMenu.broadcastChanges();
        }
    }

    private static boolean isMenuItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        CompoundNBT tag = stack.getTag();
        return tag != null
                && tag.contains("MenuButtonID")
                && tag.contains("MenuSlot");
    }
}
