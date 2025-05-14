// src/main/java/rl/sage/rangerlevels/database/JSONBackupManager.java
package rl.sage.rangerlevels.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.util.UUIDTypeAdapter;
import rl.sage.rangerlevels.RangerLevels;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JSONBackupManager implements IPlayerDataManager {
    private final File file;
    private final Gson gson;
    private final Map<UUID, PlayerData> cache = new HashMap<>();

    public JSONBackupManager(RangerLevels plugin) {
        File cfg = new File("config", plugin.MODID);
        if (!cfg.exists()) cfg.mkdirs();
        this.file = new File(cfg, "PlayerData.json");
        this.gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter( UUID.class, new UUIDTypeAdapter() ).create();
        initTables();
    }

    @Override
    public void initTables() {
        // carga inicial
        if (!file.exists()) return;
        try (Reader r = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            Type type = new TypeToken<Map<UUID, PlayerData>>(){}.getType();
            Map<UUID, PlayerData> raw = gson.fromJson(r, type);
            if (raw != null) cache.putAll(raw);
        } catch (IOException e) {
            RangerLevels.INSTANCE.getLogger().error("[BackupJSON] Error cargando respaldo", e);
        }
    }

    @Override
    public PlayerData loadPlayerData(UUID uuid, String nickname) {
        PlayerData pd = cache.get(uuid);
        if (pd != null) {
            pd.setNickname(nickname);
            return pd;
        }
        // nuevo
        pd = new PlayerData(uuid, nickname,
                RangerLevels.INSTANCE.getExpConfig().levels.starting.level,
                RangerLevels.INSTANCE.getExpConfig().levels.starting.experience,
                RangerLevels.INSTANCE.getExpConfig().multipliers.playerDefault
        );
        cache.put(uuid, pd);
        savePlayerData(pd);
        return pd;
    }

    @Override
    public void savePlayerData(PlayerData data) {
        cache.put(data.getUuid(), data);
        try (Writer w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            gson.toJson(cache, w);
        } catch (IOException e) {
            RangerLevels.INSTANCE.getLogger().error("[BackupJSON] Error guardando respaldo", e);
        }
    }

    @Override
    public void unloadPlayerData(UUID uuid) {
        cache.remove(uuid);
        saveAll();
    }

    private void saveAll() {
        try (Writer w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            gson.toJson(cache, w);
        } catch (IOException e) {
            RangerLevels.INSTANCE.getLogger().error("[BackupJSON] Error guardando respaldo", e);
        }
    }

    @Override
    public void close() {
        // nada extra
    }

    @Override
    public String getSourceName() {
        return "JSON-Backup";
    }
}
