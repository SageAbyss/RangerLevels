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
 * Tótem de Raíz Primordial: Tier MITICO.
 * - Resistencia II y Suerte II mientras esté en inventario u offhand.
 * - Mismos tipos afectados que el Fragmento de Corazón de Gaia.
 * - 50% chance de aumentar en 10% los IVs al capturar Pokémon de tipos afectados.
 * - 5% chance de hacer shiny al capturar Pokémon de tipos afectados.
 * - El Pokémon capturado se hace Microscopic o Ginormous aleatoriamente (siempre).
 * - Bonus de +30% EXP en combates y capturas de Pokémon de tipos afectados.
 */
public class TotemRaizPrimordial extends RangerItemDefinition {
    public static final String ID = "totem_raiz_primordial";

    public TotemRaizPrimordial() {
        super(
                ID,
                PixelmonItems.smooth_rock,  // Ejemplo de ítem base; ajustar según prefieras
                Tier.MITICO,
                null,
                "✦ Tótem de Raíz Primordial ✦",
                null  // lore por defecto vacío; lo añadimos en createStack
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(amount);
        // 1) Nombre con degradado DE CLASE UTIL
        stack.setHoverName(GradientText.of(getDisplayName(), "#FF5151","#FFC16F")
                .withStyle(style -> style.withItalic(false)));

        // 2) Lore con descripción de efectos
        List<IFormattableTextComponent> lore = Arrays.asList(
                new StringTextComponent("§7✧ Tipos Afectados: Bicho, Tierra, Roca, Normal, Eléctrico, Planta"),
                new StringTextComponent("§7✧ Inmunidad al daño de caída"),
                new StringTextComponent("§7✧ Captura de Tipos:"),
                new StringTextComponent("   §7- 50% probabilidad de +5-12% IVs"),
                new StringTextComponent("   §7-  5% probabilidad de convertir en Shiny"),
                new StringTextComponent("   §7- Gracias al poder del Totem, el tamaño cambia"),
                new StringTextComponent("§7✧ Bonus de §6+30%§7 EXP en:"),
                new StringTextComponent("   §7- Capturas por Tipo"),
                new StringTextComponent("   §7- Derrotas por Tipo"),
                new StringTextComponent("   §7- Invocar a Dialga, Palkia o Giratina"),
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
        EnchantUtils.addEnchantment(stack, Enchantments.UNBREAKING, 1);
        // 3) Ocultar atributos y demás
        NBTUtils.applyAllHideFlags(tag);
        stack.setTag(tag);
        return stack;
    }
}
