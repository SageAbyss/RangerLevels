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

public class ModificadorTamano extends RangerItemDefinition {
    public static final String ID = "modificador_tamano";
    private static final String NBT_SPECIES    = "ModifierSpecies";
    private static final String NBT_SIZE       = "SelectedSize";
    private static final String NBT_POKEMON_ID = "ModifierPokemonID";

    public ModificadorTamano() {
        super(
                ID,
                PixelmonItems.legendary_clues,  // ítem base; ajústalo si prefieres otro
                Tier.RARO,
                TextFormatting.AQUA,
                "✦ Modificador de Tamaño ✦",
                getBaseLore()
        );
        CustomItemRegistry.register(this);
    }

    private static List<IFormattableTextComponent> getBaseLore() {
        List<IFormattableTextComponent> lore = new ArrayList<>();
        lore.add(new StringTextComponent("§7Utiliza esta herramienta para"));
        lore.add(new StringTextComponent("§7aplicar un Tamaño específico"));
        lore.add(new StringTextComponent("§7al Pokémon indicado por la Esencia."));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7Haz clic derecho para elegir Tamaño"));
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

    /** Crea un Modificador solo ligado a especie (sin ID de instancia) */
    public static ItemStack createForSpecies(String species) {
        ModificadorTamano def = (ModificadorTamano) CustomItemRegistry.getDefinition(ID);
        ItemStack stack = def.createStack(1);

        CompoundNBT tag = stack.getOrCreateTag();
        tag.putString(NBT_SPECIES, species);

        stack.setHoverName(new StringTextComponent("✦ Modificador de Tamaño ✦")
                .setStyle(Style.EMPTY.withColor(TextFormatting.AQUA).withItalic(false)));

        List<IFormattableTextComponent> lore = new ArrayList<>();
        lore.add(new StringTextComponent("§7Especie: §e" + species));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7Haz clic derecho para elegir Tamaño"));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7▶ Tamaño: §cNinguno"));
        CompoundNBT display = tag.contains("display") ? tag.getCompound("display") : new CompoundNBT();
        NBTUtils.writeLore(display, lore);
        tag.put("display", display);

        NBTUtils.applyAllHideFlags(tag);
        stack.setTag(tag);
        return stack;
    }

    /** Crea el Modificador ligado a una instancia concreta de Pokémon */
    public static ItemStack createForSpeciesAndId(String species, String uniqueId) {
        ModificadorTamano def = (ModificadorTamano) CustomItemRegistry.getDefinition(ID);
        ItemStack stack = def.createStack(1);

        CompoundNBT tag = stack.getOrCreateTag();
        tag.putString(NBT_SPECIES, species);
        tag.putString(NBT_POKEMON_ID, uniqueId);

        stack.setHoverName(new StringTextComponent("✦ Modificador de Tamaño ✦")
                .setStyle(Style.EMPTY.withColor(TextFormatting.AQUA).withItalic(false)));

        List<IFormattableTextComponent> lore = new ArrayList<>();
        lore.add(new StringTextComponent("§7Especie: §e" + species));
        lore.add(new StringTextComponent("§7Válido solo para esta instancia"));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7Haz clic derecho para elegir Tamaño"));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7▶ Tamaño: §cNinguno"));
        CompoundNBT display = tag.contains("display") ? tag.getCompound("display") : new CompoundNBT();
        NBTUtils.writeLore(display, lore);
        tag.put("display", display);

        NBTUtils.applyAllHideFlags(tag);
        stack.setTag(tag);
        return stack;
    }

    /** Llama esto cuando el jugador elige un tamaño en el menú */
    public static void setSize(ItemStack stack, String sizeName) {
        if (stack.isEmpty()) return;
        CompoundNBT tag = stack.getOrCreateTag();
        if (!tag.contains(NBT_SPECIES)) return;

        tag.putString(NBT_SIZE, sizeName);

        String species = tag.getString(NBT_SPECIES);
        List<IFormattableTextComponent> lore = new ArrayList<>();
        lore.add(new StringTextComponent("§7Especie: §e" + species));
        if (tag.contains(NBT_POKEMON_ID)) {
            lore.add(new StringTextComponent("§7Válido solo para esta instancia"));
        }
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7Haz clic derecho para elegir Tamaño"));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7▶ Tamaño: §a" + sizeName));
        CompoundNBT display = tag.contains("display") ? tag.getCompound("display") : new CompoundNBT();
        NBTUtils.writeLore(display, lore);
        tag.put("display", display);

        EnchantUtils.addEnchantment(stack, Enchantments.UNBREAKING, 1);
        stack.setTag(tag);
    }

    /** Extrae la especie, o null si no tiene */
    public static String getSpecies(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(NBT_SPECIES))
            return stack.getTag().getString(NBT_SPECIES);
        return null;
    }

    /** Extrae el tamaño, o null si no tiene */
    public static String getSize(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(NBT_SIZE))
            return stack.getTag().getString(NBT_SIZE);
        return null;
    }

    /** Extrae el uniqueId guardado (Personality), o null si no existe o está vacío */
    public static String getPokemonId(ItemStack stack) {
        if (!stack.hasTag()) return null;
        CompoundNBT tag = stack.getTag();
        if (!tag.contains(NBT_POKEMON_ID)) return null;
        String id = tag.getString(NBT_POKEMON_ID);
        return id.isEmpty() ? null : id;
    }
}
