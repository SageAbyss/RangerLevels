// File: rl/sage/rangerlevels/items/reliquias/ReliquiaTemporalComun.java
package rl.sage.rangerlevels.items.reliquias;

import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.Tier;
import rl.sage.rangerlevels.util.NBTUtils;
import java.util.Arrays;
import java.util.List;

public class ReliquiaTemporalComun extends RangerItemDefinition {
    public static final String ID = "reliquia_temporal_comun";

    public ReliquiaTemporalComun() {
        super(
                ID,
                PixelmonItems.lure_casing_weak,
                Tier.COMUN,
                null,
                "✦ Reliquia Temporal Común ✦",
                null
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(amount);
        stack.setHoverName(Tier.COMUN.applyGradient(getDisplayName()));

        List<IFormattableTextComponent> lore = Arrays.asList(
                new StringTextComponent("§7✧ Repite la última cantidad de EXP obtenida"),
                new StringTextComponent("§7✧ Bonificación: +10%"),
                new StringTextComponent("§7✧ Click para activar"),
                new StringTextComponent(" "),
                new StringTextComponent("§7▶ Tier: ").append(Tier.COMUN.getColor())
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

        NBTUtils.applyAllHideFlags(tag);
        stack.setTag(tag);
        return stack;
    }
}
