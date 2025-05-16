package rl.sage.rangerlevels.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SSetSlotPacket;

public class MainMenuContainer extends ChestContainer {
    private final Inventory menuInv;

    public MainMenuContainer(int windowId, PlayerInventory playerInv, Inventory menuInventory) {
        super(ContainerType.GENERIC_9x3, windowId, playerInv, menuInventory, 3);
        this.menuInv = menuInventory;
        this.menuInv.startOpen(playerInv.player);

        // Reemplazamos los primeros 27 slots por MenuSlot
        for (int i = 0; i < menuInv.getContainerSize(); i++) {
            Slot old = this.slots.get(i);
            MenuSlot menuSlot = new MenuSlot(menuInv, i, old.x, old.y);
            menuSlot.set(old.getItem());
            this.slots.set(i, menuSlot);
        }
    }

    /** 1) Bloquea shift-click (quick-move) y recoloca el ítem en su slot */
    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int index) {
        if (index >= 0 && index < menuInv.getContainerSize()) {
            Slot slot = this.slots.get(index);
            ItemStack original = slot.getItem();
            slot.set(original);
            return original.isEmpty() ? ItemStack.EMPTY : original.copy();
        }
        return ItemStack.EMPTY;
    }

    /** 2) Bloquea “pick all” (tecla Q) y recoloca el ítem */
    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        if (slot.container == this.menuInv) {
            ItemStack original = slot.getItem();
            slot.set(original);
            return false;
        }
        return super.canTakeItemForPickAll(stack, slot);
    }

    /** 3) Bloquea arrastre de stacks entre slots */
    @Override
    public boolean canDragTo(Slot slot) {
        return false;
    }

    /** 4) Bloquea moveItemStackTo (p. ej. shift+drag) */
    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        return false;
    }

    /**
     * 5) Captura **todos** los clics (izq, der, shift, arrastre...).
     *    Para slots de menú: ejecuta acción, restaura ítem, limpia cursor.
     */
    @Override
    public ItemStack clicked(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        if (slotId >= 0 && slotId < menuInv.getContainerSize()) {
            Slot slot = this.slots.get(slotId);
            ItemStack original = slot.getItem();

            if (isMenuItem(original) && player instanceof ServerPlayerEntity) {
                String id = original.getTag().getString("MenuButtonID");
                ServerPlayerEntity sp = (ServerPlayerEntity) player;

                // limpia cursor en cliente y servidor
                clearPlayerCursor(sp);
                sp.inventory.setCarried(ItemStack.EMPTY);
            }

            // restaurar y sincronizar
            slot.set(original.copy());
            slot.setChanged();
            this.broadcastChanges();

            return original.copy();
        }

        // bloquea otros clics peligrosos
        if (clickTypeIn == ClickType.SWAP
                || clickTypeIn == ClickType.THROW
                || clickTypeIn == ClickType.QUICK_MOVE
                || clickTypeIn == ClickType.CLONE
                || clickTypeIn == ClickType.PICKUP_ALL) {
            return ItemStack.EMPTY;
        }

        return super.clicked(slotId, dragType, clickTypeIn, player);
    }

    /** Envía paquete para limpiar cursor en cliente */
    private void clearPlayerCursor(ServerPlayerEntity player) {
        player.connection.send(new SSetSlotPacket(-1, 0, ItemStack.EMPTY));
    }

    @Override
    public boolean stillValid(PlayerEntity playerIn) {
        return true; // no se cierra solo
    }

    private boolean isMenuItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        CompoundNBT tag = stack.getTag();
        return tag != null
                && tag.contains("MenuButtonID")
                && tag.contains("MenuSlot");
    }
}
