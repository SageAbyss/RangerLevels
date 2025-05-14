package rl.sage.rangerlevels.config;

import java.util.HashMap;
import java.util.Map;

public class EarnExpConfig {
    /** Clave ⇒ configuración de cada evento Pixelmon */
    private Map<String, EventConfig> pixelmonEvents = new HashMap<>();
    /** Clave ⇒ configuración de cada evento Minecraft */
    private Map<String, EventConfig> minecraftEvents = new HashMap<>();

    public Map<String, EventConfig> getPixelmonEvents() {
        return pixelmonEvents;
    }

    public void setPixelmonEvents(Map<String, EventConfig> pixelmonEvents) {
        this.pixelmonEvents = pixelmonEvents != null ? pixelmonEvents : new HashMap<>();
    }

    public Map<String, EventConfig> getMinecraftEvents() {
        return minecraftEvents;
    }

    public void setMinecraftEvents(Map<String, EventConfig> minecraftEvents) {
        this.minecraftEvents = minecraftEvents != null ? minecraftEvents : new HashMap<>();
    }
}
