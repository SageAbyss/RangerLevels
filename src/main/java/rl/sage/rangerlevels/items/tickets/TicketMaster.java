package rl.sage.rangerlevels.items.tickets;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.Tier;

import java.util.Arrays;
public class TicketMaster extends RangerItemDefinition {
    public static final String ID = "ticket_master";

    public TicketMaster() {
        super(
                ID,
                PixelmonItems.rainbow_pass,
                Tier.MITICO,                      // Tier Legendario
                Tier.MITICO.getColor(),           // color GOLD (por ejemplo)
                "✦ Ticket Pase Master ✦",              // nombre decorado con Unicode
                Arrays.asList(
                        // 1ª línea: viñeta “✧” + descripción
                        (IFormattableTextComponent) new StringTextComponent(
                                TextFormatting.GRAY + "✧ Usa este Ticket para ventajas de pase Ultra."
                        ),
                        // 2ª línea: viñeta + caducidad
                        (IFormattableTextComponent) new StringTextComponent(
                                TextFormatting.GRAY + "✧ Caduca en §e24 §7horas"
                        ),
                        // 3ª línea: flechas y Tier en su color
                        (IFormattableTextComponent) new StringTextComponent(
                                Tier.MITICO.getColor() +
                                        "§7▶ Tier: " +
                                        Tier.MITICO.getColor() +
                                        Tier.MITICO.getDisplayName()
                        )
                )
        );

        CustomItemRegistry.register(this);
    }
}
