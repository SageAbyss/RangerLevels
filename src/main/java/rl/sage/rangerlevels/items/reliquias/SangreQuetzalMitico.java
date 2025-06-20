// File: rl/sage/rangerlevels/items/bonuses/SangreQuetzalMitico.java
package rl.sage.rangerlevels.items.reliquias;

import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
import rl.sage.rangerlevels.util.GradientText;
import rl.sage.rangerlevels.util.NBTUtils;

import java.util.Arrays;
import java.util.List;

public class SangreQuetzalMitico extends RangerItemDefinition {
    public static final String ID = "sangre_quetzal_mitico";

    public SangreQuetzalMitico() {
        super(
                ID,
                PixelmonItems.curry_seasoned,
                Tier.MITICO,
                null,
                "✦ Sangre de Quetzalcóatl ✦",
                null
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(1);
        stack.setHoverName(GradientText.of(getDisplayName(), "#F72E3A","#860524")
                .withStyle(style -> style.withItalic(false)));

        ItemsConfig.BloodConfig cfg = ItemsConfig.get().blood;
        double pct = cfg.miticoPercent;
        int   dur = cfg.miticoDurationMinutes;

        List<IFormattableTextComponent> lore = Arrays.asList(
                new StringTextComponent("§7❖ Bonus al x2 de EXP general"),
                new StringTextComponent(String.format("§7❖ Chance de +%.0f%% por %d min", pct, dur)),
                new StringTextComponent("§7✧ Usar para activar."),
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
        EnchantUtils.addEnchantment(stack, Enchantments.UNBREAKING, 1);
        NBTUtils.applyAllHideFlags(tag);
        stack.setTag(tag);
        return stack;
    }
}
