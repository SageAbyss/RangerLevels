package rl.sage.rangerlevels.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SSetSlotPacket;

/**
 * Clase base para menús de 3 filas (27 slots) que:
 *   1) Bloquea cualquier movimiento de ítems en menú o en inventario del jugador.
 *   2) Permite solo “clic izquierdo normal” sobre un botón válido
 *      (ItemStack con tags NBT MenuButtonID + MenuSlot).
 *   3) En ese único caso invoca handleButtonClick(...) y restaura el ítem en su slot.
 *   4) En cualquier otro clic (shift+click, hotbar-swap, drop, pick-all, etc.),
 *      restaura explícita y simultáneamente:
 *        – TODOS los slots del menú,
 *        – TODOS los slots del inventario del jugador,
 *        – limpia el cursor,
 *        – reenvía broadcastChanges() tanto del menú como del inventario.
 *
 * Añade impresiones de depuración para:
 *   1) Ver cualquier clic que llega al servidor.
 *   2) Detectar cuándo es un “clic válido” (izquierdo normal sobre botón con NBT).
 */
public abstract class BaseMenuContainer extends ChestContainer {

    private final Inventory menuInv;
    private final int menuSize;

    public BaseMenuContainer(int windowId,
                             net.minecraft.entity.player.PlayerInventory playerInv,
                             Inventory menuInventory) {
        super(ContainerType.GENERIC_9x3, windowId, playerInv, menuInventory, 3);
        this.menuInv = menuInventory;
        this.menuSize = menuInv.getContainerSize();
        this.menuInv.startOpen(playerInv.player);

        // 1) Reemplazamos los primeros menuSize slots por MenuSlot
        for (int i = 0; i < menuSize; i++) {
            Slot old = this.slots.get(i);
            MenuSlot newSlot = new MenuSlot(menuInv, i, old.x, old.y);
            newSlot.set(old.getItem().copy());
            this.slots.set(i, newSlot);
        }
    }

    // ----------------------- SHIFT-CLICK -----------------------

    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int index) {
        if (!(player instanceof ServerPlayerEntity)) {
            return ItemStack.EMPTY;
        }
        ServerPlayerEntity sp = (ServerPlayerEntity) player;

        // 1) Restaurar TODOS los slots del menú
        restoreAllMenuSlots(sp);
        // 2) Restaurar TODOS los slots del inventario del jugador
        restoreAllPlayerInventorySlots(sp);
        // 3) Forzar broadcast completo y limpiar cursor
        this.broadcastChanges();
        sp.inventoryMenu.broadcastChanges();
        clearPlayerCursor(sp);

        return ItemStack.EMPTY;
    }

    // ------------- BLOQUEO DE “PICK‐ALL” (Ctrl+Click) -------------

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        if (slot != null && (slot.container == this.menuInv || isPlayerInvSlot(slot.index))) {
            return false;
        }
        return super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public boolean canDragTo(Slot slotIn) {
        return false; // Prohíbe arrastrar en cualquier slot mientras el menú esté abierto
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        return false; // Prohíbe cualquier shift+drag
    }

    /**
     * Captura **todos** los CLICS (LEFT, RIGHT, SHIFT, SWAP, DROP, CLONE, etc.).
     * 1) Si clic en slot de menú:
     *    a) Si (ClickType.PICKUP, dragType == 0) y es botón válido:
     *         – Depura cualquier clic recibido
     *         – Depura “clic válido”
     *         – clear cursor
     *         – handleButtonClick(...)
     *         – Si seguimos en el mismo contenedor, restaurar sitio y broadcast.
     *    b) Cualquier otro clic en MENÚ:
     *         – Si seguimos en el mismo contenedor, restaurar
     *           TODOS los slots del menú + inventario + broadcast.
     * 2) Si clic en slot de inventario del jugador:
     *         – Si seguimos en el mismo contenedor, restaurar
     *           TODOS los slots del menú + inventario + broadcast.
     * 3) Si slotId fuera de rango (<0 o ≥ slots.size()), delegamos a super.clicked().
     */
    @Override
    public ItemStack clicked(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        int totalSlots = this.slots.size();

        // 0) Slot fuera de rango: delegar a super
        if (slotId < 0 || slotId >= totalSlots) {
            return super.clicked(slotId, dragType, clickTypeIn, player);
        }

        // 1) CLIC SOBRE SLOT DE MENÚ
        if (isMenuSlot(slotId)) {
            Slot slot = this.slots.get(slotId);
            ItemStack original = slot.getItem();

            // 1.a) Clic izquierdo normal (ClickType.PICKUP, dragType==0) sobre un botón válido
            if (clickTypeIn == ClickType.PICKUP && dragType == 0 && isMenuItem(original)) {
                if (player instanceof ServerPlayerEntity) {
                    ServerPlayerEntity sp = (ServerPlayerEntity) player;

                    // – Limpiar cursor
                    clearPlayerCursor(sp);

                    // – Ejecutar acción específica
                    String buttonId = original.getTag().getString("MenuButtonID");
                    handleButtonClick(buttonId, sp);

                    // – Si, tras handleButtonClick, seguimos en este mismo contenedor,
                    //    restauramos el slot y hacemos broadcast.
                    if (sp.containerMenu == this) {
                        slot.set(original.copy());
                        slot.setChanged();
                        this.broadcastChanges();
                        sp.connection.send(new SSetSlotPacket(this.containerId, slotId, original.copy()));
                    }
                } else {
                    // (modo singleplayer teórico)
                    slot.set(original.copy());
                    slot.setChanged();
                    this.broadcastChanges();
                }
                return ItemStack.EMPTY;
            }

            // 1.b) Cualquier otro clic en el MENÚ (shift-click, swap 1–9, drop, etc.)
            if (player instanceof ServerPlayerEntity) {
                ServerPlayerEntity sp = (ServerPlayerEntity) player;

                // Si seguimos en el mismo contenedor, restauramos TODO y broadcast
                if (sp.containerMenu == this) {
                    restoreAllMenuSlots(sp);
                    restoreAllPlayerInventorySlots(sp);
                    clearPlayerCursor(sp);
                    this.broadcastChanges();
                    sp.inventoryMenu.broadcastChanges();
                }
            }
            return ItemStack.EMPTY;
        }

        // 2) CLIC EN SLOT DE INVENTARIO DEL JUGADOR
        if (isPlayerInvSlot(slotId)) {
            if (player instanceof ServerPlayerEntity) {
                ServerPlayerEntity sp = (ServerPlayerEntity) player;

                // Si seguimos siendo este contenedor, restaurar TODO + broadcast
                if (sp.containerMenu == this) {
                    restoreAllMenuSlots(sp);
                    restoreAllPlayerInventorySlots(sp);
                    clearPlayerCursor(sp);
                    this.broadcastChanges();
                    sp.inventoryMenu.broadcastChanges();
                }
            }
            return ItemStack.EMPTY;
        }

        // 3) Slot fuera de nuestro rango: delegar a super.clicked()
        return super.clicked(slotId, dragType, clickTypeIn, player);
    }

    // -------------------- MÉTODOS AUXILIARES --------------------

    private void restoreAllMenuSlots(ServerPlayerEntity sp) {
        for (int i = 0; i < menuSize; i++) {
            Slot slot = this.slots.get(i);
            ItemStack original = slot.getItem().copy();
            sp.connection.send(new SSetSlotPacket(this.containerId, i, original));
        }
    }

    private void restoreAllPlayerInventorySlots(ServerPlayerEntity sp) {
        int totalSlots = this.slots.size();
        for (int i = menuSize; i < totalSlots; i++) {
            ItemStack toSend = getServerSideInventoryStack(sp, i);
            sp.connection.send(new SSetSlotPacket(this.containerId, i, toSend));
        }
    }

    private ItemStack getServerSideInventoryStack(ServerPlayerEntity sp, int containerIndex) {
        // índices [menuSize..menuSize+26] → main inventory  (→ invIndex = (containerIndex - menuSize) + 9)
        if (containerIndex >= menuSize && containerIndex < menuSize + 27) {
            int invIndex = (containerIndex - menuSize) + 9;
            return sp.inventory.getItem(invIndex).copy();
        }
        // índices [menuSize+27..menuSize+35] → hotbar (→ hotbarIndex = containerIndex - (menuSize+27))
        if (containerIndex >= menuSize + 27 && containerIndex < menuSize + 27 + 9) {
            int hotbarIndex = containerIndex - (menuSize + 27);
            return sp.inventory.getItem(hotbarIndex).copy();
        }
        return ItemStack.EMPTY;
    }

    private void clearPlayerCursor(ServerPlayerEntity player) {
        player.inventory.setCarried(ItemStack.EMPTY);
        player.connection.send(new SSetSlotPacket(-1, 0, ItemStack.EMPTY));
    }

    protected boolean isMenuSlot(int slotId) {
        return slotId >= 0 && slotId < menuSize;
    }

    protected boolean isPlayerInvSlot(int slotId) {
        return slotId >= menuSize && slotId < this.slots.size();
    }

    protected boolean isMenuItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (!stack.hasTag()) return false;
        return stack.getTag().contains("MenuButtonID")
                && stack.getTag().contains("MenuSlot");
    }

    /**
     * Cada subclase (MainMenuContainer, HelpMenuContainer, BuyPassMenuContainer, etc.)
     * IMPLEMENTA este método para definir la acción de cada botón. Si esa acción
     * cierra el contenedor actual (p.ej. player.closeContainer()), la comprobación
     * “sp.containerMenu == this” evitará que inmediatamente después se haga el
     * “restoreAllMenuSlots” sobre el contenedor viejo que acabamos de cerrar.
     */
    protected abstract void handleButtonClick(String buttonId, ServerPlayerEntity player);

    public Inventory getMenuInv() {
        return this.menuInv;
    }
}
