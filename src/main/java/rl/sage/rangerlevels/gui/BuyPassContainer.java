package rl.sage.rangerlevels.gui;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.ITextComponent.Serializer;
import rl.sage.rangerlevels.pass.PassManager;
import rl.sage.rangerlevels.pass.PassManager.PassType;

import java.util.ArrayList;
import java.util.List;

public class BuyPassContainer extends ChestContainer {
    private final Inventory menuInv;
    private final ServerPlayerEntity owner;  // ← guardamos el jugador
    private void clearPlayerCursor(ServerPlayerEntity player) {
        player.connection.send(new SSetSlotPacket(-1, 0, ItemStack.EMPTY));
    }


    public BuyPassContainer(int windowId, PlayerInventory playerInv, Inventory menuInventory) {
        super(ContainerType.GENERIC_9x3, windowId, playerInv, menuInventory, 3);
        this.menuInv = menuInventory;
        this.menuInv.startOpen(playerInv.player);
        this.owner = (ServerPlayerEntity) playerInv.player;  // ← inicializamos

        // Reemplaza todos los slots por MenuSlot personalizados
        for (int i = 0; i < menuInv.getContainerSize(); i++) {
            Slot old = this.slots.get(i);
            MenuSlot ms = new MenuSlot(menuInv, i, old.x, old.y);
            ms.set(old.getItem());
            this.slots.set(i, ms);
        }

        // Inicializa el contenido visual del menú
        initMenuItems();
    }

    @Override
    public boolean stillValid(PlayerEntity playerIn) {
        return true;
    }

    // 1) Bloquear shift-click (quick-move)
    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int index) {
        if (index < menuInv.getContainerSize()) {
            return ItemStack.EMPTY;
        }
        return super.quickMoveStack(player, index);
    }

    // 2) Bloquear pick-all (Q)
    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != this.menuInv && super.canTakeItemForPickAll(stack, slot);
    }

    // 3) Bloquear “drag” entre slots
    @Override
    public boolean canDragTo(Slot slot) {
        return slot.container != this.menuInv && super.canDragTo(slot);
    }

    // 4) Bloquear moveItemStackTo (shift+drag)
    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        // si la zona de destino está dentro de nuestro menú, denegamos
        if (startIndex < menuInv.getContainerSize() || endIndex <= menuInv.getContainerSize()) {
            return false;
        }
        return super.moveItemStackTo(stack, startIndex, endIndex, reverseDirection);
    }

    @Override
    public ItemStack clicked(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        if (slotId >= 0 && slotId < menuInv.getContainerSize()) {
            Slot slot = this.slots.get(slotId);
            ItemStack stack = slot.getItem();
            if (isMenuItem(stack) && player instanceof ServerPlayerEntity) {
                ServerPlayerEntity sp = (ServerPlayerEntity) player;
                String id = stack.getTag().getString("MenuButtonID");

                // 1) Detecta el botón "back"
                if ("back".equalsIgnoreCase(id)) {
                    handleBackClick(sp);
                    return ItemStack.EMPTY;
                }


                // 2) Botón "current"
                if ("info".equalsIgnoreCase(id)) {
                    handleCurrentClick(sp);
                } else {
                    // 3) Botones de pase (super, ultra, master)
                    try {
                        PassType type = PassType.valueOf(id.toUpperCase());
                        handlePassClick(sp, type);
                    } catch (IllegalArgumentException e) {
                        sp.sendMessage(
                                new StringTextComponent(TextFormatting.RED + "Error: pase desconocido."),
                                sp.getUUID()
                        );
                    }
                }
            }

            // Limpieza y actualización
            if (player instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity) player).inventory.setCarried(ItemStack.EMPTY);
            }
            slot.setChanged();
            this.broadcastChanges();
            return ItemStack.EMPTY;
        }
        // limpia servidor
        ((ServerPlayerEntity)player).inventory.setCarried(ItemStack.EMPTY);
        // limpia cliente
        clearPlayerCursor((ServerPlayerEntity)player);

        return super.clicked(slotId, dragType, clickTypeIn, player);
    }


    private boolean isMenuItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        CompoundNBT tag = stack.getTag();
        return tag != null && tag.contains("MenuButtonID") && tag.contains("MenuSlot");
    }

    // ------------------------------
    // Lógica de renderizado de items
    // ------------------------------

    private void initMenuItems() {
        int size = menuInv.getContainerSize();  // normalmente 27
        // 1) Limpia todo
        for (int i = 0; i < size; i++) {
            menuInv.setItem(i, ItemStack.EMPTY);
        }

        // 2) Paneles decorativos grises
        ItemStack filler = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
        CompoundNBT fTag = new CompoundNBT();
        fTag.putString("MenuSlot", "filler");
        filler.setTag(fTag);
        filler.setHoverName(new StringTextComponent(" "));
        for (int i = 0; i < size; i++) {
            menuInv.setItem(i, filler.copy());
        }

        // 3) Botones de pase en la fila central
        int[] passSlots = {11, 13, 15};
        PassType[] types = {PassType.SUPER, PassType.ULTRA, PassType.MASTER};
        for (int idx = 0; idx < types.length; idx++) {
            menuInv.setItem(passSlots[idx], createButtonStack(types[idx], passSlots[idx]));
        }

        // 4) Botón "Tu pase actual" arriba al centro (slot 4)
        menuInv.setItem(4, PlayerInfoUtils.getInfoItem(owner, 4));
        menuInv.setItem(26, createBackButton(26));
    }

    private ItemStack createButtonStack(PassType type, int slot) {
        ItemStack stack = new ItemStack(Items.NETHER_STAR);
        CompoundNBT tag = new CompoundNBT();
        tag.putString("MenuButtonID", type.name().toLowerCase());
        tag.putInt("MenuSlot", slot);
        stack.setTag(tag);

        // Nombre con gradiente
        ITextComponent name = type.getGradientDisplayName()
                .withStyle(style -> style.withColor(TextFormatting.WHITE));
        stack.setHoverName(name);

        // Lore descriptivo
        List<ITextComponent> loreComponents = new ArrayList<>();
        loreComponents.add(new StringTextComponent(TextFormatting.GRAY + type.getDescription()));
        loreComponents.add(new StringTextComponent(TextFormatting.DARK_GRAY + "Haz clic para más info"));

        // Convertir lore a NBT
        ListNBT loreList = new ListNBT();
        for (ITextComponent line : loreComponents) {
            String json = Serializer.toJson(line);
            loreList.add(StringNBT.valueOf(json));
        }
        CompoundNBT display = new CompoundNBT();
        display.put("Lore", loreList);
        stack.getOrCreateTag().put("display", display);

        // Glow con encantamiento válido
        stack.enchant(Enchantments.UNBREAKING, 1);

        return stack;
    }


    // 2) Nuevo método para crear el botón de regresar:
    private ItemStack createBackButton(int slot) {
        ItemStack stack = new ItemStack(Items.ARROW);
        CompoundNBT tag = new CompoundNBT();
        tag.putString("MenuButtonID", "back");
        tag.putInt("MenuSlot", slot);
        stack.setTag(tag);

        // Nombre y lore
        stack.setHoverName(new StringTextComponent(TextFormatting.YELLOW + "Regresar"));
        List<ITextComponent> lore = new ArrayList<>();
        lore.add(new StringTextComponent(TextFormatting.GRAY + "Volver al menú principal"));
        // Convertir lore a NBT
        ListNBT loreList = new ListNBT();
        for (ITextComponent line : lore) {
            loreList.add(StringNBT.valueOf(Serializer.toJson(line)));
        }
        CompoundNBT display = new CompoundNBT();
        display.put("Lore", loreList);
        stack.getOrCreateTag().put("display", display);

        stack.enchant(Enchantments.UNBREAKING, 1); // glow
        return stack;
    }


    // ------------------------------
    // Lógica modular de clics
    // ------------------------------

    private void handlePassClick(ServerPlayerEntity player, PassType type) {
        player.sendMessage(
                type.getGradientDisplayName()
                        .append(new StringTextComponent("\n" + TextFormatting.GRAY + "Beneficios: " + TextFormatting.WHITE + type.getDescription()))
                        .append(new StringTextComponent("\n" + TextFormatting.AQUA + "Compra: " + TextFormatting.BLUE + type.getPurchaseUrl())),
                player.getUUID()
        );
    }

    private void handleCurrentClick(ServerPlayerEntity player) {
        PassType current = PassManager.getPass(player);
        player.sendMessage(
                current.getGradientDisplayName()
                        .append(new StringTextComponent("\n" + TextFormatting.GRAY + "Tu pase actual: Tier " + TextFormatting.YELLOW + current.getTier())),
                player.getUUID()
        );
    }
    private void handleBackClick(ServerPlayerEntity player) {
        player.playSound(SoundEvents.UI_BUTTON_CLICK, 1.0F, 1.0F);
        player.closeContainer();
        BuyPassMenu.open(player);
    }

}
