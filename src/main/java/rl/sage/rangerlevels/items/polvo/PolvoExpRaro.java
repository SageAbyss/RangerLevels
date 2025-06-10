// File: rl/sage/rangerlevels/items/polvo/PolvoExpRaro.java
package rl.sage.rangerlevels.items.polvo;

import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.Tier;
import rl.sage.rangerlevels.util.EnchantUtils;
import rl.sage.rangerlevels.util.NBTUtils;

import java.util.Arrays;
import java.util.List;

public class PolvoExpRaro extends RangerItemDefinition {
    public static final String ID = "polvo_exp_raro";

    public PolvoExpRaro() {
        super(
                ID,
                PixelmonItems.quick_powder,
                Tier.RARO,
                null,
                "✦ Polvo de Experiencia Ancestral ✦",
                null
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        // genera el stack base con ID, tier, hideflags y unique tag
        ItemStack stack = super.createStack(amount);

        // Nombre con degradado de Tier RARO
        stack.setHoverName(Tier.RARO.applyGradient(getDisplayName()));

        // Lore siempre aplicado
        List<IFormattableTextComponent> lore = Arrays.asList(
                new StringTextComponent("§7❖ Incrementa la EXP obtenida por:"),
                new StringTextComponent("§7❖ Eclosionar Huevos y Evolucionar Pokémon"),
                new StringTextComponent("§7❖ Bonificación: §6+10%"),
                new StringTextComponent(" "),
                new StringTextComponent("§7▶ Tier: ").append(Tier.RARO.getColor())
        );
        CompoundNBT tag = stack.getOrCreateTag();
        CompoundNBT display = tag.contains("display") ? tag.getCompound("display") : new CompoundNBT();
        ListNBT loreList = new ListNBT();
        for (IFormattableTextComponent line : lore) {
            loreList.add(StringNBT.valueOf(IFormattableTextComponent.Serializer.toJson(line)));
        }
        display.put("Lore", loreList);
        tag.put("display", display);

        // Encanto y hideflags
        EnchantUtils.addEnchantment(stack, Enchantments.UNBREAKING, 1);
        NBTUtils.applyAllHideFlags(tag);

        stack.setTag(tag);
        return stack;
    }
}
