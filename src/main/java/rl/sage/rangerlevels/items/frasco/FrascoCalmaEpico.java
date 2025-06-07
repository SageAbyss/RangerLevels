// File: rl/sage/rangerlevels/items/frasco/FrascoCalmaEpico.java
package rl.sage.rangerlevels.items.frasco;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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

/**
 * Frasco de Calma - Tier Épico:
 *   - Reduce un 30% la tasa de huida de Pokémon salvajes durante 30 minutos.
 *   - Botella teñida de color morado (hex #800080).
 *   - Añade encantamiento Unbreaking I, escondido con HideFlags.
 */
public class FrascoCalmaEpico extends RangerItemDefinition {
    public static final String ID = "frasco_calma_epico";
    private static final int DURACION_MS = 30 * 60_000; // 30 minutos
    private static final double REDUCCION = 0.30;       // 30%

    public FrascoCalmaEpico() {
        super(
                ID,
                Items.POTION,
                Tier.EPICO,   // Tier ÉPICO
                null,         // No pasamos color sólido; usamos degradado en createStack()
                "✦ Frasco de Calma Épico ✦",
                null          // Lore se asigna en createStack()
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        // 1) Creamos el ItemStack base
        ItemStack stack = super.createStack(amount);

        // 2) Asignamos el hover-name con degradado pastel de Tier.EPICO
        stack.setHoverName(Tier.EPICO.applyGradient(getDisplayName()));

        // 3) Generamos el lore con la línea de Tier en degradado
        List<IFormattableTextComponent> generatedLore = Arrays.asList(
                // Viñeta “✧” + descripción de efecto
                new StringTextComponent("§7✧ Reduce un 30% la fuga de Pokémon salvajes"),
                // Viñeta “✧” + duración
                new StringTextComponent("§7✧ Dura 30 minutos"),
                // “▶ Tier:” en gris + “ÉPICO” en degradado pastel
                new StringTextComponent("§7▶ Tier: ").append(Tier.EPICO.getColor())
        );

        // 4) Insertamos el lore en NBT
        CompoundNBT tag = stack.getOrCreateTag();
        CompoundNBT display = tag.contains("display")
                ? tag.getCompound("display")
                : new CompoundNBT();
        ListNBT loreList = new ListNBT();
        for (IFormattableTextComponent line : generatedLore) {
            String json = IFormattableTextComponent.Serializer.toJson(line);
            loreList.add(StringNBT.valueOf(json));
        }
        display.put("Lore", loreList);
        tag.put("display", display);

        // 5) Aplicar tinte morado al contenido de la 'poción' (botella)
        tag.putInt("CustomPotionColor", 0x800080);

        // 6) Encantamientos por Helper
        EnchantUtils.addEnchantment(stack, Enchantments.UNBREAKING, 1);

        // 7) Ocultar atributos y encantamientos con HideFlags:
        NBTUtils.applyAllHideFlags(tag);

        stack.setTag(tag);
        return stack;
    }

    public static double getReduccion() {
        return REDUCCION;
    }

    public static int getDuracionMs() {
        return DURACION_MS;
    }
}
