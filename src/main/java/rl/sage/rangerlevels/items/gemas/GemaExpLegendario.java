package rl.sage.rangerlevels.items.gemas;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.Tier;

import java.util.Arrays;

/**
 * Gema de Experiencia LEGENDARIA:
 *  - +50% EXP post-captura/derrota durante 1 hora (3_600_000 ms)
 */
public class GemaExpLegendario extends RangerItemDefinition {
    public static final String ID = "gema_exp_legendario";

    public GemaExpLegendario() {
        super(
                ID,
                Items.EMERALD,               // Ítem base: esmeralda (puedes cambiarlo)
                Tier.LEGENDARIO,             // Tier LEGENDARIO
                Tier.LEGENDARIO.getColor(),  // Color GOLD
                "✦ Gema de Experiencia ✦",
                Arrays.asList(
                        (IFormattableTextComponent) new StringTextComponent(
                                TextFormatting.GRAY + "✧ Otorga +50% de EXP al capturar/derrotar Pokémon"
                        ),
                        (IFormattableTextComponent) new StringTextComponent(
                                TextFormatting.GRAY + "✧ Dura 1 hora"
                        ),
                        (IFormattableTextComponent) new StringTextComponent(
                                Tier.LEGENDARIO.getColor() + "§7▶ Tier: " + Tier.LEGENDARIO.getColor() + Tier.LEGENDARIO.getDisplayName()
                        )
                )
        );

        CustomItemRegistry.register(this);
    }
}
