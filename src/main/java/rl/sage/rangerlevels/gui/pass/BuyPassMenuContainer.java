// src/main/java/rl/sage/rangerlevels/gui/BuyPassMenuContainer.java
package rl.sage.rangerlevels.gui.pass;

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
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import rl.sage.rangerlevels.config.ExpConfig;
import rl.sage.rangerlevels.gui.MainMenu;
import rl.sage.rangerlevels.gui.MenuSlot;
import rl.sage.rangerlevels.pass.PassManager;
import rl.sage.rangerlevels.pass.PassType;

public class BuyPassMenuContainer extends ChestContainer {
    private final Inventory menuInv;

    public BuyPassMenuContainer(int windowId, PlayerInventory playerInv, Inventory menuInventory) {
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
            slot.set(slot.getItem());
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

                // Limpia cursor en cliente
                sp.connection.send(new SSetSlotPacket(-1, 0, ItemStack.EMPTY));
                sp.inventory.setCarried(ItemStack.EMPTY);

                // Lógica según botón pulsado
                switch (id) {
                    case "buy_super": {
                        String url = ExpConfig.get().getPassBuyUrls().getOrDefault("super", "");
                        IFormattableTextComponent msg = new StringTextComponent("§eEnlace SuperPass: ")
                                // parte clicable
                                .append(new StringTextComponent(url)
                                        .withStyle(Style.EMPTY
                                                .withColor(TextFormatting.GREEN)
                                                .withClickEvent(new ClickEvent(
                                                        ClickEvent.Action.OPEN_URL,
                                                        url
                                                ))
                                                .withHoverEvent(new HoverEvent(
                                                        HoverEvent.Action.SHOW_TEXT,
                                                        new StringTextComponent("§6Clic para abrir enlace")
                                                ))
                                        )
                                );
                        sp.sendMessage(msg, sp.getUUID());
                        sp.closeContainer();
                        break;
                    }
                    case "buy_ultra": {
                        String url = ExpConfig.get().getPassBuyUrls().getOrDefault("ultra", "");
                        IFormattableTextComponent msg = new StringTextComponent("§eEnlace UltraPass: ")
                                .append(new StringTextComponent(url)
                                        .withStyle(Style.EMPTY
                                                .withColor(TextFormatting.AQUA)
                                                .withClickEvent(new ClickEvent(
                                                        ClickEvent.Action.OPEN_URL,
                                                        url
                                                ))
                                                .withHoverEvent(new HoverEvent(
                                                        HoverEvent.Action.SHOW_TEXT,
                                                        new StringTextComponent("§6Clic para abrir enlace")
                                                ))
                                        )
                                );
                        sp.sendMessage(msg, sp.getUUID());
                        sp.closeContainer();
                        break;
                    }
                    case "buy_master": {
                        String url = ExpConfig.get().getPassBuyUrls().getOrDefault("master", "");
                        IFormattableTextComponent msg = new StringTextComponent("§eEnlace MasterPass: ")
                                .append(new StringTextComponent(url)
                                        .withStyle(Style.EMPTY
                                                .withColor(TextFormatting.GOLD)
                                                .withClickEvent(new ClickEvent(
                                                        ClickEvent.Action.OPEN_URL,
                                                        url
                                                ))
                                                .withHoverEvent(new HoverEvent(
                                                        HoverEvent.Action.SHOW_TEXT,
                                                        new StringTextComponent("§6Clic para abrir enlace")
                                                ))
                                        )
                                );
                        sp.sendMessage(msg, sp.getUUID());
                        sp.closeContainer();
                        break;
                    }
                    case "info":
                        // la cabeza ya muestra datos en lore
                        break;
                    case "back":
                        sp.closeContainer();
                        MainMenu.open(sp);
                        break;
                    default:
                        break;
                }

                // Restaurar el ítem en el slot
                slot.set(original.copy());
                slot.setChanged();
                this.broadcastChanges();
                sp.connection.send(new SSetSlotPacket(this.containerId, slotId, original.copy()));
                return original.copy();
            }

            // Cliente o no‐ServerPlayerEntity
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
