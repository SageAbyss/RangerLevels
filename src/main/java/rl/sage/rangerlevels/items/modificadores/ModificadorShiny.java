// File: rl/sage/rangerlevels/items/sacrificios/ModificadorShiny.java
package rl.sage.rangerlevels.items.modificadores;

import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.Tier;
import rl.sage.rangerlevels.util.EnchantUtils;
import rl.sage.rangerlevels.util.NBTUtils;

import java.util.ArrayList;
import java.util.List;

public class ModificadorShiny extends RangerItemDefinition {
    public static final String ID = "modificador_shiny";
    private static final String NBT_SPECIES    = "ModifierSpecies";
    private static final String NBT_SHINY      = "SelectedShiny";
    private static final String NBT_POKEMON_ID = "ModifierPokemonID";

    public ModificadorShiny() {
        super(
                ID,
                PixelmonItems.legendary_clues,
                Tier.RARO,
                TextFormatting.LIGHT_PURPLE,
                "✦ Modificador Shiny ✦",
                getBaseLore()
        );
        CustomItemRegistry.register(this);
    }

    private static List<IFormattableTextComponent> getBaseLore() {
        List<IFormattableTextComponent> lore = new ArrayList<>();
        lore.add(new StringTextComponent("§7Utiliza esta herramienta para"));
        lore.add(new StringTextComponent("§7hacer tu Pokémon Shiny o No Shiny"));
        lore.add(new StringTextComponent("§7según la Esencia ligada."));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7Haz clic derecho para elegir estado"));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7▶ Shiny: §cNinguno"));
        return lore;
    }

    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(amount);
        NBTUtils.applyAllHideFlags(stack.getOrCreateTag());
        return stack;
    }

    /** Crea un Modificador solo ligado a especie */
    public static ItemStack createForSpecies(String species) {
        ModificadorShiny def = (ModificadorShiny) CustomItemRegistry.getDefinition(ID);
        ItemStack stack = def.createStack(1);

        CompoundNBT tag = stack.getOrCreateTag();
        tag.putString(NBT_SPECIES, species);

        stack.setHoverName(
                new StringTextComponent("✦ Modificador Shiny ✦")
                        .setStyle(Style.EMPTY.withColor(TextFormatting.LIGHT_PURPLE).withItalic(false))
        );

        List<IFormattableTextComponent> lore = new ArrayList<>();
        lore.add(new StringTextComponent("§7Especie: §e" + species));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7Haz clic derecho para elegir estado"));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7▶ Shiny: §cNinguno"));
        CompoundNBT display = tag.contains("display") ? tag.getCompound("display") : new CompoundNBT();
        NBTUtils.writeLore(display, lore);
        tag.put("display", display);

        NBTUtils.applyAllHideFlags(tag);
        stack.setTag(tag);
        return stack;
    }

    /** Crea un Modificador ligado a instancia concreta */
    public static ItemStack createForSpeciesAndId(String species, String uniqueId) {
        ModificadorShiny def = (ModificadorShiny) CustomItemRegistry.getDefinition(ID);
        ItemStack stack = def.createStack(1);

        CompoundNBT tag = stack.getOrCreateTag();
        tag.putString(NBT_SPECIES, species);
        tag.putString(NBT_POKEMON_ID, uniqueId);

        stack.setHoverName(
                new StringTextComponent("✦ Modificador Shiny ✦")
                        .setStyle(Style.EMPTY.withColor(TextFormatting.LIGHT_PURPLE).withItalic(false))
        );

        List<IFormattableTextComponent> lore = new ArrayList<>();
        lore.add(new StringTextComponent("§7Especie: §e" + species));
        lore.add(new StringTextComponent("§7Válido solo para esta instancia"));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7Haz clic derecho para elegir estado"));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7▶ Shiny: §cNinguno"));
        CompoundNBT display = tag.contains("display") ? tag.getCompound("display") : new CompoundNBT();
        NBTUtils.writeLore(display, lore);
        tag.put("display", display);

        NBTUtils.applyAllHideFlags(tag);
        stack.setTag(tag);
        return stack;
    }

    /** Aplica la selección de Shiny (true) o No Shiny (false) */
    public static void setShiny(ItemStack stack, boolean shiny) {
        if (stack.isEmpty()) return;
        CompoundNBT tag = stack.getOrCreateTag();
        if (!tag.contains(NBT_SPECIES)) return;

        tag.putBoolean(NBT_SHINY, shiny);

        String species = tag.getString(NBT_SPECIES);
        List<IFormattableTextComponent> lore = new ArrayList<>();
        lore.add(new StringTextComponent("§7Especie: §e" + species));
        if (tag.contains(NBT_POKEMON_ID)) {
            lore.add(new StringTextComponent("§7Válido solo para esta instancia"));
        }
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7Haz clic derecho para elegir estado"));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7▶ Shiny: §a" + (shiny ? "Sí" : "No")));
        CompoundNBT display = tag.contains("display") ? tag.getCompound("display") : new CompoundNBT();
        NBTUtils.writeLore(display, lore);
        tag.put("display", display);

        EnchantUtils.addEnchantment(stack, Enchantments.UNBREAKING, 1);
        stack.setTag(tag);
    }

    /** Devuelve la especie o null */
    public static String getSpecies(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(NBT_SPECIES))
            return stack.getTag().getString(NBT_SPECIES);
        return null;
    }

    /** Devuelve la selección de shiny o null si no tiene */
    public static Boolean getShiny(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(NBT_SHINY))
            return stack.getTag().getBoolean(NBT_SHINY);
        return null;
    }

    /** Devuelve el ID de instancia o null */
    public static String getPokemonId(ItemStack stack) {
        if (!stack.hasTag()) return null;
        CompoundNBT tag = stack.getTag();
        if (!tag.contains(NBT_POKEMON_ID)) return null;
        String id = tag.getString(NBT_POKEMON_ID);
        return id.isEmpty() ? null : id;
    }
}
