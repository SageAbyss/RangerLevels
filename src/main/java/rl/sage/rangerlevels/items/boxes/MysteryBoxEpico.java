package rl.sage.rangerlevels.items.boxes;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import rl.sage.rangerlevels.config.MysteryBoxesConfig;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.Tier;
import rl.sage.rangerlevels.util.NBTUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Tier ÉPICO: cofre normal
 */
public class MysteryBoxEpico extends RangerItemDefinition {
    public static final String ID = "caja_misteriosa_epico";

    public MysteryBoxEpico() {
        super(
                ID,
                Items.CHEST,
                Tier.EPICO,
                TextFormatting.DARK_PURPLE,
                "❖ Caja Misteriosa Épica ❖",
                null
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(amount);
        stack.setHoverName(Tier.EPICO.applyGradient(getDisplayName()));

        MysteryBoxesConfig.MysteryBoxConfig.TierBoxConfig cfg = MysteryBoxesConfig.get().mysteryBox.epico;
        List<IFormattableTextComponent> lore = Arrays.asList(
                new StringTextComponent("§7✧ Recompensas épicas del pase"),
                new StringTextComponent("§7✧ Haz clic izquierdo para abrir"),
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
