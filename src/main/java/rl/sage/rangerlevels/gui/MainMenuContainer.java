package rl.sage.rangerlevels.gui;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.pass.PassManager;

public class MainMenuContainer extends ChestContainer {
    private static final ITextComponent TITLE = new StringTextComponent("Menú Principal");
    private final Inventory menuInv;

    public MainMenuContainer(int windowId, PlayerInventory playerInv, Inventory menuInventory) {
        super(ContainerType.GENERIC_9x3, windowId, playerInv, menuInventory, 3);
        this.menuInv = menuInventory;
        this.menuInv.startOpen(playerInv.player);

        // Reemplaza los primeros slots por MenuSlot
        for (int i = 0; i < menuInv.getContainerSize(); i++) {
            Slot old = this.slots.get(i);
            MenuSlot ms = new MenuSlot(menuInv, i, old.x, old.y);
            ms.set(old.getItem());
            this.slots.set(i, ms);
        }
    }

    /** Abre este menú usando el provider directamente */
    public static void open(ServerPlayerEntity player) {
        player.openMenu(new MainMenuProvider());
    }

    @Override
    public boolean stillValid(PlayerEntity playerIn) {
        return true;  // Nunca se cierra por distancia
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int index) {
        // Si no es un slot de nuestro menú, delegamos
        if (index < 0 || index >= menuInv.getContainerSize()) {
            return super.quickMoveStack(player, index);
        }
        // Bloqueamos shift-click sobre nuestro menú
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        if (slot.container == this.menuInv) {
            // Bloquea la Q sobre nuestro menú
            slot.set(slot.getItem());
            return false;
        }
        return super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public boolean canDragTo(Slot slotIn) {
        // Bloquea arrastre de stacks
        return false;
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        // Bloquea shift+drag
        return false;
    }

    @Override
    public ItemStack clicked(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        // Si clic en nuestro menú y es jugador de servidor
        if (slotId >= 0 && slotId < menuInv.getContainerSize() && player instanceof ServerPlayerEntity) {
            Slot slot = this.slots.get(slotId);
            ItemStack original = slot.getItem();

            if (isMenuItem(original)) {
                String id = original.getTag().getString("MenuButtonID");
                ServerPlayerEntity sp = (ServerPlayerEntity) player;
                switch (id) {
                    case "info_button":
                        sp.sendMessage(new StringTextComponent("Aquí va tu info…"), sp.getUUID());
                        break;
                    case "rewards_menu":
                        // sp.openMenu(new RewardsProvider());
                        break;
                    case "help_menu":
                        // sp.openMenu(new HelpProvider());
                        break;
                    case "buy_pass_menu":
                        sp.openMenu(new BuyPassProvider());
                        break;
                    case "close":
                        sp.closeContainer();
                        break;
                }
            }

            // Restaurar ítem y limpiar cursor
            slot.set(original.copy());
            slot.setChanged();
            clearCursor((ServerPlayerEntity) player);
            this.broadcastChanges();
            return ItemStack.EMPTY;
        }

        // Bloquea clicks peligrosos fuera de nuestro menú
        if (clickTypeIn == ClickType.SWAP
                || clickTypeIn == ClickType.THROW
                || clickTypeIn == ClickType.QUICK_MOVE
                || clickTypeIn == ClickType.CLONE
                || clickTypeIn == ClickType.PICKUP_ALL) {
            return ItemStack.EMPTY;
        }

        // Delegar resto
        return super.clicked(slotId, dragType, clickTypeIn, player);
    }

    private void clearCursor(ServerPlayerEntity player) {
        player.connection.send(new SSetSlotPacket(-1, 0, ItemStack.EMPTY));
    }

    private boolean isMenuItem(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        return tag != null
                && tag.contains("MenuButtonID")
                && tag.contains("MenuSlot");
    }
}
