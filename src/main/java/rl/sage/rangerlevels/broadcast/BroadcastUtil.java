package rl.sage.rangerlevels.broadcast;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.IFormattableTextComponent;
import rl.sage.rangerlevels.util.TextFormatterUtil;

import java.util.List;

public class BroadcastUtil {

    /**
     * Envía por chat SYSTEM a todos los jugadores las líneas dadas,
     * reemplazando variables y parseando colores/gradientes.
     *
     * @param server  el servidor
     * @param rawLines  líneas con placeholders (%PLAYER%, %LEVEL%) y códigos de color
     * @param playerName nombre a sustituir en %PLAYER%
     * @param level      valor a sustituir en %LEVEL%
     */
    public static void broadcastMaxLevel(
            MinecraftServer server,
            List<String> rawLines,
            String playerName,
            int level
    ) {
        for (String line : rawLines) {
            // 1) Sustituye variables
            String withVars = line
                    .replace("%PLAYER%", playerName)
                    .replace("%LEVEL%", String.valueOf(level));

            // 2) Parseo de colores y gradientes
            IFormattableTextComponent comp = TextFormatterUtil.parse(withVars);

            // 3) Envío
            server.getPlayerList()
                    .broadcastMessage(comp, ChatType.SYSTEM, Util.NIL_UUID);
        }
    }
}
