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
 * Gema de Experiencia COMÚN:
 *  - +10% EXP post-captura/derrota durante 15 minutos (900_000 ms)
 */
public class GemaExpComun extends RangerItemDefinition {
    public static final String ID = "gema_exp_comun";

    public GemaExpComun() {
        super(
                ID,
                Items.EMERALD,               // Ítem base: esmeralda (puedes cambiarlo)
                Tier.COMUN,                  // Tier COMUN
                Tier.COMUN.getColor(),       // Color blanco
                "✦ Gema de Experiencia ✦",    // Nombre visible
                Arrays.asList(
                        (IFormattableTextComponent) new StringTextComponent(
                                TextFormatting.GRAY + "✧ Otorga +10% de EXP al capturar/derrotar Pokémon"
                        ),
                        (IFormattableTextComponent) new StringTextComponent(
                                TextFormatting.GRAY + "✧ Dura 15 minutos"
                        ),
                        (IFormattableTextComponent) new StringTextComponent(
                                Tier.COMUN.getColor() + "§7▶ Tier: " + Tier.COMUN.getColor() + Tier.COMUN.getDisplayName()
                        )
                )
        );

        CustomItemRegistry.register(this);
    }
}
