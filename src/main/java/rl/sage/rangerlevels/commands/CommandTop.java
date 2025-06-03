package rl.sage.rangerlevels.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.database.FlatFilePlayerDataManager;
import rl.sage.rangerlevels.database.PlayerData;
import rl.sage.rangerlevels.util.GradientText;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Comando /rlv top: lista los 10 jugadores con MÁS nivel (y, en caso de empate, más exp).
 * Usa el mismo diseño Unicode anterior (doble borde) pero con colores más suaves
 * y un ancho reducido para evitar que “rebose” en pantalla.
 */
public class CommandTop {

    public static int top(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        // 1) Cargamos el JSON más reciente desde el DataManager
        FlatFilePlayerDataManager manager =
                (FlatFilePlayerDataManager) RangerLevels.INSTANCE.getDataManager();
        manager.loadAll(); // Asegúrate de que esto lee el archivo Data.json dentro de tu carpeta de config

        // 2) Obtenemos todos los PlayerData y los almacenamos en una lista
        List<PlayerData> todos = new ArrayList<>(manager.getAllData());
        if (todos.isEmpty()) {
            ctx.getSource().sendSuccess(
                    new StringTextComponent(TextFormatting.RED + "No hay datos de jugadores en Data.json"),
                    false
            );
            return 1;
        }

        // === BLOQUE CORREGIDO ===
        // Ahora ordenamos correctamente:
        //  - primero comparamos nivel (descendente)
        //  - en caso de empate, comparamos experiencia (descendente)
        // Para ello, invertimos CADA comparador por separado.
        List<PlayerData> top10 = todos.stream()
                .sorted(Comparator
                        .comparingInt(PlayerData::getLevel).reversed()                                // Nivel DESC
                        .thenComparing(
                                Comparator.comparingDouble(PlayerData::getExp).reversed()                // Exp DESC
                        )
                )
                .limit(10)
                .collect(Collectors.toList());
        // =========================

        // 3) Calculamos anchos dinámicos, pero restringimos a un máximo razonable
        int maxNameLen = top10.stream()
                .mapToInt(pd -> pd.getNickname().length())
                .max()
                .orElse(10);
        // No permitimos nombres demasiado largos: máximo 20 caracteres
        if (maxNameLen > 20) maxNameLen = 20;

        int nivelFieldWidth = 2;  // asumimos niveles hasta 99
        int expFieldWidth = top10.stream()
                .mapToInt(pd -> String.valueOf((int) pd.getExp()).length())
                .max()
                .orElse(4);
        if (expFieldWidth > 6) expFieldWidth = 6; // restringimos experiencia a 6 dígitos

        // Espacios fijos en cada línea:
        // " 1 ▸ " (4 chrs con espacio inicial)
        // nombre padded a maxNameLen
        // " ┇Lv:" + nivelFieldWidth + " Exp:" + expFieldWidth + " " (espacio final)
        int contentWidth = 4 + maxNameLen + 1
                + 3 + nivelFieldWidth + 5 + expFieldWidth + 1;
        //   4 = " 1 ▸"
        //   maxNameLen = longitud del nickname
        //   1 = espacio antes de stats
        //   3 = "┇Lv"
        //   nivelFieldWidth = dígitos nivel
        //   5 = " Exp:"
        //   expFieldWidth = dígitos experiencia
        //   1 = espacio final
        // Total sin bordes

        int totalWidth = contentWidth + 2; // sumamos 2 para los bordes “╔” y “╗”
        // Ajustamos a múltiplo de 2 para simetría
        if (totalWidth % 2 != 0) totalWidth++;

        // 4a) Línea superior (borde doble, color gris oscuro)
        StringBuilder topBorder = new StringBuilder();
        topBorder.append(TextFormatting.DARK_GRAY).append("╔");
        for (int i = 0; i < contentWidth; i++) topBorder.append("═");
        topBorder.append("╗");
        ctx.getSource().sendSuccess(new StringTextComponent(topBorder.toString()), false);

        // 4b) Encabezado con gradiente y PREFIX (centrado dentro del ancho)
        String titleOnly = " " + RangerLevels.PREFIX.getString() + " Top 10 ";
        IFormattableTextComponent gradientTitle = GradientText.of(
                titleOnly, "#C397F1", "#A4DDE1", "#A671BD"
        );

        int available = contentWidth - titleOnly.length();
        int padLeft = available / 2;
        int padRight = available - padLeft;

        IFormattableTextComponent headerLine = new StringTextComponent(TextFormatting.DARK_GRAY + "║");
        for (int i = 0; i < padLeft; i++) {
            headerLine.append(new StringTextComponent(" "));
        }
        headerLine.append(gradientTitle);
        for (int i = 0; i < padRight; i++) {
            headerLine.append(new StringTextComponent(" "));
        }
        headerLine.append(new StringTextComponent(TextFormatting.DARK_GRAY + "║"));
        ctx.getSource().sendSuccess(headerLine, false);

        // 4c) División entre título y tabla (borde sencillo, gris oscuro)
        StringBuilder midBorder = new StringBuilder();
        midBorder.append(TextFormatting.DARK_GRAY).append("╟");
        for (int i = 0; i < contentWidth; i++) midBorder.append("─");
        midBorder.append("╢");
        ctx.getSource().sendSuccess(new StringTextComponent(midBorder.toString()), false);

        // 4d) Fila vacía (solo bordes y espacios)
        StringBuilder emptyRowBuilder = new StringBuilder();
        emptyRowBuilder.append(TextFormatting.DARK_GRAY).append("║");
        for (int i = 0; i < contentWidth; i++) emptyRowBuilder.append(" ");
        emptyRowBuilder.append(TextFormatting.DARK_GRAY).append("║");
        String emptyRow = emptyRowBuilder.toString();
        ctx.getSource().sendSuccess(new StringTextComponent(emptyRow), false);

        // 5) Filas del Top 10
        int rank = 1;
        for (PlayerData pd : top10) {
            String rawName = pd.getNickname();
            // Si el nombre es más largo que maxNameLen, lo recortamos y agregamos "…"
            String nick = rawName.length() > maxNameLen
                    ? rawName.substring(0, maxNameLen - 1) + "…"
                    : rawName;

            int level = pd.getLevel();
            int exp = (int) pd.getExp();

            // 5a) Construimos la línea de estadística
            StringBuilder row = new StringBuilder();
            row.append(TextFormatting.DARK_GRAY).append("║").append(TextFormatting.RESET).append(" ");

            // Rango en dorado
            String rankStr = rank < 10 ? " " + rank : String.valueOf(rank);
            row.append(TextFormatting.GOLD).append(rankStr).append(TextFormatting.RESET);
            row.append(" ").append("▸").append(" ");

            // Nombre en aqua
            row.append(TextFormatting.AQUA).append(nick).append(TextFormatting.RESET);
            for (int i = nick.length(); i < maxNameLen; i++) row.append(" ");

            row.append(" "); // espacio antes de stats

            // Estadísticas en blanco/amarillo/gris
            String lvlStr = String.valueOf(level);
            String expStr = String.valueOf(exp);
            row.append(TextFormatting.GRAY).append("┇Lv:").append(TextFormatting.WHITE);
            for (int i = lvlStr.length(); i < nivelFieldWidth; i++) row.append(" ");
            row.append(lvlStr).append(TextFormatting.GRAY).append(" Exp:").append(TextFormatting.WHITE);
            for (int i = expStr.length(); i < expFieldWidth; i++) row.append(" ");
            row.append(expStr).append(TextFormatting.RESET).append(" ");

            // Rellenar con espacios si hace falta
            int currentLen = stripColorCodes(row.toString()).length() - 2;
            int needed = contentWidth - currentLen;
            for (int i = 0; i < needed; i++) row.append(" ");

            // Cierre de borde
            row.append(TextFormatting.DARK_GRAY).append("║");

            ctx.getSource().sendSuccess(new StringTextComponent(row.toString()), false);
            rank++;
        }

        // 4e) Fila vacía antes del borde inferior
        ctx.getSource().sendSuccess(new StringTextComponent(emptyRow), false);

        // 4f) Borde inferior (doble línea, gris oscuro)
        StringBuilder bottomBorder = new StringBuilder();
        bottomBorder.append(TextFormatting.DARK_GRAY).append("╚");
        for (int i = 0; i < contentWidth; i++) bottomBorder.append("═");
        bottomBorder.append("╝");
        ctx.getSource().sendSuccess(new StringTextComponent(bottomBorder.toString()), false);

        try {
            ServerPlayerEntity ejecutor = ctx.getSource().getPlayerOrException();
            // Reproducir sonido al jugador que ejecuta
            playSound(ejecutor);
        } catch (Exception e) {
            // Si lo ejecuta la consola, getPlayerOrException() lanza excepción; la ignoramos
        }

        return 1;
    }

    private static void playSound(ServerPlayerEntity player) {
        PlayerSoundUtils.playSoundToPlayer(
                player,
                SoundEvents.NOTE_BLOCK_CHIME,
                SoundCategory.MASTER,
                1.0f,
                0.8f
        );
    }

    /**
     * Elimina códigos de color (p. ej. "§a", "§b", etc.) para medir longitud real del texto.
     */
    private static String stripColorCodes(String s) {
        return s.replaceAll("(?i)§[0-9A-FK-OR]", "");
    }
}
