// File: rl/sage/rangerlevels/items/reliquias/CapsulaExperienciaVolatil.java
package rl.sage.rangerlevels.items.randoms;

import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.config.ItemsConfig;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.Tier;
import rl.sage.rangerlevels.util.EnchantUtils;
import rl.sage.rangerlevels.util.NBTUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Cápsula de Experiencia Volátil: Tier Singular.
 * 50% de probabilidad de fallo, en caso de éxito otorga EXP aleatoria
 * en rango configurable en ItemsConfig.
 */
public class CapsulaExperienciaVolatil extends RangerItemDefinition {
    public static final String ID = "capsula_experiencia_volatil";

    public CapsulaExperienciaVolatil() {
        super(
                ID,
                PixelmonItems.terrain_extender ,
                Tier.EPICO,
                null,
                "✦ Cápsula Volátil de EXP ✦",
                null
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(amount);
        stack.setHoverName(Tier.EPICO.applyGradient(getDisplayName()));
        int min = ItemsConfig.get().volatilCapsule.expMin;
        int max = ItemsConfig.get().volatilCapsule.expMax;
        double fail = ItemsConfig.get().volatilCapsule.failChance * 100;
        List<IFormattableTextComponent> lore = Arrays.asList(
                new StringTextComponent("§7❖ Nivel de riesgo: §cAlta§7 | Úsala con cuidado"),
                new StringTextComponent("§7❖ Rango de EXP: §e" + min + "§7–§e" + max),
                new StringTextComponent("§7❖ Probabilidad de fallo: §c" + String.format("%.0f%%", fail)),
                new StringTextComponent("§7✧ Solo se consume al activarse"),
                new StringTextComponent(" "),
                new StringTextComponent("§7▶ Tier: ").append(Tier.EPICO.getColor())
    );

        CompoundNBT tag = stack.getOrCreateTag();
        CompoundNBT display = tag.contains("display") ? tag.getCompound("display") : new CompoundNBT();
        ListNBT loreList = new ListNBT();
        for (IFormattableTextComponent line : lore) {
            loreList.add(StringNBT.valueOf(IFormattableTextComponent.Serializer.toJson(line)));
        }
        display.put("Lore", loreList);
        tag.put("display", display);
        EnchantUtils.addEnchantment(stack, Enchantments.UNBREAKING, 1);
        NBTUtils.applyAllHideFlags(tag);
        stack.setTag(tag);
        return stack;
    }
}