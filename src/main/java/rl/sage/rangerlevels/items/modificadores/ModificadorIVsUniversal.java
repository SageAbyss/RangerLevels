// File: rl/sage/rangerlevels/items/sacrificios/ModificadorIVsUniversal.java
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

public class ModificadorIVsUniversal extends RangerItemDefinition {
    public static final String ID = "modificador_ivs_universal";
    private static final String NBT_ACTION = "SelectedAction";
    private static final String NBT_STAT   = "SelectedStat";

    public ModificadorIVsUniversal() {
        super(
                ID,
                PixelmonItems.legendary_clues,
                Tier.RARO,
                TextFormatting.LIGHT_PURPLE,
                "✦ Modificador IVs Universal ✦",
                getBaseLore()
        );
        CustomItemRegistry.register(this);
    }

    private static List<IFormattableTextComponent> getBaseLore() {
        List<IFormattableTextComponent> lore = new ArrayList<>();
        lore.add(new StringTextComponent("§7Utiliza esta herramienta para"));
        lore.add(new StringTextComponent("§7ajustar un IV (0 o 31)"));
        lore.add(new StringTextComponent("§7de cualquier legendario o Ultraente."));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7Haz clic derecho para elegir acción"));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7▶ Acción: §cNinguna"));
        return lore;
    }

    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(amount);
        NBTUtils.applyAllHideFlags(stack.getOrCreateTag());
        return stack;
    }

    /** Selección de acción (up/31 o down/0) */
    public static void setAction(ItemStack stack, String action) {
        if (stack.isEmpty()) return;
        CompoundNBT tag = stack.getOrCreateTag();
        tag.putString(NBT_ACTION, action);

        List<IFormattableTextComponent> lore = new ArrayList<>();
        lore.add(new StringTextComponent("§7Modificador IVs Universal"));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7Acción: §e" + (action.equals("up") ? "Subir a 31" : "Bajar a 0")));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7Haz clic derecho para elegir stat"));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7▶ Stat: §cNinguno"));

        CompoundNBT display = tag.contains("display") ? tag.getCompound("display") : new CompoundNBT();
        NBTUtils.writeLore(display, lore);
        tag.put("display", display);

        EnchantUtils.addEnchantment(stack, Enchantments.UNBREAKING, 1);
        stack.setTag(tag);
    }

    /** Selección de stat */
    public static void setStat(ItemStack stack, String stat) {
        if (stack.isEmpty()) return;
        CompoundNBT tag = stack.getOrCreateTag();
        if (!tag.contains(NBT_ACTION)) return;
        tag.putString(NBT_STAT, stat);

        String action = tag.getString(NBT_ACTION);
        List<IFormattableTextComponent> lore = new ArrayList<>();
        lore.add(new StringTextComponent("§7Modificador IVs Universal"));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7Acción: §e" + (action.equals("up") ? "Subir a 31" : "Bajar a 0")));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7Stat: §a" + stat));

        CompoundNBT display = tag.getCompound("display");
        NBTUtils.writeLore(display, lore);
        tag.put("display", display);

        stack.setTag(tag);
    }

    public static String getAction(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(NBT_ACTION)
                ? stack.getTag().getString(NBT_ACTION)
                : null;
    }

    public static String getStat(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(NBT_STAT)
                ? stack.getTag().getString(NBT_STAT)
                : null;
    }
}
