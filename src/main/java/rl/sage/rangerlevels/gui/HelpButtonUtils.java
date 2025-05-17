package rl.sage.rangerlevels.gui;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.server.ServerWorld;
import rl.sage.rangerlevels.config.ExpConfig;
import rl.sage.rangerlevels.purge.PurgeData;
import rl.sage.rangerlevels.util.GradientText;

import java.util.Map;

public class HelpButtonUtils {

    /**
     * Envía al jugador el menú de ayuda con botones que muestran información al pasar el cursor.
     */
    public static void sendHelpMenu(ServerPlayerEntity player) {
        // Título con gradiente pastel y decoración
        IFormattableTextComponent msg = GradientText.of(
                " ◎ Menú de Ayuda RangerLevels ◎ ",
                "#90E96D", // pastel rosa
                "#D5DB4F", // pastel melocotón
                "#E5B157"  // pastel lavanda
        ).withStyle(Style.EMPTY.withBold(false));
        msg.append(new StringTextComponent("\n\n"));

        // Lista vertical de "botones" hover
        msg.append(makeHoverLine(
                "§f§l✦ Eventos Activos",
                buildEventosHover()
        ));
        msg.append(new StringTextComponent("\n"));

        msg.append(makeHoverLine(
                "§l✧ Cómo comprar el pase",
                buildCompraHover()
        ));
        msg.append(new StringTextComponent("\n"));

        msg.append(makeHoverLine(
                "§f§l✦ Nivel Máximo Actual",
                new StringTextComponent("§7Nivel Máximo: §f" + ExpConfig.get().getMaxLevel())
        ));
        msg.append(new StringTextComponent("\n"));

        msg.append(makeHoverLine(
                "§l✧ Próximo Reinicio del pase",
                buildReinicioHover(player)
        ));
        msg.append(new StringTextComponent("\n"));

        msg.append(makeHoverLine(
                "§f§l✦ Limitador Activo",
                buildLimiterHover(player)
        ));
        msg.append(new StringTextComponent("\n"));

        player.sendMessage(msg, player.getUUID());
    }

    /** Crea una línea con texto con hover, sin acción de click. */
    private static IFormattableTextComponent makeHoverLine(String label, ITextComponent hoverContent) {
        return new StringTextComponent(label)
                .withStyle(Style.EMPTY
                        .withColor(TextFormatting.YELLOW)
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                hoverContent
                        ))
                );
    }

    /** Construye el componente con la lista de eventos y multiplicadores. */
    private static ITextComponent buildEventosHover() {
        IFormattableTextComponent hover = new StringTextComponent("§dEventos Activos y multiplicadores:\n");
        for (Map.Entry<String, Float> e : ExpConfig.get().multipliers.events.entrySet()) {
            hover.append(new StringTextComponent(" §7» §f" + e.getKey() + ": §b" + e.getValue() + "x\n"));
        }
        return hover;
    }

    /** Construye el componente con el mini tutorial de compra. */
    private static ITextComponent buildCompraHover() {
        IFormattableTextComponent hover = new StringTextComponent("");
        hover.append(GradientText.of(" ▶ Tutorial de Compra ◀ ", "#FFB3BA", "#FFDFBA", "#FFFFBA"))
                .append(new StringTextComponent("\n"));
        hover.append(new StringTextComponent("(1) §7Entra al enlace del pase que quieras comprar\n"));
        hover.append(new StringTextComponent("(2) §7Sigue las instrucciones de compra de la página\n"));
        hover.append(new StringTextComponent("(3) §7En menos de 10 minutos recibirás el pase\n"));
        hover.append(new StringTextComponent("(4) §7Revisa tu pase con /rlv pass info"));
        return hover;
    }

    /** Construye el componente con el tiempo restante hasta el reinicio del pase. */
    private static ITextComponent buildReinicioHover(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.level;
        PurgeData data = PurgeData.get(world);
        long seconds = data.getRemainingSeconds();
        String formatted = formatDuration(seconds);
        return new StringTextComponent("§7Tiempo restante: §f" + formatted);
    }

    /** Construye el componente con el tiempo restante hasta el próximo reset del limitador. */
    private static ITextComponent buildLimiterHover(ServerPlayerEntity player) {
        ExpConfig.Limiter lim = ExpConfig.get().limiter;
        if (!lim.enable) {
            return new StringTextComponent("§7Limitador Desactivado");
        }
        // Calcula tiempo hasta el próximo reset basado en world time y timer config (ej. "24h")
        ServerWorld world = (ServerWorld) player.level;
        // Tiempo de mundo en segundos
        long currentSec = world.getGameTime() / 20;
        // Parse timer, espera formato "Nh"
        String t = lim.timer.toLowerCase().replace("h", "");
        long hours = 24;
        try { hours = Long.parseLong(t); } catch (Exception ex) { /*fallback*/ }
        long resetInterval = hours * 3600;
        long secondsToReset = resetInterval - (currentSec % resetInterval);
        String formatted = formatDuration(secondsToReset);

        IFormattableTextComponent hover = new StringTextComponent("§7Diario: §f" + lim.expAmount + " §7EXP\n");
        hover.append(new StringTextComponent("§7Reset en: §f" + formatted));
        return hover;
    }

    /** Formatea segundos a Días, Horas, Minutos y Segundos. */
    private static String formatDuration(long totalSeconds) {
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0 || days > 0) sb.append(hours).append("h ");
        if (minutes > 0 || hours > 0 || days > 0) sb.append(minutes).append("m ");
        sb.append(seconds).append("s");
        return sb.toString();
    }
}
