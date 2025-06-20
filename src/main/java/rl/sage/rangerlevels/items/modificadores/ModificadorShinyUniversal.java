// File: rl/sage/rangerlevels/items/sacrificios/ModificadorShinyUniversal.java
package rl.sage.rangerlevels.items.modificadores;

import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.Tier;
import rl.sage.rangerlevels.util.EnchantUtils;
import rl.sage.rangerlevels.util.NBTUtils;

import java.util.ArrayList;
import java.util.List;

public class ModificadorShinyUniversal extends RangerItemDefinition {
    public static final String ID = "modificador_shiny_universal";
    private static final String NBT_SHINY = "SelectedShiny";

    public ModificadorShinyUniversal() {
        super(
                ID,
                PixelmonItems.legendary_clues,
                Tier.RARO,
                TextFormatting.LIGHT_PURPLE,
                "✦ Modificador Shiny Universal ✦",
                getBaseLore()
        );
        CustomItemRegistry.register(this);
    }

    private static List<IFormattableTextComponent> getBaseLore() {
        List<IFormattableTextComponent> lore = new ArrayList<>();
        lore.add(new StringTextComponent("§7Utiliza esta herramienta para"));
        lore.add(new StringTextComponent("§7hacer cualquier Legendario o Ultraente"));
        lore.add(new StringTextComponent("§7Shiny o No Shiny."));
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

    /** Llamar cuando el jugador elige Shiny/No Shiny en el menú */
    public static void setShiny(ItemStack stack, boolean shiny) {
        if (stack.isEmpty()) return;
        CompoundNBT tag = stack.getOrCreateTag();
        tag.putBoolean(NBT_SHINY, shiny);

        List<IFormattableTextComponent> lore = new ArrayList<>();
        lore.add(new StringTextComponent("§7Modificador Shiny Universal"));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7▶ Shiny: §a" + (shiny ? "Sí" : "No")));

        CompoundNBT display = tag.contains("display") ? tag.getCompound("display") : new CompoundNBT();
        NBTUtils.writeLore(display, lore);
        tag.put("display", display);

        EnchantUtils.addEnchantment(stack, Enchantments.UNBREAKING, 1);
        stack.setTag(tag);
    }

    /** Devuelve la selección de shiny (true/false) o null si no está elegida */
    public static Boolean getShiny(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(NBT_SHINY))
            return stack.getTag().getBoolean(NBT_SHINY);
        return null;
    }
}
