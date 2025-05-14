// src/main/java/rl/sage/rangerlevels/capability/LimiterCapability.java
package rl.sage.rangerlevels.capability;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import rl.sage.rangerlevels.limiter.LimiterManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LimiterCapability implements ILimiter {
    private static final Logger LOGGER = LogManager.getLogger();

    private long windowStart;
    private int accumulatedExp;
    private boolean notified;

    public LimiterCapability() {
        this.windowStart    = currentSecond();
        this.accumulatedExp = 0;
        this.notified       = false;
    }

    @Override
    public long getWindowStart() {
        return windowStart;
    }

    @Override
    public void setWindowStart(long ts) {
        this.windowStart = ts;
    }

    @Override
    public int getAccumulatedExp() {
        return accumulatedExp;
    }

    @Override
    public void setAccumulatedExp(int exp) {
        this.accumulatedExp = exp;
    }

    @Override
    public void resetWindow(long newStartTimestamp) {
        this.windowStart    = newStartTimestamp;
        this.accumulatedExp = 0;
        this.notified       = false;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            IFormattableTextComponent sep = new StringTextComponent("§7———————————————————————————————");
            IFormattableTextComponent msg = new StringTextComponent("§6§l¡El límite de EXP diario fue reseteado!");
            server.getPlayerList().broadcastMessage(sep, ChatType.SYSTEM, Util.NIL_UUID);
            server.getPlayerList().broadcastMessage(msg, ChatType.SYSTEM, Util.NIL_UUID);
            server.getPlayerList().broadcastMessage(sep, ChatType.SYSTEM, Util.NIL_UUID);
        }

        LOGGER.info("[Limiter] Ventana reiniciada y broadcast enviado");
    }

    @Override
    public int addExp(int amount, int maxAllowed) {
        long now       = currentSecond();
        long windowSec = LimiterManager.getWindowSeconds();

        LOGGER.debug("[Limiter] windowSec = {}", windowSec);
        LOGGER.debug("[Limiter] now = {}, windowStart = {}, delta = {}", now, windowStart, now - windowStart);

        if (windowSec > 0 && now - windowStart >= windowSec) {
            resetWindow(now);
        }

        int remaining = maxAllowed - accumulatedExp;
        if (remaining <= 0) {
            LOGGER.debug("[Limiter] Límite diario alcanzado ({} exp)", maxAllowed);
            return 0;
        }

        int granted = Math.min(amount, remaining);
        this.accumulatedExp += granted;
        LOGGER.debug("[Limiter] Otorgando {} exp (acumulado ahora {})", granted, accumulatedExp);
        return granted;
    }

    @Override
    public boolean wasNotified() {
        return notified;
    }

    @Override
    public void setNotified(boolean notified) {
        this.notified = notified;
    }

    private static long currentSecond() {
        return System.currentTimeMillis() / 1_000L;
    }
}
