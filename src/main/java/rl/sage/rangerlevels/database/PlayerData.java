// src/main/java/rl/sage/rangerlevels/database/PlayerData.java
package rl.sage.rangerlevels.database;

import java.util.UUID;

public class PlayerData {

    private String uuid;        // UUID como string sin guiones
    private String nickname;    // Nombre actual del jugador
    private int level;          // Nivel actual
    private double exp;         // Experiencia acumulada
    private double multiplier;  // Multiplicador actual
    private long timestamp;     // Epoch seconds de última actualización

    /** Constructor vacío requerido por Gson */
    public PlayerData() { }

    /**
     * Constructor completo que genera un PlayerData nuevo.
     *
     * @param uuid       UUID real del jugador
     * @param nickname   Nickname actual
     * @param level      Nivel
     * @param exp        Experiencia
     * @param multiplier Multiplicador
     * @param timestamp  Epoch seconds (System.currentTimeMillis()/1000)
     */
    public PlayerData(UUID uuid, String nickname, int level, double exp, double multiplier, long timestamp) {
        // Guardamos el UUID como string sin guiones
        this.uuid       = uuid.toString().replace("-", "");
        this.nickname   = nickname;
        this.level      = level;
        this.exp        = exp;
        this.multiplier = multiplier;
        this.timestamp  = timestamp;
    }

    // --- Getters y setters (Gson los necesita) ---

    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getNickname() {
        return nickname;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }

    public int getExp() {
        return (int) exp;
    }
    public void setExp(double exp) {
        this.exp = exp;
    }

    public float getMultiplier() {
        return (float) multiplier;
    }
    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Reconstruye un objeto UUID a partir de la cadena sin guiones.
     */
    public UUID getUuidAsObject() {
        String s = uuid;
        if (s.length() != 32) {
            throw new IllegalStateException("UUID inválido en PlayerData: " + s);
        }
        // Insertar guiones en el formato 8-4-4-4-12
        String withDashes = s.substring(0, 8) + "-" +
                s.substring(8, 12) + "-" +
                s.substring(12, 16) + "-" +
                s.substring(16, 20) + "-" +
                s.substring(20);
        return UUID.fromString(withDashes);
    }
}
