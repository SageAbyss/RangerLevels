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
     * Crea un botón nuevo: ItemStack de material con nombre, lore y tags de menú.
     *
     * @param nameComponent Componente de texto ya formateado (ej. gradiente).
     * @param lore          Lista de líneas de lore (cada String se convierte a JSON en gris).
     * @param material      Tipo de ítem (Items.CHEST, Items.BOOK, etc.).
     * @param idTag         Identificador interno del botón (p.ej. “shop_select_Pikachu”).
     * @param slotIndex     Índice del slot (0..menuSize-1) donde estará el botón.
     */
    public static ItemStack createButton(ITextComponent nameComponent,
                                         List<String> lore,
                                         Item material,
                                         String idTag,
                                         int slotIndex)
    {
        // Creamos el ItemStack base
        ItemStack item = new ItemStack(material);

        // Añadimos nombre y lore
        addDisplayAndLore(item, nameComponent, lore);

        // Añadimos tags de menú
        CompoundNBT tag = item.getOrCreateTag();
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

    /**
     * Decora un ItemStack existente con nombre y lore (sin tags de menú).
     *
     * @param stack     ItemStack base.
     * @param name      Nombre a mostrar (String).
     * @param loreLines Líneas de lore.
     * @return El mismo ItemStack modificado con display name y lore.
     */
    public static ItemStack decorateWithNameAndLore(ItemStack stack,
                                                    String name,
                                                    List<String> loreLines) {
        return decorateWithNameAndLore(stack, new StringTextComponent(name), loreLines);
    }

    /**
     * Decora un ItemStack existente con nombre y lore (sin tags de menú).
     *
     * @param stack          ItemStack base.
     * @param nameComponent  Nombre a mostrar (ITextComponent).
     * @param loreLines      Líneas de lore.
     * @return El mismo ItemStack modificado con display name y lore.
     */
    public static ItemStack decorateWithNameAndLore(ItemStack stack,
                                                    ITextComponent nameComponent,
                                                    List<String> loreLines) {
        addDisplayAndLore(stack, nameComponent, loreLines);
        return stack;
    }

    /**
     * Decora un ItemStack existente con nombre, lore y tags de menú.
     *
     * @param stack      ItemStack base.
     * @param name       Nombre a mostrar.
     * @param loreLines  Líneas de lore.
     * @param idTag      Identificador interno del botón.
     * @param slotIndex  Índice del slot donde estará.
     * @return El mismo ItemStack modificado.
     */
    public static ItemStack decorateWithNameAndLore(ItemStack stack,
                                                    String name,
                                                    List<String> loreLines,
                                                    String idTag,
                                                    int slotIndex) {
        return decorateWithNameAndLore(stack, new StringTextComponent(name), loreLines, idTag, slotIndex);
    }

    /**
     * Decora un ItemStack existente con nombre, lore y tags de menú.
     *
     * @param stack          ItemStack base.
     * @param nameComponent  Nombre a mostrar.
     * @param loreLines      Líneas de lore.
     * @param idTag          Identificador interno del botón.
     * @param slotIndex      Índice del slot donde estará.
     * @return El mismo ItemStack modificado.
     */
    public static ItemStack decorateWithNameAndLore(ItemStack stack,
                                                    ITextComponent nameComponent,
                                                    List<String> loreLines,
                                                    String idTag,
                                                    int slotIndex) {
        addDisplayAndLore(stack, nameComponent, loreLines);
        CompoundNBT tag = stack.getOrCreateTag();
        tag.putString("MenuButtonID", idTag);
        tag.putInt("MenuSlot", slotIndex);
        stack.setTag(tag);
        return stack;
    }

    /**
     * Método interno que aplica el display name y el lore en el tag "display".
     */
    private static void addDisplayAndLore(ItemStack stack,
                                          ITextComponent nameComponent,
                                          List<String> loreLines) {
        if (stack == null) return;
        CompoundNBT tag = stack.getOrCreateTag();
        CompoundNBT display = new CompoundNBT();
        // Nombre en JSON
        display.putString("Name", ITextComponent.Serializer.toJson(nameComponent));
        // Lore
        if (loreLines != null && !loreLines.isEmpty()) {
            ListNBT loreList = new ListNBT();
            for (String line : loreLines) {
                ITextComponent lineComp = new StringTextComponent(line);
                String json = ITextComponent.Serializer.toJson(lineComp);
                loreList.add(StringNBT.valueOf(json));
            }
            display.put("Lore", loreList);
        }
        tag.put("display", display);
        stack.setTag(tag);
    }
}
