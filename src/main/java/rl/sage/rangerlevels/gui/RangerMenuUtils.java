package rl.sage.rangerlevels.gui;

import java.util.UUID;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import rl.sage.rangerlevels.pass.PassManager;
import rl.sage.rangerlevels.pass.PassManager.PassType;
import rl.sage.rangerlevels.util.GradientText;

public class RangerMenuUtils {

    /**
     * Envía al jugador un listado de los pases disponibles:
     * - FREE: solo texto plano
     * - SUPER/ULTRA/MASTER: texto con hover (descripción) y click (comando de compra)
     * - Indica con una flecha el pase actual
     */
    public static void sendComprarPaseMessage(ServerPlayerEntity player) {
        UUID uuid = player.getUUID();

        // Cabecera con gradiente pastel y decoración
        IFormattableTextComponent message = GradientText.of(
                " ❖❖ « Pases Disponibles RangerLevels » ❖❖ ",
                "#A8E6CF", // verde menta pastel
                "#DCEDC2", // lima pastel
                "#FFD3B6"  // salmón pastel
        ).withStyle(Style.EMPTY.withBold(true));
        message.append(new StringTextComponent("\n\n"));

        // Iterate sobre cada tipo de pase
        for (PassType type : PassType.values()) {
            // Línea de presentación del pase con espacio y símbolo
            IFormattableTextComponent line = new StringTextComponent("  ▶ ")
                    .append(type.getGradientDisplayName().copy())
                    .append(new StringTextComponent("\n"));

            // Si no es FREE, agregar hover y click events
            if (type != PassType.FREE) {
                Style style = Style.EMPTY
                        // Hover primario: descripción del pase
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                new StringTextComponent("§e" + type.getDescription() + "§r\n§7Click para abrir")
                        ))
                        // Click: comando para comprar
                        .withClickEvent(new ClickEvent(
                                ClickEvent.Action.RUN_COMMAND,
                                "/rangerlevels pass buy "
                        ));
                line.setStyle(style);
            }
            message.append(line);
        }

        player.sendMessage(message, uuid);
    }
}
