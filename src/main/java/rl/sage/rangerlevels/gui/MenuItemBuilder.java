package rl.sage.rangerlevels.gui;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;

public class MenuItemBuilder {

    /**
     * Construye un ItemStack con nombre, lore y un tag identificador.
     *
     * @param name     Texto a mostrar como nombre del ítem.
     * @param lore     Líneas de lore (cada String será coloreada en gris).
     * @param material Tipo de ítem (Items.CHEST, Items.BOOK, etc.).
     * @param idTag    Identificador custom para detectar clics.
     */
    public static ItemStack createButton(String name, List<String> lore, Item material, String idTag, int slotIndex) {
        ItemStack item = new ItemStack(material);

        CompoundNBT display = new CompoundNBT();
        ITextComponent nameComponent = new StringTextComponent(name);
        display.putString("Name", ITextComponent.Serializer.toJson(nameComponent));

        if (lore != null && !lore.isEmpty()) {
            ListNBT loreList = new ListNBT();
            for (String line : lore) {
                ITextComponent lineComp = new StringTextComponent(line);
                String json = ITextComponent.Serializer.toJson(lineComp);
                loreList.add(StringNBT.valueOf(json));
            }
            display.put("Lore", loreList);
        }

        CompoundNBT tag = item.getOrCreateTag();
        tag.put("display", display);
        tag.putString("MenuButtonID", idTag);
        tag.putInt("MenuSlot", slotIndex); // 👈 Aquí agregamos la posición

        item.setTag(tag);
        return item;
    }

}
