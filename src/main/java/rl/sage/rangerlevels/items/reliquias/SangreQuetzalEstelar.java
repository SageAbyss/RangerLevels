// File: rl/sage/rangerlevels/items/bonuses/SangreQuetzalEstelar.java
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
import rl.sage.rangerlevels.util.NBTUtils;

import java.util.Arrays;
import java.util.List;

public class SangreQuetzalEstelar extends RangerItemDefinition {
    public static final String ID = "sangre_quetzal_estelar";

    public SangreQuetzalEstelar() {
        super(
                ID,
                PixelmonItems.curry_seasoned,
                Tier.ESTELAR,
                null,
                "✦ Sangre de Quetzalcóatl ✦",
                null
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(1);
        stack.setHoverName(Tier.ESTELAR.applyGradient(getDisplayName()));

        ItemsConfig.BloodConfig cfg = ItemsConfig.get().blood;
        double pct = cfg.estelarPercent;
        int   dur = cfg.estelarDurationMinutes;

        List<IFormattableTextComponent> lore = Arrays.asList(
                new StringTextComponent("§7❖ Otorga bonus general de EXP"),
                new StringTextComponent(String.format("§7❖ +%.0f%% por %d min", pct, dur)),
                new StringTextComponent("§7✧ Usar para activar."),
                new StringTextComponent(" "),
                new StringTextComponent("§7▶ Tier: ").append(Tier.ESTELAR.getColor())
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
