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
     * Construye un ItemStack con nombre, lore y un tag identificador,
     * recibiendo el nombre como String (se envolverá en StringTextComponent).
     *
     * @param name      Texto a mostrar como nombre del ítem.
     * @param lore      Líneas de lore (cada String será coloreada en gris).
     * @param material  Tipo de ítem (Items.CHEST, Items.BOOK, etc.).
     * @param idTag     Identificador custom para detectar clics.
     * @param slotIndex Índice de slot para protección de MenuItemProtector.
     */
    public static ItemStack createButton(String name, List<String> lore, Item material, String idTag, int slotIndex) {
        // Convertimos el nombre String en un TextComponent simple
        return createButton(new StringTextComponent(name), lore, material, idTag, slotIndex);
    }

    /**
     * Construye un ItemStack con nombre, lore y un tag identificador,
     * recibiendo el nombre como ITextComponent (p.ej. un gradient).
     *
     * @param nameComponent Componente de texto ya formateado.
     * @param lore          Líneas de lore (cada String será coloreada en gris).
     * @param material      Tipo de ítem (Items.CHEST, Items.BOOK, etc.).
     * @param idTag         Identificador custom para detectar clics.
     * @param slotIndex     Índice de slot para protección de MenuItemProtector.
     */
    public static ItemStack createButton(ITextComponent nameComponent, List<String> lore, Item material, String idTag, int slotIndex) {
        ItemStack item = new ItemStack(material);

        // 1) Display NBT
        CompoundNBT display = new CompoundNBT();
        // Serializamos el nombre (ya coloreado o gradient) a JSON
        display.putString("Name", ITextComponent.Serializer.toJson(nameComponent));

        // 2) Lore NBT
        if (lore != null && !lore.isEmpty()) {
            ListNBT loreList = new ListNBT();
            for (String line : lore) {
                ITextComponent lineComp = new StringTextComponent(line);
                String json = ITextComponent.Serializer.toJson(lineComp);
                loreList.add(StringNBT.valueOf(json));
            }
            display.put("Lore", loreList);
        }

        // 3) Tag de identificación y slot
        CompoundNBT tag = item.getOrCreateTag();
        tag.put("display", display);
        tag.putString("MenuButtonID", idTag);
        tag.putInt("MenuSlot", slotIndex);

        item.setTag(tag);
        return item;
    }
}
