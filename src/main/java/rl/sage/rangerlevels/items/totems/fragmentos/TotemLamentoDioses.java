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

/**
 * Tótem del Lamento de los Dioses: Tier MITICO.
 * - Inmunidad a fuego y lava mientras esté en inventario u offhand.
 * - Fuerza II mientras esté en inventario.
 * - 12% de bonus en IVs y 5% de probabilidad de Shiny a Pokémon de tipos: Fuego, Dragón, Lucha, Psíquico, Fantasma y Siniestro.
 * - El Pokémon afectado siempre se hace Microscopic o Ginormous.
 */
public class TotemLamentoDioses extends RangerItemDefinition {
    public static final String ID = "totem_lamento_dioses";

    public TotemLamentoDioses() {
        super(
                ID,
                PixelmonItems.heat_rock,  // base item
                Tier.MITICO,
                null,
                "✦ Tótem del Lamento de los Dioses ✦",
                null
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(amount);
        // Nombre con degradado del tier
        stack.setHoverName(GradientText.of(getDisplayName(), "#FF4A4A", "#F5BE47", "#C82A2A")
                .withStyle(style -> style.withItalic(false))
        );

        // Lore descriptivo
        List<IFormattableTextComponent> lore = Arrays.asList(
                new StringTextComponent("§7✧ Tipos Afectados: Fuego, Dragón, Lucha, Psíquico, Fantasma, Siniestro"),
                new StringTextComponent("§7✧ Inmunidad al fuego"),
                new StringTextComponent("§7✧ Otorga Fuerza II"),
                new StringTextComponent("§7✧ Captura de Tipos:"),
                new StringTextComponent("   §7- 50% probabilidad de §6+5-12% IVs"),
                new StringTextComponent("   §7- 5% probabilidad de convertir en Shiny"),
                new StringTextComponent("   §7- Las dimensiones del Pokémon se ven afectadas"),
                new StringTextComponent("§7✧ Bonus de §6+30%§7 EXP en:"),
                new StringTextComponent("   §7- Capturas por Tipo"),
                new StringTextComponent("   §7- Derrotas por Tipo"),
                new StringTextComponent("   §7- Invocación de Articuno, Zapdos o Moltres"),
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
