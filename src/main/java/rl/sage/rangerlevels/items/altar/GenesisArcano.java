package rl.sage.rangerlevels.items.altar;

import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.Tier;
import rl.sage.rangerlevels.util.EnchantUtils;
import rl.sage.rangerlevels.util.NBTUtils;

import java.util.Arrays;
import java.util.List;

public class GenesisArcano extends RangerItemDefinition {
    public static final String ID = "genesis_arcano";

    public GenesisArcano() {
        super(
                ID,
                PixelmonItems.nugget,
                Tier.LEGENDARIO,
                null,
                "✹ Génesis ⚶ Arcano ✹",
                null
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(amount);
        // Nombre con degradado LEGENDARIO
        stack.setHoverName(Tier.LEGENDARIO.applyGradient(getDisplayName()));

        // Lore: núcleo de invocación + algo de historia
        List<IFormattableTextComponent> lore = Arrays.asList(
                new StringTextComponent("§7✧ Núcleo de toda invocación del Altar Arcano"),
                new StringTextComponent("§7✧ Se dice que contiene el eco de los primeros dioses"),
                new StringTextComponent("§7✧ Su pulso rítmico marca el compás del multiverso"),
                new StringTextComponent(" "),
                new StringTextComponent("§7▶ Tier: ").append(Tier.LEGENDARIO.getColor())
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

        // Un toque de encantamiento visual
        EnchantUtils.addEnchantment(stack, Enchantments.UNBREAKING, 1);

        // Ocultar flags extra
        NBTUtils.applyAllHideFlags(tag);
        stack.setTag(tag);
        return stack;
    }
}
