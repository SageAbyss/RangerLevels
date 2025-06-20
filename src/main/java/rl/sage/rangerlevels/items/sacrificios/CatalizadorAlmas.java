// File: rl/sage/rangerlevels/items/sacrificios/CatalizadorAlmas.java
package rl.sage.rangerlevels.items.sacrificios;

import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.Tier;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.util.EnchantUtils;
import rl.sage.rangerlevels.util.NBTUtils;

import java.util.Arrays;
import java.util.List;

public class CatalizadorAlmas extends RangerItemDefinition {
    public static final String ID = "catalizador_almas";

    public CatalizadorAlmas() {
        super(
                ID,
                PixelmonItems.dna_splicers,      // base Pixelmon: dna_splicers
                Tier.MITICO,                     // tier mítico
                TextFormatting.DARK_PURPLE,      // color del nombre
                "✦ Catalizador de Almas ✦",      // nombre visible
                getDefaultLore()                 // lore completo y dinámico
        );
        CustomItemRegistry.register(this);
    }

    /** Lore descriptivo */
    private static List<IFormattableTextComponent> getDefaultLore() {
        return Arrays.asList(
                new StringTextComponent("§7✧ Un artefacto forjado con la esencia ancestral."),
                new StringTextComponent("§7✧ Haz clic derecho sobre un Pokémon Legendario"),
                new StringTextComponent("§7   o Ultraente de tu equipo para sacrificarlo."),
                new StringTextComponent(" "),
                new StringTextComponent("§7✧ El sacrificio extrae la Esencia específica"),
                new StringTextComponent("§7   que luego sirve para crear modificadores"),
                new StringTextComponent("§7   de ADN únicos para ese Pokémon."),
                new StringTextComponent(" "),
                new StringTextComponent("§7▶ Tier: ").append(Tier.MITICO.getColor())
        );
    }

    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(amount);
        EnchantUtils.addEnchantment(stack, Enchantments.UNBREAKING, 1);
        // Oculta atributos extra
        NBTUtils.applyAllHideFlags(stack.getOrCreateTag());
        return stack;
    }
}
