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

public class BuyPassContainer extends ChestContainer {
    private static final ITextComponent TITLE = new StringTextComponent("Comprar Pase");
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

    /** Abre este menú desde cualquier sitio del servidor */
    public static void open(ServerPlayerEntity player) {
        player.openMenu(new SimpleNamedContainerProvider(
                (windowId, inv, plyr) -> {
                    Inventory menuInv = new Inventory(3 * 9);
                    // initMenuItems(menuInv, player); // si tienes lógica de llenado
                    return new BuyPassContainer(windowId, inv, menuInv);
                },
                TITLE
        ));
    }

    @Override
    public boolean stillValid(PlayerEntity playerIn) {
        return true;  // Nunca se cierra por distancia
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
        if (slotId >= 0 && slotId < menuInv.getContainerSize() && player instanceof ServerPlayerEntity) {
            Slot slot = this.slots.get(slotId);
            ItemStack stack = slot.getItem();
            if (isMenuItem(stack)) {
                ServerPlayerEntity sp = (ServerPlayerEntity) player;
                String id = stack.getTag().getString("MenuButtonID");
                switch (id) {
                    case "super":  sendInfo(sp, PassManager.PassType.SUPER); break;
                    case "ultra":  sendInfo(sp, PassManager.PassType.ULTRA); break;
                    case "master": sendInfo(sp, PassManager.PassType.MASTER); break;
                    case "current":
                        PassManager.PassType curr = PassManager.getPass(sp);
                        sp.sendMessage(curr.getGradientDisplayName()
                                        .append(new StringTextComponent("\nNivel: " + curr.getTier())),
                                sp.getUUID());
                        break;
                }
            }
            // Restaurar ítem y limpiar cursor
            ItemStack original = slot.getItem().copy();
            slot.set(original);
            slot.setChanged();
            ((ServerPlayerEntity)player).inventory.setCarried(ItemStack.EMPTY);
            this.broadcastChanges();
            return ItemStack.EMPTY;
        }
        return super.clicked(slotId, dragType, clickTypeIn, player);
    }

    private static void sendInfo(ServerPlayerEntity sp, PassManager.PassType type) {
        sp.sendMessage(
                type.getGradientDisplayName()
                        .append(new StringTextComponent("\nCompra: " + type.getPurchaseUrl())),
                sp.getUUID()
        );
    }

    private boolean isMenuItem(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        return tag != null && tag.contains("MenuButtonID") && tag.contains("MenuSlot");
    }
}
