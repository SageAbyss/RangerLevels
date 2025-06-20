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
public class LagrimaDiosaTiempo extends RangerItemDefinition {
    public static final String ID = "lagrima_diosa_tiempo";

    public LagrimaDiosaTiempo() {
        super(
                ID,
                PixelmonItems.white_mane_hair,  // ítem base, elegir uno apropiado
                Tier.MITICO,
                null,
                "✦ Lágrima de la Diosa del Tiempo ✦",
                null
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(amount);
        stack.setHoverName(Tier.MITICO.applyGradient(getDisplayName()));

        ItemsConfig.LagrimaTiempoConfig cfg = ItemsConfig.get().lagrimaTiempo;
        int expAmount = cfg.expAmount;
        int intervalMin = cfg.intervalMinutes;

        List<IFormattableTextComponent> lore = Arrays.asList(
                new StringTextComponent(
                        String.format("§7✧ Otorga §6%d§7 de EXP cada §6%d minutos",
                                expAmount, intervalMin)
                ),
                new StringTextComponent("§7✧ No requiere activación, debe permanecer en inventario"),
                new StringTextComponent(" "),
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

        EnchantUtils.addEnchantment(stack, Enchantments.UNBREAKING, 1);
        NBTUtils.applyAllHideFlags(tag);
        stack.setTag(tag);
        return stack;
    }
}