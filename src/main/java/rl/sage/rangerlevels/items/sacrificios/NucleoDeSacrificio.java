// File: rl/sage/rangerlevels/items/sacrificios/NucleoDeSacrificio.java
package rl.sage.rangerlevels.items.sacrificios;

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
import rl.sage.rangerlevels.util.GradientText;
import rl.sage.rangerlevels.util.NBTUtils;

import java.util.Arrays;
import java.util.List;

public class NucleoDeSacrificio extends RangerItemDefinition {
    public static final String ID = "nucleo_sacrificio";

    public NucleoDeSacrificio() {
        super(
                ID,
                PixelmonItems.dark_stone,  // ícono por defecto; cámbialo si prefieres otro
                Tier.SINGULAR,
                null,
                "✦ Núcleo de Sacrificio ✦",
                null
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(amount);
        stack.setHoverName(
                GradientText.of(getDisplayName(), "#FF4500", "#8B0000")
                        .withStyle(s -> s.withItalic(false)));

        // Lore descriptivo
        List<IFormattableTextComponent> lore = Arrays.asList(
                new StringTextComponent("§7✧ Un núcleo imbuido con la Esencia"),
                new StringTextComponent("§7   liberada de varios legendarios."),
                new StringTextComponent(" "),
                new StringTextComponent("§7❖ Se usa para invocaciones del "),
                new StringTextComponent("§4   Altar de Almas"),
                new StringTextComponent(" "),
                new StringTextComponent("§7▶ Tier: ").append(Tier.SINGULAR.getColor())
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

        // Brillo para resaltar
        EnchantUtils.addEnchantment(stack, Enchantments.UNBREAKING, 1);
        NBTUtils.applyAllHideFlags(tag);
        stack.setTag(tag);
        return stack;
    }
}
