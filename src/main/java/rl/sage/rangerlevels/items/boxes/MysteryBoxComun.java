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
 * Tier COMÚN: cofre normal
 */
public class MysteryBoxComun extends RangerItemDefinition {
    public static final String ID = "caja_misteriosa_comun";

    public MysteryBoxComun() {
        super(
                ID,
                Items.CHEST,
                Tier.COMUN,
                TextFormatting.GRAY,
                "❖ Caja Misteriosa Común ❖",
                null
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(amount);
        // 1) Nombre con degradado
        stack.setHoverName(Tier.COMUN.applyGradient(getDisplayName()));

        // 2) Lore
        MysteryBoxesConfig.MysteryBoxConfig.TierBoxConfig cfg = MysteryBoxesConfig.get().mysteryBox.comun;
        List<IFormattableTextComponent> lore = Arrays.asList(
                new StringTextComponent("§7✧ Contiene recompensas exclusivas del pase"),
                new StringTextComponent("§7✧ Colocalo en el suelo y abrelo"),
                new StringTextComponent(" "),
                new StringTextComponent("§7▶ Tier: ").append(Tier.COMUN.getColor())
        );
        CompoundNBT tag = stack.getOrCreateTag();
        CompoundNBT display = tag.contains("display") ? tag.getCompound("display") : new CompoundNBT();
        ListNBT loreList = new ListNBT();
        for (IFormattableTextComponent line : lore) {
            loreList.add(StringNBT.valueOf(IFormattableTextComponent.Serializer.toJson(line)));
        }
        display.put("Lore", loreList);
        tag.put("display", display);

        // 3) Flags
        NBTUtils.applyAllHideFlags(tag);
        stack.setTag(tag);
        return stack;
    }
}
