// src/main/java/rl/sage/rangerlevels/limiter/LimiterManager.java
package rl.sage.rangerlevels.limiter;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rl.sage.rangerlevels.capability.LimiterProvider;
import rl.sage.rangerlevels.config.ExpConfig;
import rl.sage.rangerlevels.util.GradientText;
import rl.sage.rangerlevels.util.TimeUtil;

/**
 * Provee acceso centralizado a la configuración del limitador de EXP.
 */
public final class LimiterManager {
    private static final Logger LOGGER = LogManager.getLogger(LimiterManager.class);

    private LimiterManager() {
        // Evitar instanciación
    }

    /** @return true si el limitador está activado en la config */
    public static boolean isEnabled() {
        return ExpConfig.get().isLimiterEnabled();
    }

    /** @return la cantidad máxima de EXP permitida por ventana */
    public static int getMaxExp() {
        return ExpConfig.get().getLimiterExpAmount();
    }

    private static long lastGlobalReset = System.currentTimeMillis() / 1_000L;

    /**
     * @return la duración de la ventana en segundos (parseada desde el campo 'timer' de la config)
     */
    public static long getWindowSeconds() {
        String timer = ExpConfig.get().getLimiterTimer();
        LOGGER.debug("[LimiterManager] raw timer string = '{}'", timer);
        long secs = TimeUtil.parseDuration(timer);
        LOGGER.debug("[LimiterManager] parsed windowSeconds = {}", secs);
        return secs;
    }
    /** Llamar cada tick para checar si ya toca resetear todo */
    public static void checkGlobalReset(MinecraftServer server) {
        long now = System.currentTimeMillis() / 1_000L;
        long windowSec = getWindowSeconds();
        if (windowSec > 0 && now - lastGlobalReset >= windowSec) {
            lastGlobalReset = now;
            // 1) Limpiar cada capability sin broadcast
            server.getPlayerList().getPlayers().forEach(p ->
                    LimiterProvider.get(p).ifPresent(cap -> cap.resetWindowSilent(now))
            );
            // 2) Broadcast único bonito
            IFormattableTextComponent sep = GradientText.of(
                    "══════════════════════════════════",
                    "#FF0000", "#FF7F00", "#FFFF00", "#00FF00", "#0000FF", "#8B00FF"
            );
            IFormattableTextComponent msg = new StringTextComponent(
                    "§7El límite diario de §6EXP§7 ha sido §6RESETEADO§7. ¡A por más niveles!"
            );

            server.getPlayerList().broadcastMessage(sep, ChatType.SYSTEM, Util.NIL_UUID);
            server.getPlayerList().broadcastMessage(msg, ChatType.SYSTEM, Util.NIL_UUID);
            server.getPlayerList().broadcastMessage(sep, ChatType.SYSTEM, Util.NIL_UUID);

            LOGGER.info("[LimiterManager] Reseteo global y broadcast enviado");
        }
    }
}
