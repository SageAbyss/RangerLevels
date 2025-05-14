package rl.sage.rangerlevels.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class MenuSlot extends Slot {
    public MenuSlot(Inventory inv, int index, int xPosition, int yPosition) {
        super(inv, index, xPosition, yPosition);
    }

    // 1) Prohíbe poner ítems en el slot
    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    // 2) Prohíbe sacar con clic normal
    @Override
    public boolean mayPickup(PlayerEntity playerIn) {
        return false;
    }

    // 3) Anula la retirada de cantidad (shift+clic, arrastre)
    @Override
    public ItemStack remove(int amount) {
        return ItemStack.EMPTY;
    }

    // 4) Intercepta cuando el cliente "toma" el ítem con clic normal
    //    Debe retornar ItemStack para coincidir con la firma de Forge 1.16.5
    @Override
    public ItemStack onTake(PlayerEntity playerIn, ItemStack stack) {
        // Reponemos inmediatamente la pila original
        this.container.setItem(this.getSlotIndex(), this.getItem().copy());
        // Devolvemos EMPTY para que el cliente no reciba nada
        return ItemStack.EMPTY;
    }
}
