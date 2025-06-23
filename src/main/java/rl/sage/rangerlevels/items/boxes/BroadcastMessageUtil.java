// File: rl/sage/rangerlevels/items/boxes/BroadcastMessageUtil.java
package rl.sage.rangerlevels.items.boxes;

import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import rl.sage.rangerlevels.util.GradientText;

public class BroadcastMessageUtil {
    private static final String[] TEMPLATES = {
            "☄ %s ha recibido una Caja Misteriosa del espacio!",
            "☄ Se estrelló una Caja Misteriosa cerca de %s!",
            "☄ Meteoro detectado cerca de %s, buscalo!",
            "☄ ¡Un Meteoro impactó cerca de %s!",
            "☄ El universo envió una Caja Misteriosa a %s",
            "☄ ¡Impacto galáctico! Una Caja Misteriosa aterrizó cerca de %s",
            "☄ De las estrellas cayó una Caja Misteriosa para %s"
    };
    private static final ThreadLocalRandom RAND = ThreadLocalRandom.current();

    /**
     * Construye el componente de broadcast con:
     *  - Línea separadora
     *  - Mensaje random mencionando al jugador
     *  - Línea con coordenadas en formato [x, y, z]
     *  - Línea separadora
     */
    public static IFormattableTextComponent getBroadcast(ServerPlayerEntity player, BlockPos pos) {
        String sep = TextFormatting.DARK_GRAY + "" + TextFormatting.STRIKETHROUGH
                + "──────────────────────────────────\n";
        // elegir y formatear plantilla
        String template = TEMPLATES[RAND.nextInt(TEMPLATES.length)];
        String msgLine = String.format(template, player.getName().getString());
        // mensaje con gradiente
        IFormattableTextComponent comp = new StringTextComponent(sep)
                .append(GradientText.of(msgLine + "\n", "#FF7F50", "#FFD700"))
                .append(new StringTextComponent(TextFormatting.GRAY
                        + String.format("                                   [%d, %d, %d]\n", pos.getX(), pos.getY(), pos.getZ())))
                .append(new StringTextComponent(sep));
        return comp;
    }

    /** Envía el broadcast de forma directa */
    public static void broadcastToAll(ServerPlayerEntity anyPlayer, IFormattableTextComponent comp) {
        Objects.requireNonNull(anyPlayer.getServer()).getPlayerList()
                .broadcastMessage(comp, ChatType.SYSTEM, Util.NIL_UUID);
    }
}
