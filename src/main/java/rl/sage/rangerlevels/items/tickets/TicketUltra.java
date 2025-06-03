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
 * Define todo lo que identifica al “ticket_ultra”:
 *  - id = "ticket_ultra"
 *  - baseItem = PixelmonItems.ss_ticket_ultra (la Ultra Ball de Pixelmon)
 *  - tier = Tier.LEGENDARIO
 *  - tierColor = Tier.LEGENDARIO.getColor()
 *  - displayName = "✦ Ticket Pase Ultra ✦"
 *  - defaultLore:
 *      • Líneas con “✧” como viñetas
 *      • Incluye “▶ Tier: Legendario ◀” en su color
 *
 * Se registra en CustomItemRegistry en el constructor.
 */
public class TicketUltra extends RangerItemDefinition {
    public static final String ID = "ticket_ultra";

    public TicketUltra() {
        super(
                ID,
                PixelmonItems.rainbow_pass,
                Tier.LEGENDARIO,                      // Tier Legendario
                Tier.LEGENDARIO.getColor(),           // color GOLD (por ejemplo)
                "✦ Ticket Pase Ultra ✦",              // nombre decorado con Unicode
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
                                Tier.LEGENDARIO.getColor() +
                                        "§7▶ Tier: " +
                                        Tier.LEGENDARIO.getColor() +
                                        Tier.LEGENDARIO.getDisplayName()
                        )
                )
        );

        CustomItemRegistry.register(this);
    }
}
