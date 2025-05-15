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
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.pass.PassManager;

public class BuyPassContainer extends ChestContainer {
    private final Inventory menuInv;

    public BuyPassContainer(int windowId, PlayerInventory playerInv, Inventory menuInventory) {
        super(ContainerType.GENERIC_9x3, windowId, playerInv, menuInventory, 3);
        this.menuInv = menuInventory;
        this.menuInv.startOpen(playerInv.player);

        // Reemplaza todos los slots por MenuSlot
        for (int i = 0; i < menuInv.getContainerSize(); i++) {
            Slot old = this.slots.get(i);
            MenuSlot ms = new MenuSlot(menuInv, i, old.x, old.y);
            ms.set(old.getItem());
            this.slots.set(i, ms);
        }
    }

    @Override
    public boolean stillValid(PlayerEntity playerIn) {
        return true;  // No se cierra solo
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
    public ItemStack clicked(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        if (slotId >= 0 && slotId < menuInv.getContainerSize()) {
            Slot slot = this.slots.get(slotId);
            ItemStack stack = slot.getItem();
            if (isMenuItem(stack) && player instanceof ServerPlayerEntity) {
                String id = stack.getTag().getString("MenuButtonID");
                ServerPlayerEntity sp = (ServerPlayerEntity) player;

                switch (id) {
                    case "super":
                        sp.sendMessage(PassManager.PassType.SUPER.getGradientDisplayName()
                                        .append(new StringTextComponent("\nBeneficios: ...\nCompra: www.tu-tienda.com/super")),
                                sp.getUUID());
                        break;
                    case "ultra":
                        sp.sendMessage(PassManager.PassType.ULTRA.getGradientDisplayName()
                                        .append(new StringTextComponent("\nBeneficios: ...\nCompra: www.tu-tienda.com/ultra")),
                                sp.getUUID());
                        break;
                    case "master":
                        sp.sendMessage(PassManager.PassType.MASTER.getGradientDisplayName()
                                        .append(new StringTextComponent("\nBeneficios: ...\nCompra: www.tu-tienda.com/master")),
                                sp.getUUID());
                        break;
                    case "current":
                        PassManager.PassType current = PassManager.getPass(sp);
                        sp.sendMessage(current.getGradientDisplayName()
                                        .append(new StringTextComponent("\nTu pase actual con tier " + current.getTier())),
                                sp.getUUID());
                        break;
                }
            }
            // Restaurar ítem y limpiar cursor
            ItemStack original = slot.getItem().copy();
            slot.set(original);
            slot.setChanged();
            if (player instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity) player).inventory.setCarried(ItemStack.EMPTY);
            }
            this.broadcastChanges();
            return ItemStack.EMPTY;
        }
        return super.clicked(slotId, dragType, clickTypeIn, player);
    }

    private boolean isMenuItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        CompoundNBT tag = stack.getTag();
        return tag != null && tag.contains("MenuButtonID") && tag.contains("MenuSlot");
    }
}
