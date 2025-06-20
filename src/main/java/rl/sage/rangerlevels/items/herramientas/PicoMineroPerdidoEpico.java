// File: rl/sage/rangerlevels/items/bonuses/PicoMineroPerdidoEpico.java
package rl.sage.rangerlevels.items.herramientas;

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
import rl.sage.rangerlevels.util.NBTUtils;

import java.util.Arrays;
import java.util.List;

public class PicoMineroPerdidoEpico extends RangerItemDefinition {
    public static final String ID = "pico_minero_perdido_epico";

    public PicoMineroPerdidoEpico() {
        super(
                ID,
                Items.NETHERITE_PICKAXE,
                Tier.EPICO,
                null,
                "✦ Pico del Minero Perdido ✦",
                null
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(1);
        stack.setHoverName(Tier.EPICO.applyGradient(getDisplayName()));

        double pct = ItemsConfig.get().pickBonus.epicPercent;
        List<IFormattableTextComponent> lore = Arrays.asList(
                new StringTextComponent("§7❖ Bonus de EXP al minar bloques"),
                new StringTextComponent(String.format("§7❖ Bonus de EXP: §6+%.0f%%", pct)),
                new StringTextComponent("§7✧ Debe estar en el Inventario"),
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

        NBTUtils.applyAllHideFlags(tag);
        stack.setTag(tag);
        return stack;
    }
}
