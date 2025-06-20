// File: rl/sage/rangerlevels/items/sacrificios/ModificadorNaturalezaUniversal.java
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

public class ModificadorNaturalezaUniversal extends RangerItemDefinition {
    public static final String ID = "modificador_naturaleza_universal";
    private static final String NBT_NATURE = "SelectedNature";

    public ModificadorNaturalezaUniversal() {
        super(
                ID,
                PixelmonItems.legendary_clues,
                Tier.RARO,
                TextFormatting.GREEN,
                "✦ Modificador de Naturaleza Universal ✦",
                getBaseLore()
        );
        CustomItemRegistry.register(this);
    }

    private static List<IFormattableTextComponent> getBaseLore() {
        List<IFormattableTextComponent> lore = new ArrayList<>();
        lore.add(new StringTextComponent("§7Utiliza esta herramienta para"));
        lore.add(new StringTextComponent("§7aplicar cualquier Naturaleza"));
        lore.add(new StringTextComponent("§7a un Legendario o Ultraente."));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7Haz clic derecho para elegir Naturaleza"));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7▶ Naturaleza: §cNinguna"));
        return lore;
    }

    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(amount);
        NBTUtils.applyAllHideFlags(stack.getOrCreateTag());
        return stack;
    }

    /** Llamar cuando el jugador elige una naturaleza en el menú */
    public static void setNature(ItemStack stack, String natureName) {
        if (stack.isEmpty()) return;
        CompoundNBT tag = stack.getOrCreateTag();
        tag.putString(NBT_NATURE, natureName);

        List<IFormattableTextComponent> lore = new ArrayList<>();
        lore.add(new StringTextComponent("§7Modificador de Naturaleza Universal"));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7▶ Naturaleza: §a" + natureName));

        CompoundNBT display = tag.contains("display") ? tag.getCompound("display") : new CompoundNBT();
        NBTUtils.writeLore(display, lore);
        tag.put("display", display);

        EnchantUtils.addEnchantment(stack, Enchantments.UNBREAKING, 1);
        stack.setTag(tag);
    }

    /** Extrae la naturaleza seleccionada, o null si no tiene */
    public static String getNature(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(NBT_NATURE))
            return stack.getTag().getString(NBT_NATURE);
        return null;
    }
}
