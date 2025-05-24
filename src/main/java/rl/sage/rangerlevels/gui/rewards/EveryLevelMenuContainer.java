// src/main/java/rl/sage/rangerlevels/gui/rewards/EveryLevelMenuContainer.java
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
import rl.sage.rangerlevels.gui.rewards.EveryLevelMenu;

public class EveryLevelMenuContainer extends ChestContainer {
    private final Inventory menuInv;

    public EveryLevelMenuContainer(int windowId, PlayerInventory inv, Inventory menuInv) {
        super(ContainerType.GENERIC_9x6, windowId, inv, menuInv, 6);
        this.menuInv = menuInv;
        menuInv.startOpen(inv.player);
        for (int i = 0; i < menuInv.getContainerSize(); i++) {
            Slot old = this.slots.get(i);
            MenuSlot ms = new MenuSlot(menuInv, i, old.x, old.y);
            ms.set(old.getItem());
            this.slots.set(i, ms);
        }
    }

    @Override
    public ItemStack clicked(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        if (slotId >= 0 && slotId < menuInv.getContainerSize()) {
            Slot slot = this.slots.get(slotId);
            ItemStack original = slot.getItem();
            if (player instanceof ServerPlayerEntity) {
                ServerPlayerEntity sp = (ServerPlayerEntity) player;
                String id = original.hasTag() ? original.getTag().getString("MenuButtonID") : "";

                // limpiar cursor
                sp.connection.send(new SSetSlotPacket(-1, 0, ItemStack.EMPTY));
                sp.inventory.setCarried(ItemStack.EMPTY);

// —————— PAGINACIÓN DINÁMICA ——————
                if (id.startsWith("page:")) {
                    try {
                        int nuevaPagina = Integer.parseInt(id.split(":")[1]);
                        EveryLevelMenu.open(sp, nuevaPagina);
                    } catch (NumberFormatException ignored) {}
                    return original.copy();
                }

                switch (id) {
                    case "info":
                        // No acción adicional
                        break;
                    case "back":
                        sp.closeContainer();
                        RewardsMenu.open(sp);
                        break;
                    case "claim_all":
                        // Cuando reclamas todas, volvemos a la primera página (o puedes calcular la última si prefieres)
                        EveryLevelMenu.claimAll(sp);
                        EveryLevelMenu.open(sp, 1);
                        break;
                    default:
                        if (id.startsWith("EveryLevel.")) {
                            // id = "EveryLevel.<nivel>.<ruta>.<page>"
                            String[] parts = id.split("\\.");
                            if (parts.length == 4) {
                                String nivel = parts[1];
                                String ruta  = parts[2];
                                int pagina   = Integer.parseInt(parts[3]);

                                EveryLevelMenu.claimSingle(sp, nivel, ruta);
                                // Reabrimos en la misma página donde estaba la recompensa
                                EveryLevelMenu.open(sp, pagina);
                            }
                        }
                        break;
                }

                // restaurar slot
                slot.set(original.copy());
                slot.setChanged();
                this.broadcastChanges();
                sp.connection.send(new SSetSlotPacket(this.containerId, slotId, original.copy()));
                return original.copy();
            }
            slot.set(original.copy());
            slot.setChanged();
            this.broadcastChanges();
            return ItemStack.EMPTY;
        }
        return super.clicked(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int index) {
        // deshabilitar shift‑click
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return false;
    }

    @Override
    public boolean canDragTo(Slot slot) {
        return false;
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int start, int end, boolean rev) {
        return false;
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        return true;
    }
}
