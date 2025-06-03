// JSONBackupManager.java
package rl.sage.rangerlevels.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.fml.loading.FMLPaths;
import rl.sage.rangerlevels.RangerLevels;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Implementación sencilla de IBackupManager: crea un archivo JSON individual
 * en config/rangerlevels/backups/<uuid>_<timestamp>.json
 */
public class JSONBackupManager implements IBackupManager {

    private static final String SUBDIR    = "rangerlevels/backups";
    private final Gson gson;
    private final RangerLevels mod = RangerLevels.INSTANCE;
    private final Path backupDir;

    public JSONBackupManager() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        Path configDir = FMLPaths.CONFIGDIR.get();
        this.backupDir = configDir.resolve(SUBDIR);
        try {
            if (!Files.exists(backupDir)) {
                Files.createDirectories(backupDir);
            }
        } catch (IOException e) {
            mod.getLogger().error("[RangerLevels] No se pudo crear carpeta de backups: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void savePlayerData(PlayerData data) {
        String filename = data.getUuid() + ".json";
        Path filePath = backupDir.resolve(filename);
        try (Writer writer = Files.newBufferedWriter(filePath)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            mod.getLogger().error("[RangerLevels] ERROR creando backup {}: {}", filename, e.getMessage());
            e.printStackTrace();
        }
    }
}
