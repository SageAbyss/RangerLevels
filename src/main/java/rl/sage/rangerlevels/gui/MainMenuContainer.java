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
import rl.sage.rangerlevels.gui.help.HelpMenu;
import rl.sage.rangerlevels.gui.pass.BuyPassMenu;
import rl.sage.rangerlevels.gui.rewards.RewardsMenu;


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
                switch (id) {
                    // tu lógica de información
                    case "info":
                        break;
                    // tu lógica de información

                    case "help":  // si quieres que “info” o “help” abran la ayuda
                        // Aquí llamas a tu nuevo menú de ayuda
                        //rl.sage.rangerlevels.gui.HelpButtonUtils.sendHelpMenu(sp);
                        sp.closeContainer();
                        HelpMenu.open(sp);

                        break;
                    case "buy":
                        sp.closeContainer();
                        BuyPassMenu.open(sp);
                        break;

                    case "rewards":
                        sp.closeContainer();
                        RewardsMenu.open(sp);
                        break;
                    // ...otros botones
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
