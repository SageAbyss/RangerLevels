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
 * Tier MÍTICO: cofre del End
 */
public class MysteryBoxMitico extends RangerItemDefinition {
    public static final String ID = "caja_misteriosa_mitico";

    public MysteryBoxMitico() {
        super(
                ID,
                Items.ENDER_CHEST,
                Tier.MITICO,
                TextFormatting.RED,
                "❖ Caja Misteriosa Mítico ❖",
                null
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(amount);
        stack.setHoverName(Tier.MITICO.applyGradient(getDisplayName()));

        MysteryBoxesConfig.MysteryBoxConfig.TierBoxConfig cfg = MysteryBoxesConfig.get().mysteryBox.mitico;
        List<IFormattableTextComponent> lore = Arrays.asList(
                new StringTextComponent("§7✧ Recompensas míticas del pase"),
                new StringTextComponent("§7✧ Haz clic izquierdo para abrir"),
                new StringTextComponent("§7▶ Tier: ").append(Tier.MITICO.getColor())
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
