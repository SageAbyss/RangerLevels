// src/main/java/rl/sage/rangerlevels/gui/rewards/PackagesLevelMenuContainer.java
package rl.sage.rangerlevels.gui.rewards;

import javax.annotation.Nullable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SSetSlotPacket;
import rl.sage.rangerlevels.gui.MenuSlot;

public class PackagesLevelMenuContainer extends ChestContainer {
    private final Inventory menuInv;

    public PackagesLevelMenuContainer(int windowId, PlayerInventory inv, Inventory menuInv) {
        super(ContainerType.GENERIC_9x6, windowId, inv, menuInv, 6);
        this.menuInv = menuInv;
        menuInv.startOpen(inv.player);

        // 1) Proteger cada slot con MenuSlot
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
                String id = "";
                if (original.hasTag()) {
                    id = original.getTag().getString("MenuButtonID");
                }

                // limpiar cursor en cliente
                sp.connection.send(new SSetSlotPacket(-1, 0, ItemStack.EMPTY));
                sp.inventory.setCarried(ItemStack.EMPTY);

                // paginación
                if (id.startsWith("page:")) {
                    try {
                        int nuevaPagina = Integer.parseInt(id.split(":")[1]);
                        PackagesLevelMenu.open(sp, nuevaPagina);
                    } catch (NumberFormatException ignore) {}
                    return original.copy();
                }

                // acciones fijas
                if ("info".equals(id)) {
                    // nada
                } else if ("back".equals(id)) {
                    sp.closeContainer();
                    RewardsMenu.open(sp);
                } else if ("claim_all".equals(id)) {
                    PackagesLevelMenu.claimAll(sp);
                    PackagesLevelMenu.open(sp, 1);
                }
                // reclamar un solo paquete
                else if (id.startsWith("Packages.")) {
                    // id = "Packages.<iv>.<nivel>.<ruta>.<page>"
                    String[] parts = id.split("\\.");
                    if (parts.length == 5) {
                        String iv     = parts[1];
                        String nivel  = parts[2];
                        String ruta   = parts[3];
                        int pagina;
                        try {
                            pagina = Integer.parseInt(parts[4]);
                        } catch (NumberFormatException e) {
                            pagina = 1;
                        }
                        PackagesLevelMenu.claimSingle(sp, iv, nivel, ruta);
                        PackagesLevelMenu.open(sp, pagina);
                    }
                }

                // restaurar slot en servidor y sincronizar
                slot.set(original.copy());
                slot.setChanged();
                this.broadcastChanges();
                sp.connection.send(new SSetSlotPacket(this.containerId, slotId, original.copy()));
                return original.copy();
            }

            // cliente o no-ServerPlayer: restaurar y devolver vacío
            slot.set(original.copy());
            slot.setChanged();
            this.broadcastChanges();
            return ItemStack.EMPTY;
        }

        return super.clicked(slotId, dragType, clickTypeIn, player);
    }

    // Bloquear cualquier otra forma de mover ítems
    @Override public ItemStack quickMoveStack(PlayerEntity player, int index) { return ItemStack.EMPTY; }
    @Override public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) { return false; }
    @Override public boolean canDragTo(Slot slot) { return false; }
    @Override protected boolean moveItemStackTo(ItemStack stack, int start, int end, boolean rev) { return false; }
    @Override public boolean stillValid(PlayerEntity player) { return true; }
}
