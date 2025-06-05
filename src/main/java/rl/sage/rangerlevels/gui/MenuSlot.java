package rl.sage.rangerlevels.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

/**
 * Slot para menús: impide mover ítems de cualquier forma,
 * y, cuando se hace clic izquierdo normal sobre él, llama a handleButtonClick(...).
 */
public class MenuSlot extends Slot {

    public MenuSlot(net.minecraft.inventory.Inventory inv, int index, int xPosition, int yPosition) {
        super(inv, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(PlayerEntity playerIn) {
        return false;
    }

    @Override
    public ItemStack remove(int amount) {
        return ItemStack.EMPTY;
    }

    /**
     * Este método se invoca cuando el jugador hace clic izquierdo normal
     * sobre este slot (ItemStack stack = this.getItem()).
     *
     * Ahora: si el ItemStack lleva tag “MenuButtonID”, llamamos a handleButtonClick(...)
     * en el contenedor actual, y luego RESTAURAMOS la pila (sin eliminarla).
     * Devolvemos EMPTY para que el cliente no reciba nada en cursor.
     */
    @Override
    public ItemStack onTake(PlayerEntity playerIn, ItemStack stack) {
        // 1) Si el stack lleva el tag MenuButtonID, extraemos el ID y disparamos la acción
        if (stack.hasTag() && stack.getTag().contains("MenuButtonID")) {
            String buttonId = stack.getTag().getString("MenuButtonID");
            // Si estamos en servidor y el contenedor es BaseMenuContainer, llamamos a handleButtonClick:
            if (playerIn instanceof ServerPlayerEntity) {
                ServerPlayerEntity sp = (ServerPlayerEntity) playerIn;
                if (sp.containerMenu instanceof BaseMenuContainer) {
                    ((BaseMenuContainer) sp.containerMenu).handleButtonClick(buttonId, sp);
                }
            }
        }

        // 2) Restauramos inmediatamente la pila original en este slot
        this.container.setItem(this.getSlotIndex(), this.getItem().copy());
        this.setChanged();

        // 3) Limpiamos el cursor en cliente: devolvemos EMPTY para que no quede nada en mouse
        return ItemStack.EMPTY;
    }
}
