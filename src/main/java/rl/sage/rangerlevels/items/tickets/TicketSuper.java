package rl.sage.rangerlevels.items.tickets;

import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.Tier;

import java.util.Arrays;

/**
 * Ahora el “ticket_super” usará como base una Ultra Ball de Pixelmon, en lugar de un mapa vanilla.
 */
public class TicketSuper extends RangerItemDefinition {
    public static final String ID = "ticket_super";

    public TicketSuper() {
        super(
                ID,
                PixelmonItems.rainbow_pass,
                Tier.EPICO,
                Tier.EPICO.getColor(),
                "✦ Ticket Pase Super ✦",
                Arrays.asList(
                        (IFormattableTextComponent) new StringTextComponent(
                                TextFormatting.GRAY + "✧ Usa este Ticket para ventajas de pase."
                        ),
                        (IFormattableTextComponent) new StringTextComponent(
                                TextFormatting.GRAY + "✧ Caduca en §e24 §7horas"
                        ),
                        (IFormattableTextComponent) new StringTextComponent(
                                Tier.EPICO.getColor() + "§7▶ Tier: " + Tier.EPICO.getColor() + Tier.EPICO.getDisplayName()
                        )
                )
        );

        CustomItemRegistry.register(this);
    }
}
