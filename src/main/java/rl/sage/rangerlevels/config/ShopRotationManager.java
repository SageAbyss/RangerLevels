// File: rl/sage/rangerlevels/config/ShopRotationManager.java
package rl.sage.rangerlevels.config;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rl.sage.rangerlevels.util.TimeUtil;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShopRotationManager {
    private static final Logger LOGGER = LogManager.getLogger(ShopRotationManager.class);

    /**
     * Comprueba si ya llegó el momento de rotar; si es así, ejecuta la rotación y actualiza nextRotationEpoch.
     * Debe llamarse periódicamente en el hilo de servidor (p. ej. cada tick).
     */
    public static void rotateIfDue() {
        ShopState state = ShopState.get();
        long now = Instant.now().getEpochSecond();
        long next = state.nextRotationEpoch;
        if (now >= next) {
            doRotationServerThread();
        }
    }

    /**
     * Ejecuta la rotación: nueva selección, limpia compras, recalcula nextRotationEpoch, guarda estado y broadcast.
     * Debe llamarse en hilo de servidor.
     */
    private static synchronized void doRotationServerThread() {
        LOGGER.info(">>> Ejecutando rotación automática en {}", Instant.now());
        // 1) Recarga config y obtiene parámetros
        ShopConfig.reload();
        ShopConfig cfg = ShopConfig.get();
        ShopState state = ShopState.get();

        // 2) Selección aleatoria
        List<String> pool = new ArrayList<>(LegendaryPool.ALL);
        Collections.shuffle(pool);
        int selSize = cfg.rotation.selectionSize;
        state.currentSelection = new ArrayList<>(pool.subList(0, Math.min(selSize, pool.size())));

        // 3) Limpia registro de compras
        state.purchasedPlayers.clear();

        // 4) Recalcula nextRotationEpoch y lastInterval
        String intervalStr = cfg.rotation.interval;
        long intervalSec;
        try {
            intervalSec = TimeUtil.parseDuration(intervalStr);
        } catch (IllegalArgumentException ex) {
            LOGGER.error("Intervalo inválido '{}' en rotación, usando 1d.", intervalStr, ex);
            intervalSec = TimeUtil.parseDuration("1d");
        }
        long now = Instant.now().getEpochSecond();
        state.nextRotationEpoch = now + intervalSec;
        state.lastInterval = intervalStr;

        // 5) Guarda el estado
        ShopState.save();

        // 6) Broadcast con tiempo hasta la siguiente rotación
        long diff = state.nextRotationEpoch - now;
        String timeStr = TimeUtil.formatDuration(Math.max(diff, 0));
        String msg = cfg.messages.rotationReset.replace("%time%", timeStr);
        MinecraftServer srv = ServerLifecycleHooks.getCurrentServer();
        if (srv != null) {
            srv.getPlayerList().broadcastMessage(
                    new StringTextComponent(msg),
                    ChatType.SYSTEM, Util.NIL_UUID
            );
        }

        LOGGER.info("-> Tienda actualizada: {} | Próxima en {}s (epoch {}).",
                state.currentSelection, diff, state.nextRotationEpoch);
    }

    /**
     * Retorna la selección actual.
     */
    public static List<String> getCurrentSelection() {
        return ShopState.get().currentSelection;
    }

    /**
     * Retorna el tiempo restante formateado hasta nextRotationEpoch.
     */
    public static String getTimeRemaining() {
        long now = Instant.now().getEpochSecond();
        long next = ShopState.get().nextRotationEpoch;
        long diff = next - now;
        return TimeUtil.formatDuration(Math.max(diff, 0));
    }
}
