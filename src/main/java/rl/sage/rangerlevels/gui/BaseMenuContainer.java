package rl.sage.rangerlevels.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SSetSlotPacket;

public abstract class BaseMenuContainer extends ChestContainer {

    protected final IInventory menuInv;

    public BaseMenuContainer(ContainerType<? extends ChestContainer> type,
                             int windowId,
                             IInventory menuInv,
                             PlayerInventory playerInv,
                             int rows) {
        super(type, windowId, playerInv, menuInv, rows);
        this.menuInv = menuInv;
        // Usa playerInv.player para obtener el PlayerEntity
        this.menuInv.startOpen(playerInv.player);

        // Reemplazar los primeros N slots por MenuSlot
        for (int i = 0; i < menuInv.getContainerSize(); i++) {
            Slot old = this.slots.get(i);
            // Asegúrate de que MenuSlot acepte IInventory;
            // si no, castea: new MenuSlot((Inventory) menuInv, i, old.x, old.y)
            MenuSlot ms = new MenuSlot((Inventory) menuInv, i, old.x, old.y);
            ms.set(old.getItem());
            this.slots.set(i, ms);
        }
    }

    // 1) Bloqueo de SHIFT‑CLICK ocultando método de la superclase
    public ItemStack transferStackInSlot(PlayerEntity player, int index) {
        if (index >= 0 && index < menuInv.getContainerSize()) {
            Slot slot = this.slots.get(index);
            ItemStack original = slot.getItem();
            slot.set(original.copy());
            slot.setChanged();
            this.broadcastChanges();
        }
        return ItemStack.EMPTY;
    }

    // 2) Refuerzo extra
    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int index) {
        return ItemStack.EMPTY;
    }

    // 3) Desactivar cualquier drag/merge automático
    @Override
    public boolean canDragTo(Slot slotIn) {
        return false;
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        return false;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slotIn) {
        int idx = slotIn.getSlotIndex(); // en mappings Yarn esto existe
        if (idx >= 0 && idx < menuInv.getContainerSize()) {
            slotIn.set(slotIn.getItem());
            return false;
        }
        return super.canTakeItemForPickAll(stack, slotIn);
    }


    // 4) Interceptar todos los clicks no deseados y restaurar
    @Override
    public ItemStack clicked(int slotId, int dragType, ClickType clickType, PlayerEntity player) {
        // Shift, Ctrl+Q, Q, swap, clone y quick-craft (drag)
        if (clickType == ClickType.QUICK_MOVE
                || clickType == ClickType.PICKUP_ALL
                || clickType == ClickType.THROW
                || clickType == ClickType.SWAP
                || clickType == ClickType.CLONE
                || clickType == ClickType.QUICK_CRAFT) {

            if (slotId >= 0 && slotId < menuInv.getContainerSize()) {
                Slot slot = this.slots.get(slotId);
                ItemStack original = slot.getItem();
                if (player instanceof ServerPlayerEntity) {
                    ((ServerPlayerEntity) player).connection.send(
                            new SSetSlotPacket(-1, 0, ItemStack.EMPTY)
                    );
                    player.inventory.setCarried(ItemStack.EMPTY);
                }
                slot.set(original.copy());
                slot.setChanged();
                this.broadcastChanges();
            }
            return ItemStack.EMPTY;
        }

        // Click válido en tu menú: dispara acción y restaura
        if (slotId >= 0 && slotId < menuInv.getContainerSize()) {
            Slot slot = this.slots.get(slotId);
            ItemStack original = slot.getItem();
            if (isMenuItem(original) && player instanceof ServerPlayerEntity) {
                String id = original.getTag().getString("MenuButtonID");
                handleMenuAction(id, (ServerPlayerEntity) player);

                ((ServerPlayerEntity) player).connection.send(
                        new SSetSlotPacket(-1, 0, ItemStack.EMPTY)
                );
                player.inventory.setCarried(ItemStack.EMPTY);

                slot.set(original.copy());
                slot.setChanged();
                this.broadcastChanges();
            }
            return ItemStack.EMPTY;
        }

        // Slots de jugador: delegar
        return super.clicked(slotId, dragType, clickType, player);
    }

    protected void handleMenuAction(String id, ServerPlayerEntity player) {
        // Sobrescribe en tus subclases
    }

    protected boolean isMenuItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        CompoundNBT tag = stack.getTag();
        return tag != null && tag.contains("MenuButtonID") && tag.contains("MenuSlot");
    }

    // 5) Cierre y validación de alcance
    @Override
    public boolean stillValid(PlayerEntity player) {
        return this.menuInv.stillValid(player);
    }

    @Override
    public void removed(PlayerEntity player) {
        super.removed(player);
        this.menuInv.stopOpen(player);
    }
}
