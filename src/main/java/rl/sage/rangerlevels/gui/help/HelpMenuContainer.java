package rl.sage.rangerlevels.gui.help;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.gui.MenuItemBuilder;
import rl.sage.rangerlevels.gui.MenuSlot;
import rl.sage.rangerlevels.gui.MainMenu;

public class HelpMenuContainer extends ChestContainer {
    private final Inventory menuInv;

    public HelpMenuContainer(int windowId, PlayerInventory playerInv, Inventory menuInventory) {
        super(ContainerType.GENERIC_9x3, windowId, playerInv, menuInventory, 3);
        this.menuInv = menuInventory;
        this.menuInv.startOpen(playerInv.player);

        // Reemplaza los primeros 27 slots por MenuSlot
        for (int i = 0; i < menuInv.getContainerSize(); i++) {
            Slot old = this.slots.get(i);
            MenuSlot menuSlot = new MenuSlot(menuInv, i, old.x, old.y);
            menuSlot.set(old.getItem());
            this.slots.set(i, menuSlot);
        }
    }

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

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        if (slot.container == this.menuInv) {
            ItemStack original = slot.getItem();
            slot.set(original);
            return false;
        }
        return super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public boolean canDragTo(Slot slot) {
        return false;
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        return false;
    }

    @Override
    public ItemStack clicked(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        if (slotId >= 0 && slotId < menuInv.getContainerSize()) {
            Slot slot = this.slots.get(slotId);
            ItemStack original = slot.getItem();

            if (player instanceof ServerPlayerEntity) {
                ServerPlayerEntity sp = (ServerPlayerEntity) player;
                String id = original.hasTag() ? original.getTag().getString("MenuButtonID") : "";

                // Limpia cursor
                sp.connection.send(new SSetSlotPacket(-1, 0, ItemStack.EMPTY));
                sp.inventory.setCarried(ItemStack.EMPTY);

                switch (id) {
                    case "back":
                        // Volver al menú principal
                        sp.closeContainer();
                        MainMenu.open(sp);
                        break;
                    // Aquí podrías añadir más opciones de ayuda si lo deseas:
                    case "topic1":
                        // Lógica para mostrar topic1
                        break;
                    case "topic2":
                        // Lógica para mostrar topic2
                        break;
                }

                // Restaurar ítem
                slot.set(original.copy());
                slot.setChanged();
                this.broadcastChanges();
                sp.connection.send(new SSetSlotPacket(this.containerId, slotId, original.copy()));

                return original.copy();
            }

            // En cliente o no ServerPlayer
            slot.set(original.copy());
            slot.setChanged();
            this.broadcastChanges();
            return ItemStack.EMPTY;
        }

        return super.clicked(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public boolean stillValid(PlayerEntity playerIn) {
        return true;
    }
}