// src/main/java/rl/sage/rangerlevels/database/PlayerData.java
package rl.sage.rangerlevels.database;

import java.time.Instant;
import java.util.UUID;

public class PlayerData {
    private UUID uuid;
    private String nickname;
    private int level;
    private double exp;
    private Instant lastUpdate;      // fecha
    private float multiplier;

    public PlayerData(UUID uuid, String nickname, int level, double exp, float multiplier) {
        this.uuid = uuid;
        this.nickname = nickname;
        this.level = level;
        this.exp = exp;
        this.multiplier = multiplier;
        this.lastUpdate = Instant.now();
    }

    // Getters y setters
    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getExp() { return (int) exp; }
    public void setExp(double exp) { this.exp = exp; }

    public Instant getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(Instant lastUpdate) { this.lastUpdate = lastUpdate; }

    public float getMultiplier() { return multiplier; }
    public void setMultiplier(float multiplier) { this.multiplier = multiplier; }
}
