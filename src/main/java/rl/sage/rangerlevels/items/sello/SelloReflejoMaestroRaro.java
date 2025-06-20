package rl.sage.rangerlevels.items.sello;

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

public class SelloReflejoMaestroRaro extends RangerItemDefinition {
    public static final String ID = "sello_reflejo_maestro_raro";

    public SelloReflejoMaestroRaro() {
        super(
                ID,
                PixelmonItems.legendary_clues,  // ítem base a elección
                Tier.RARO,
                null,
                "✦ Sello Reflejo del Maestro ✦",
                null
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(amount);
        // Nombre con degradado del tier:
        stack.setHoverName(Tier.RARO.applyGradient(getDisplayName()));

        // Obtener defaults para el lore:
        double chance = ItemsConfig.get().selloReflejo.chanceRaro;
        int durMin    = ItemsConfig.get().selloReflejo.durationRaro;
        List<IFormattableTextComponent> lore = Arrays.asList(
                new StringTextComponent(String.format("§7✧ Chance de x2 EXP en combates: §6%.1f%%", chance)),
                new StringTextComponent(String.format("§7✧ Duración al usar: §6%d minutos", durMin)),
                new StringTextComponent("§7✧ Usar para activar el efecto"),
                new StringTextComponent(" "),
                new StringTextComponent("§7▶ Tier: ").append(Tier.RARO.getColor())
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