package rl.sage.rangerlevels.items.totems.fragmentos;

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
import rl.sage.rangerlevels.util.GradientText;
import rl.sage.rangerlevels.util.NBTUtils;

import java.util.Arrays;
import java.util.List;

public class TotemAbismoGlacial extends RangerItemDefinition {
    public static final String ID = "totem_abismo_glacial";

    public TotemAbismoGlacial() {
        super(
                ID,
                PixelmonItems.icy_rock,  // elige un ítem base representativo
                Tier.MITICO,
                null,
                "✦ Tótem del Abismo Glacial ✦",
                null
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(amount);
        // Nombre con degradado del tier
        stack.setHoverName(GradientText.of(getDisplayName(), "#A4FFFB", "#0BAA9F", "#272082")
                .withStyle(style -> style.withItalic(false))
        );

        // Lore descriptivo
        List<IFormattableTextComponent> lore = Arrays.asList(
                new StringTextComponent("§7✧ Tipos Afectados: Hielo, Agua, Volador, Veneno, Hada, Acero"),
                new StringTextComponent("§7✧ Inmunidad a ahogarse y daño de caída"),
                new StringTextComponent("§7✧ Captura de Tipos:"),
                new StringTextComponent("   §7- 50% probabilidad de §6+5-12% IVs"),
                new StringTextComponent("   §7- 5% probabilidad de convertir en Shiny"),
                new StringTextComponent("   §7- Las dimensiones del Pokémon se ven afectadas"),
                new StringTextComponent("§7✧ Bonus de §6+30%§7 EXP en:"),
                new StringTextComponent("   §7- Capturas de Tipo"),
                new StringTextComponent("   §7- Derrotas de Tipo"),
                new StringTextComponent("   §7- Invocación de Arceus"),
                new StringTextComponent("§7✧ Debe estar en el inventario"),
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

        // Enchantment visual opcional
        EnchantUtils.addEnchantment(stack, Enchantments.UNBREAKING, 1);

        // Ocultar atributos extra
        NBTUtils.applyAllHideFlags(tag);
        stack.setTag(tag);
        return stack;
    }
}
