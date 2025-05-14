// src/main/java/rl/sage/rangerlevels/database/IPlayerDataManager.java
package rl.sage.rangerlevels.database;

import java.util.UUID;

public interface IPlayerDataManager {
    /** Crea tablas o archivos según el backend */
    void initTables();
    /** Carga datos del jugador (o crea nuevos) */
    PlayerData loadPlayerData(UUID uuid, String nickname);
    /** Guarda/actualiza datos del jugador */
    void savePlayerData(PlayerData data);
    /** Limpia de memoria (p. ej. al desconectarse) */
    void unloadPlayerData(UUID uuid);
    /** Cierra conexiones si aplica */
    void close();
    /** Nombre legible del backend */
    String getSourceName();
}
