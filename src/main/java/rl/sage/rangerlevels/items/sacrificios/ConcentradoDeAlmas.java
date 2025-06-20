// File: rl/sage/rangerlevels/items/sacrificios/ConcentradoDeAlmas.java
package rl.sage.rangerlevels.items.sacrificios;

import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import net.minecraft.enchantment.Enchantment;
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
import rl.sage.rangerlevels.util.GradientText;
import rl.sage.rangerlevels.util.NBTUtils;

import java.util.Arrays;
import java.util.List;

public class ConcentradoDeAlmas extends RangerItemDefinition {
    public static final String ID = "concentrado_almas";

    public ConcentradoDeAlmas() {
        super(
                ID,
                PixelmonItems.intriguing_stone,
                Tier.MITICO,
                null,
                "✦ Concentrado de Almas ✦",
                null
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(amount);

        // Nombre con gradiente
        stack.setHoverName(
                GradientText.of(getDisplayName(), "#A75EE4", "#3C88AC")
                        .withStyle(s -> s.withItalic(false))
        );

        // Lore fijo mostrando los valores de bonus
        List<IFormattableTextComponent> lore = Arrays.asList(
                new StringTextComponent("§7✧ Un poderoso extracto, resultado de"),
                new StringTextComponent("§7   fusionar múltiples Esencias legendarias."),
                new StringTextComponent("§7✧ Efectos por Captura de Legendarios:"),
                new StringTextComponent("§7❖ Bonus EXP: §6+30%"),
                new StringTextComponent("§7❖ Taza de Huida: §6–15%"),
                new StringTextComponent(" "),
                new StringTextComponent("§7▶ Tier: ").append(Tier.MITICO.getColor())
        );

        CompoundNBT tag = stack.getOrCreateTag();
        CompoundNBT display = tag.contains("display")
                ? tag.getCompound("display")
                : new CompoundNBT();
        ListNBT loreList = new ListNBT();
        for (IFormattableTextComponent line : lore) {
            loreList.add(StringNBT.valueOf(IFormattableTextComponent.Serializer.toJson(line)));
        }
        display.put("Lore", loreList);
        tag.put("display", display);

        // Brillo decorativo
        EnchantUtils.addEnchantment(stack, Enchantments.UNBREAKING, 1);
        NBTUtils.applyAllHideFlags(tag);
        stack.setTag(tag);
        return stack;
    }
}
