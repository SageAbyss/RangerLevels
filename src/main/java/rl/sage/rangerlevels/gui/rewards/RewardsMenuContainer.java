package rl.sage.rangerlevels.gui.rewards;

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
import rl.sage.rangerlevels.gui.MenuSlot;

public class RewardsMenuContainer extends ChestContainer {
    private final Inventory menuInv;

    public RewardsMenuContainer(int windowId, PlayerInventory inv, Inventory menuInv) {
        super(ContainerType.GENERIC_9x3, windowId, inv, menuInv, 3);
        this.menuInv = menuInv;
        menuInv.startOpen(inv.player);
        for (int i = 0; i < menuInv.getContainerSize(); i++) {
            Slot old = this.slots.get(i);
            MenuSlot ms = new MenuSlot(menuInv, i, old.x, old.y);
            ms.set(old.getItem());
            this.slots.set(i, ms);
        }
    }

    // Clic normal / derecho / pickup
    @Override
    public ItemStack clicked(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        // Solo interceptamos los slots de nuestro menú
        if (slotId >= 0 && slotId < menuInv.getContainerSize()) {
            Slot slot = this.slots.get(slotId);
            ItemStack original = slot.getItem();

            // Si es un ServerPlayerEntity, vaciamos cursor y abrimos submenú si toca
            if (player instanceof ServerPlayerEntity) {
                ServerPlayerEntity sp = (ServerPlayerEntity) player;
                String id = original.hasTag() ? original.getTag().getString("MenuButtonID") : "";

                // Limpia el cursor en cliente
                sp.connection.send(new SSetSlotPacket(-1, 0, ItemStack.EMPTY));
                sp.inventory.setCarried(ItemStack.EMPTY);

                // Acción según ID
                if ("info".equals(id)) {
                    rl.sage.rangerlevels.gui.PlayerInfoUtils.getInfoItem(sp, slotId);
                } else if ("reward_level".equals(id)) {
                } else if ("reward_package".equals(id)) {
                } else if ("reward_exact".equals(id)) {
                }

                // Restaurar el ítem en el servidor
                slot.set(original.copy());
                slot.setChanged();
                this.broadcastChanges();

                // Enviar paquete para actualizar el slot **ahora mismo** en el cliente
                sp.connection.send(new SSetSlotPacket(this.containerId, slotId, original.copy()));

                // Devolver la copia para que Forge intente ponerla en cursor,
                // pero como ya lo vaciamos, el jugador no la retendrá.
                return original.copy();
            }

            // Si no es jugador de servidor, igual restauramos y devolvemos EMPTY
            slot.set(original.copy());
            slot.setChanged();
            this.broadcastChanges();
            return ItemStack.EMPTY;
        }

        // Para slots fuera de nuestro menú, comportamiento por defecto
        return super.clicked(slotId, dragType, clickTypeIn, player);
    }



    // Shift‑click
    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int index) {
        if (index >= 0 && index < menuInv.getContainerSize()) {
            Slot slot = this.slots.get(index);
            ItemStack original = slot.getItem();
            slot.set(original.copy());
            slot.setChanged();
            broadcastChanges();
            return ItemStack.EMPTY;
        }
        return ItemStack.EMPTY;
    }

    // Tecla Q (pick‑all)
    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        if (slot.container == menuInv && isMenuItem(slot.getItem())) {
            ItemStack original = slot.getItem();
            slot.set(original.copy());
            slot.setChanged();
            broadcastChanges();
            return false;
        }
        return super.canTakeItemForPickAll(stack, slot);
    }

    // Arrastre normal
    @Override
    public boolean canDragTo(Slot slot) {
        if (slot.container == menuInv && isMenuItem(slot.getItem())) {
            ItemStack original = slot.getItem();
            slot.set(original.copy());
            slot.setChanged();
            broadcastChanges();
        }
        return false;
    }

    // Shift + drag
    @Override
    protected boolean moveItemStackTo(ItemStack stack, int start, int end, boolean rev) {
        return false;
    }

    private boolean isMenuItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        CompoundNBT tag = stack.getTag();
        return tag != null
                && tag.contains("MenuButtonID")
                && tag.contains("MenuSlot");
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        return true;
    }
}
