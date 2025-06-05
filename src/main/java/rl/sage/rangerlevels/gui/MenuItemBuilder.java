package rl.sage.rangerlevels.gui;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;

/**
 * Helper para crear “botones” que llevan:
 *  - Un display name (puede ser ITextComponent con degradados).
 *  - Un lore (lista de Strings que se convierten a JSON en gris).
 *  - Dos tags NBT: “MenuButtonID” y “MenuSlot”.
 *
 * Todos los menús usan este builder para colocar botones en las ranuras.
 */
public class MenuItemBuilder {

    /**
     * @param nameComponent Componente de texto ya formateado (ej. gradiente).
     * @param lore          Lista de líneas de lore (cada String se convierte a JSON en gris).
     * @param material      Tipo de ítem (Items.CHEST, Items.BOOK, etc.).
     * @param idTag         Identificador interno del botón (p.ej. “rewards”, “help”, …).
     * @param slotIndex     Índice del slot (0..menuSize-1) donde estará el botón.
     */
    public static ItemStack createButton(ITextComponent nameComponent,
                                         List<String> lore,
                                         Item material,
                                         String idTag,
                                         int slotIndex)
    {
        ItemStack item = new ItemStack(material);

        // 1) Construir TAG “display” para nombre y lore:
        CompoundNBT display = new CompoundNBT();
        // Serializamos el nombre (puede venir con colores o gradientes)
        display.putString("Name", ITextComponent.Serializer.toJson(nameComponent));

        // Convertir cada línea de lore a JSON y agregarla en un ListNBT
        if (lore != null && !lore.isEmpty()) {
            ListNBT loreList = new ListNBT();
            for (String line : lore) {
                // Lo coloreamos en gris a nivel de texto
                ITextComponent lineComp = new StringTextComponent(line);
                String json = ITextComponent.Serializer.toJson(lineComp);
                loreList.add(StringNBT.valueOf(json));
            }
            display.put("Lore", loreList);
        }

        // 2) Añadir TAG de identificación y de posición:
        CompoundNBT tag = item.getOrCreateTag();
        tag.put("display", display);
        tag.putString("MenuButtonID", idTag);
        tag.putInt("MenuSlot", slotIndex);
        item.setTag(tag);

        return item;
    }

    /**
     * Sobrecarga que recibe el nombre como String simple.
     */
    public static ItemStack createButton(String name,
                                         List<String> lore,
                                         Item material,
                                         String idTag,
                                         int slotIndex)
    {
        return createButton(new StringTextComponent(name), lore, material, idTag, slotIndex);
    }
}
