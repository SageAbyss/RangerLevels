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
import rl.sage.rangerlevels.pass.PassManager;

/**
 * Provider para abrir el menú de compra de pases,
 * con lógica de creación y llenado de los ítems.
 */
public class BuyPassProvider implements INamedContainerProvider {
    private static final ITextComponent TITLE = new StringTextComponent("Comprar Pase");

    @Override
    public ITextComponent getDisplayName() {
        return TITLE;
    }

    @Override
    public Container createMenu(int windowId, PlayerInventory playerInv, PlayerEntity player) {
        Inventory menuInv = new Inventory(3 * 9);
        initMenuItems(menuInv, (ServerPlayerEntity) player);
        return new BuyPassContainer(windowId, playerInv, menuInv);
    }

    private static void initMenuItems(Inventory menuInv, ServerPlayerEntity player) {
        // 1) Limpia todos los slots
        for (int i = 0; i < menuInv.getContainerSize(); i++) {
            menuInv.setItem(i, ItemStack.EMPTY);
        }

        // 2) Botón "Tu pase actual" (slot 4)
        ItemStack currentBtn = new ItemStack(Items.PLAYER_HEAD);
        CompoundNBT currentTag = new CompoundNBT();
        currentTag.putString("MenuButtonID", "current");
        currentTag.putInt("MenuSlot", 4);
        currentBtn.setTag(currentTag);
        currentBtn.setHoverName(new StringTextComponent("Tu Pase Actual"));
        menuInv.setItem(4, currentBtn);

        // 3) Botones de compra: SUPER (11), ULTRA (13), MASTER (15)
        placePassButton(menuInv, 11, "super", Items.NETHER_STAR, player);
        placePassButton(menuInv, 13, "ultra", Items.END_CRYSTAL, player);
        placePassButton(menuInv, 15, "master", Items.DRAGON_EGG, player);
    }

    private static void placePassButton(Inventory menuInv,
                                        int slot,
                                        String id,
                                        net.minecraft.item.Item icon,
                                        ServerPlayerEntity player) {
        ItemStack btn = new ItemStack(icon);
        CompoundNBT tag = new CompoundNBT();
        tag.putString("MenuButtonID", id);
        tag.putInt("MenuSlot", slot);
        btn.setTag(tag);

        // Nombre y lore dinámico desde PassManager
        PassManager.PassType type = PassManager.PassType.valueOf(id.toUpperCase());
        ITextComponent name = type.getGradientDisplayName();
        btn.setHoverName(name);
        // (opcional) enchant para glow
        btn.enchant(net.minecraft.enchantment.Enchantments.UNBREAKING, 1);

        menuInv.setItem(slot, btn);
    }
}
