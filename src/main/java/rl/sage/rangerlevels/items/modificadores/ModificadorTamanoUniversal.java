// File: rl/sage/rangerlevels/items/sacrificios/ModificadorTamanoUniversal.java
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

public class ModificadorTamanoUniversal extends RangerItemDefinition {
    public static final String ID = "modificador_tamano_universal";
    private static final String NBT_SIZE = "SelectedSize";

    public ModificadorTamanoUniversal() {
        super(
                ID,
                PixelmonItems.legendary_clues,
                Tier.RARO,
                TextFormatting.AQUA,
                "✦ Modificador de Tamaño Universal ✦",
                getBaseLore()
        );
        CustomItemRegistry.register(this);
    }

    private static List<IFormattableTextComponent> getBaseLore() {
        List<IFormattableTextComponent> lore = new ArrayList<>();
        lore.add(new StringTextComponent("§7Utiliza esta herramienta para"));
        lore.add(new StringTextComponent("§7aplicar cualquier Tamaño"));
        lore.add(new StringTextComponent("§7a un Legendario o Ultraente."));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7Haz clic derecho para elegir Tamaño"));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7▶ Tamaño: §cNinguno"));
        return lore;
    }

    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(amount);
        NBTUtils.applyAllHideFlags(stack.getOrCreateTag());
        return stack;
    }

    /** Llamar cuando el jugador elige un tamaño en el menú */
    public static void setSize(ItemStack stack, String sizeName) {
        if (stack.isEmpty()) return;
        CompoundNBT tag = stack.getOrCreateTag();
        tag.putString(NBT_SIZE, sizeName);

        List<IFormattableTextComponent> lore = new ArrayList<>();
        lore.add(new StringTextComponent("§7Modificador de Tamaño Universal"));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7▶ Tamaño: §a" + sizeName));

        CompoundNBT display = tag.contains("display") ? tag.getCompound("display") : new CompoundNBT();
        NBTUtils.writeLore(display, lore);
        tag.put("display", display);

        EnchantUtils.addEnchantment(stack, Enchantments.UNBREAKING, 1);
        stack.setTag(tag);
    }

    /** Extrae el tamaño, o null si no tiene */
    public static String getSize(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(NBT_SIZE))
            return stack.getTag().getString(NBT_SIZE);
        return null;
    }
}
