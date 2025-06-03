// IBackupManager.java
package rl.sage.rangerlevels.database;

public interface IBackupManager {
    /** Genera un JSON individual en config/rangerlevels/backups/<uuid>_<timestamp>.json */
    void savePlayerData(PlayerData data);
}
