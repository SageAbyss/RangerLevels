// File: rl/sage/rangerlevels/items/frasco/FrascoCalmaEstelar.java
package rl.sage.rangerlevels.items.frasco;

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

import java.util.Arrays;
import java.util.List;

/**
 * Frasco de Calma - Tier Estelar:
 *   - Reduce un 50% la tasa de huida de Pokémon salvajes durante 60 minutos.
 *   - Botella teñida de color azul oscuro (hex #00AAAA).
 *   - Añade encantamiento Unbreaking I, escondido con HideFlags.
 */
public class FrascoCalmaEstelar extends RangerItemDefinition {
    public static final String ID = "frasco_calma_estelar";
    private static final int DURACION_MS = 60 * 60_000; // 60 minutos
    private static final double REDUCCION = 0.50;       // 50%

    public FrascoCalmaEstelar() {
        super(
                ID,
                Items.POTION,
                Tier.ESTELAR,   // Tier ESTELAR
                null,           // No pasamos color sólido; usamos degradado en createStack()
                "✦ Frasco de Calma Estelar ✦",
                null            // Lore se asigna en createStack()
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        // 1) Creamos el ItemStack base
        ItemStack stack = super.createStack(amount);

        // 2) Asignamos el hover-name con degradado pastel de Tier.ESTELAR
        stack.setHoverName(Tier.ESTELAR.applyGradient(getDisplayName()));

        // 3) Generamos el lore con la línea de Tier en degradado
        List<IFormattableTextComponent> generatedLore = Arrays.asList(
                // Viñeta “✧” + descripción de efecto
                new StringTextComponent("§7✧ Reduce un 50% la fuga de Pokémon salvajes"),
                // Viñeta “✧” + duración
                new StringTextComponent("§7✧ Dura 60 minutos"),
                // “▶ Tier:” en gris + “ESTELAR” en degradado pastel
                new StringTextComponent("§7▶ Tier: ").append(Tier.ESTELAR.getColor())
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

        // 5) Aplicar tinte azul oscuro al contenido de la 'poción' (botella)
        tag.putInt("CustomPotionColor", 0x00AAAA);

        // 6) Añadir encantamiento Unbreaking I (oculto)
        ListNBT enchList = new ListNBT();
        CompoundNBT ench = new CompoundNBT();
        ench.putString("id", "minecraft:unbreaking");
        ench.putShort("lvl", (short) 1);
        enchList.add(ench);
        tag.put("Enchantments", enchList);

        // 7) Ocultar atributos y encantamientos con HideFlags:
        //    - bit 1 (encantamientos)
        //    - bit 32 (potion effects o custom potion color)
        int hide = tag.contains("HideFlags") ? tag.getInt("HideFlags") : 0;
        hide |= 1;   // oculta encantamientos
        hide |= 32;  // oculta color de poción
        tag.putInt("HideFlags", hide);

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
