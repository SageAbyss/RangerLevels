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
 * Gema de Experiencia ÉPICO:
 *  - +30% EXP post-captura/derrota durante 30 minutos (1_800_000 ms)
 */
public class GemaExpEpico extends RangerItemDefinition {
    public static final String ID = "gema_exp_epico";

    public GemaExpEpico() {
        super(
                ID,
                Items.EMERALD,               // Ítem base: esmeralda (puedes cambiarlo)
                Tier.EPICO,                  // Tier ÉPICO
                Tier.EPICO.getColor(),       // Color AQUA
                "✦ Gema de Experiencia ✦",
                Arrays.asList(
                        (IFormattableTextComponent) new StringTextComponent(
                                TextFormatting.GRAY + "✧ Otorga +30% de EXP al capturar/derrotar Pokémon"
                        ),
                        (IFormattableTextComponent) new StringTextComponent(
                                TextFormatting.GRAY + "✧ Dura 30 minutos"
                        ),
                        (IFormattableTextComponent) new StringTextComponent(
                                Tier.EPICO.getColor() + "§7▶ Tier: " + Tier.EPICO.getColor() + Tier.EPICO.getDisplayName()
                        )
                )
        );

        CustomItemRegistry.register(this);
    }
}
