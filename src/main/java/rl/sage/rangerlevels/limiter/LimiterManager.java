// src/main/java/rl/sage/rangerlevels/limiter/LimiterManager.java
package rl.sage.rangerlevels.limiter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rl.sage.rangerlevels.config.ExpConfig;
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
}
