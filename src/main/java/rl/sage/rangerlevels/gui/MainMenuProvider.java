package rl.sage.rangerlevels.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

/**
 * Provider para abrir el menú principal,
 * con lógica de creación y llenado de ítems.
 */
public class MainMenuProvider implements INamedContainerProvider {
    private static final ITextComponent TITLE = new StringTextComponent("Menú Principal");

    @Override
    public ITextComponent getDisplayName() {
        return TITLE;
    }

    @Override
    public Container createMenu(int windowId, PlayerInventory playerInv, PlayerEntity player) {
        Inventory menuInv = new Inventory(3 * 9);
        initMenuItems(menuInv);
        return new MainMenuContainer(windowId, playerInv, menuInv);
    }

    private static void initMenuItems(Inventory menuInv) {
        // 1) Limpiar
        for (int i = 0; i < menuInv.getContainerSize(); i++) {
            menuInv.setItem(i, ItemStack.EMPTY);
        }

        // 2) Botones
        placeButton(menuInv, 10, "info_button", Items.PLAYER_HEAD, "🧑‍🏫 Información");
        placeButton(menuInv, 12, "rewards_menu", Items.CHEST,       "🎁 Recompensas");
        placeButton(menuInv, 14, "help_menu",    Items.BOOK,        "❓ Ayuda");
        placeButton(menuInv, 16, "buy_pass_menu",Items.EMERALD,     "💸 Comprar Pase");
        placeButton(menuInv, 26, "close",        Items.BARRIER,     "❌ Cerrar");
    }

    private static void placeButton(Inventory inv, int slot, String id, net.minecraft.item.Item icon, String label) {
        ItemStack btn = new ItemStack(icon);
        CompoundNBT tag = new CompoundNBT();
        tag.putString("MenuButtonID", id);
        tag.putInt("MenuSlot", slot);
        btn.setTag(tag);
        btn.setHoverName(new StringTextComponent(label));
        inv.setItem(slot, btn);
    }
}
