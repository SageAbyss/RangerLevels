// File: rl/sage/rangerlevels/items/sacrificios/ModificadorIVs.java
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

public class ModificadorIVs extends RangerItemDefinition {
    public static final String ID = "modificador_ivs";
    private static final String NBT_SPECIES    = "ModifierSpecies";
    private static final String NBT_ACTION     = "SelectedAction";  // "up" o "down"
    private static final String NBT_STAT       = "SelectedStat";    // HP, ATK, DEF, SPATK, SPDEF, SPEED
    private static final String NBT_POKEMON_ID = "ModifierPokemonID";

    public ModificadorIVs() {
        super(
                ID,
                PixelmonItems.legendary_clues,
                Tier.RARO,
                TextFormatting.GOLD,
                "✦ Modificador IVs ✦",
                getBaseLore()
        );
        CustomItemRegistry.register(this);
    }

    private static List<IFormattableTextComponent> getBaseLore() {
        List<IFormattableTextComponent> lore = new ArrayList<>();
        lore.add(new StringTextComponent("§7Utiliza esta herramienta para"));
        lore.add(new StringTextComponent("§7subir o bajar un IV a su valor máximo"));
        lore.add(new StringTextComponent("§731 o mínimo 0, del stat elegido."));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7Haz clic derecho para elegir acción"));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7▶ Aún no asignado"));
        return lore;
    }

    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(amount);
        NBTUtils.applyAllHideFlags(stack.getOrCreateTag());
        return stack;
    }

    /** Ligado solo a especie */
    public static ItemStack createForSpecies(String species) {
        ModificadorIVs def = (ModificadorIVs) CustomItemRegistry.getDefinition(ID);
        ItemStack stack = def.createStack(1);
        CompoundNBT tag = stack.getOrCreateTag();
        tag.putString(NBT_SPECIES, species);

        stack.setHoverName(
                new StringTextComponent("✦ Modificador IVs ✦")
                        .setStyle(Style.EMPTY.withColor(TextFormatting.GOLD).withItalic(false))
        );

        List<IFormattableTextComponent> lore = new ArrayList<>();
        lore.add(new StringTextComponent("§7Especie: §e" + species));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7Haz clic derecho para elegir acción"));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7▶ Acción: §cNinguna"));
        CompoundNBT display = tag.contains("display") ? tag.getCompound("display") : new CompoundNBT();
        NBTUtils.writeLore(display, lore);
        tag.put("display", display);

        NBTUtils.applyAllHideFlags(tag);
        stack.setTag(tag);
        return stack;
    }

    /** Ligado a instancia */
    public static ItemStack createForSpeciesAndId(String species, String uniqueId) {
        ModificadorIVs def = (ModificadorIVs) CustomItemRegistry.getDefinition(ID);
        ItemStack stack = def.createStack(1);
        CompoundNBT tag = stack.getOrCreateTag();
        tag.putString(NBT_SPECIES, species);
        tag.putString(NBT_POKEMON_ID, uniqueId);

        stack.setHoverName(
                new StringTextComponent("✦ Modificador IVs ✦")
                        .setStyle(Style.EMPTY.withColor(TextFormatting.GOLD).withItalic(false))
        );

        List<IFormattableTextComponent> lore = new ArrayList<>();
        lore.add(new StringTextComponent("§7Especie: §e" + species));
        lore.add(new StringTextComponent("§7Válido solo para esta instancia"));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7Haz clic derecho para elegir acción"));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7▶ Acción: §cNinguna"));
        CompoundNBT display = tag.contains("display") ? tag.getCompound("display") : new CompoundNBT();
        NBTUtils.writeLore(display, lore);
        tag.put("display", display);

        NBTUtils.applyAllHideFlags(tag);
        stack.setTag(tag);
        return stack;
    }

    /** Selección de acción en menú (up/31 o down/0) */
    public static void setAction(ItemStack stack, String action) {
        if (stack.isEmpty()) return;
        CompoundNBT tag = stack.getOrCreateTag();
        if (!tag.contains(NBT_SPECIES)) return;
        tag.putString(NBT_ACTION, action);

        String species = tag.getString(NBT_SPECIES);
        List<IFormattableTextComponent> lore = new ArrayList<>();
        lore.add(new StringTextComponent("§7Especie: §e" + species));
        if (tag.contains(NBT_POKEMON_ID)) {
            lore.add(new StringTextComponent("§7Válido solo para esta instancia"));
        }
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7Acción: §a" + (action.equals("up") ? "Subir a 31" : "Bajar a 0")));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7Haz clic derecho para elegir stat"));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7▶ Stat: §cNinguno"));
        CompoundNBT display = tag.getCompound("display");
        NBTUtils.writeLore(display, lore);
        tag.put("display", display);

        EnchantUtils.addEnchantment(stack, Enchantments.UNBREAKING, 1);
        stack.setTag(tag);
    }

    /** Selección de stat en menú */
    public static void setStat(ItemStack stack, String stat) {
        if (stack.isEmpty()) return;
        CompoundNBT tag = stack.getOrCreateTag();
        if (!tag.contains(NBT_ACTION)) return;
        tag.putString(NBT_STAT, stat);

        String species = tag.getString(NBT_SPECIES);
        String action  = tag.getString(NBT_ACTION);
        List<IFormattableTextComponent> lore = new ArrayList<>();
        lore.add(new StringTextComponent("§7Especie: §e" + species));
        if (tag.contains(NBT_POKEMON_ID)) {
            lore.add(new StringTextComponent("§7Válido solo para esta instancia"));
        }
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7Acción: §e" + (action.equals("up") ? "Subir a 31" : "Bajar a 0")));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7Stat: §a" + stat));
        CompoundNBT display = tag.getCompound("display");
        NBTUtils.writeLore(display, lore);
        tag.put("display", display);

        stack.setTag(tag);
    }

    public static String getSpecies(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(NBT_SPECIES))
            return stack.getTag().getString(NBT_SPECIES);
        return null;
    }
    public static String getAction(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(NBT_ACTION))
            return stack.getTag().getString(NBT_ACTION);
        return null;
    }
    public static String getStat(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(NBT_STAT))
            return stack.getTag().getString(NBT_STAT);
        return null;
    }
    public static String getPokemonId(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(NBT_POKEMON_ID))
            return stack.getTag().getString(NBT_POKEMON_ID);
        return null;
    }
}
