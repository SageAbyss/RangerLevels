package rl.sage.rangerlevels.gui;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import rl.sage.rangerlevels.pass.PassManager;
import rl.sage.rangerlevels.pass.PassManager.PassType;
import rl.sage.rangerlevels.setup.ModContainers;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.enchantment.Enchantments.UNBREAKING;

public class BuyPassContainer extends BaseMenuContainer {

    private final ServerPlayerEntity owner;

    public BuyPassContainer(int windowId,
                            PlayerInventory playerInv,
                            Inventory menuInv) {
        super(ModContainers.BUY_PASS_MENU.get(),
                windowId,
                menuInv,
                playerInv,
                3); // 3 filas

        this.owner = (ServerPlayerEntity) playerInv.player;

        // Reemplazar los primeros slots por MenuSlot si fuera necesario:
        for (int i = 0; i < menuInv.getContainerSize(); i++) {
            Slot old = this.slots.get(i);
            MenuSlot ms = new MenuSlot(menuInv, i, old.x, old.y);
            ms.set(old.getItem());
            this.slots.set(i, ms);
        }

        initMenuItems();
    }

    @Override
    protected void handleMenuAction(String id, ServerPlayerEntity sp) {
        switch (id.toLowerCase()) {
            case "back":
                sp.playSound(SoundEvents.UI_BUTTON_CLICK, 1.0F, 1.0F);
                sp.closeContainer();
                BuyPassMenu.open(sp);
                break;

            case "info":
                PassType current = PassManager.getPass(sp);
                sp.sendMessage(
                        current.getGradientDisplayName()
                                .append(new StringTextComponent("\n" + TextFormatting.GRAY
                                        + "Tu pase actual: Tier " + TextFormatting.YELLOW
                                        + current.getTier())),
                        sp.getUUID()
                );
                break;

            default:
                try {
                    PassType type = PassType.valueOf(id.toUpperCase());
                    sp.sendMessage(
                            type.getGradientDisplayName()
                                    .append(new StringTextComponent("\n" + TextFormatting.GRAY
                                            + type.getDescription()))
                                    .append(new StringTextComponent("\n" + TextFormatting.AQUA
                                            + "Compra: " + TextFormatting.BLUE
                                            + type.getPurchaseUrl())),
                            sp.getUUID()
                    );
                } catch (IllegalArgumentException e) {
                    sp.sendMessage(
                            new StringTextComponent(TextFormatting.RED + "Error: pase desconocido."),
                            sp.getUUID()
                    );
                }
                break;
        }
    }

    private void initMenuItems() {
        int size = menuInv.getContainerSize();  // normalmente 27
        // 1) Limpia todo
        for (int i = 0; i < size; i++) {
            menuInv.setItem(i, ItemStack.EMPTY);
        }

        // 2) Paneles decorativos grises
        ItemStack filler = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
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

        // 5) Botón "Regresar" en la torre inferior
        menuInv.setItem(26, createBackButton(26));
    }

    private ItemStack createButtonStack(PassType type, int slot) {
        ItemStack stack = new ItemStack(Items.NETHER_STAR);
        CompoundNBT tag = new CompoundNBT();
        tag.putString("MenuButtonID", type.name().toLowerCase());
        tag.putInt("MenuSlot", slot);
        stack.setTag(tag);

        // Nombre con gradiente y glow
        ITextComponent name = type.getGradientDisplayName()
                .withStyle(style -> style.withColor(TextFormatting.WHITE));
        stack.setHoverName(name);
        stack.enchant(UNBREAKING, 1);

        // Lore descriptivo
        List<ITextComponent> lore = new ArrayList<>();
        lore.add(new StringTextComponent(TextFormatting.GRAY + type.getDescription()));
        lore.add(new StringTextComponent(TextFormatting.DARK_GRAY + "Haz clic para más info"));
        // Convertir lore a NBT
        ListNBT loreList = new ListNBT();
        for (ITextComponent line : lore) {
            loreList.add(StringNBT.valueOf(ITextComponent.Serializer.toJson(line)));
        }
        CompoundNBT display = new CompoundNBT();
        display.put("Lore", loreList);
        stack.getOrCreateTag().put("display", display);

        return stack;
    }

    private ItemStack createBackButton(int slot) {
        ItemStack stack = new ItemStack(Items.ARROW);
        CompoundNBT tag = new CompoundNBT();
        tag.putString("MenuButtonID", "back");
        tag.putInt("MenuSlot", slot);
        stack.setTag(tag);

        stack.setHoverName(new StringTextComponent(TextFormatting.YELLOW + "Regresar"));
        List<ITextComponent> lore = new ArrayList<>();
        lore.add(new StringTextComponent(TextFormatting.GRAY + "Volver al menú principal"));
        ListNBT loreList = new ListNBT();
        for (ITextComponent line : lore) {
            loreList.add(StringNBT.valueOf(ITextComponent.Serializer.toJson(line)));
        }
        CompoundNBT display = new CompoundNBT();
        display.put("Lore", loreList);
        stack.getOrCreateTag().put("display", display);
        stack.enchant(UNBREAKING, 1);

        return stack;
    }
}
