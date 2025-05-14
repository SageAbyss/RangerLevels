// FlatFilePlayerDataManager.java
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

public class FlatFilePlayerDataManager implements IPlayerDataManager {
    private final File dataFile;
    private final Gson gson;
    private final Map<UUID, PlayerData> cache;

    public FlatFilePlayerDataManager(RangerLevels plugin) {
        File dir = new File("config", plugin.MODID);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        this.dataFile = new File(dir, "PlayerData.json");
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(UUID.class, new UUIDTypeAdapter())
                .create();
        this.cache = new HashMap<UUID, PlayerData>();
        initTables();
    }

    @Override
    public void initTables() {
        loadAll();
    }

    private void loadAll() {
        if (!dataFile.exists()) {
            return;
        }
        try (Reader reader = new InputStreamReader(new FileInputStream(dataFile), StandardCharsets.UTF_8)) {
            Type type = new TypeToken<Map<UUID, PlayerData>>() {}.getType();
            Map<UUID, PlayerData> map = gson.fromJson(reader, type);
            if (map != null) {
                cache.putAll(map);
            }
        } catch (IOException e) {
            RangerLevels.INSTANCE.getLogger().error("[FlatFile] Error cargando JSON", e);
        }
    }

    @Override
    public PlayerData loadPlayerData(UUID uuid, String nickname) {
        PlayerData pd = cache.get(uuid);
        if (pd != null) {
            pd.setNickname(nickname);
            return pd;
        }
        pd = new PlayerData(
                uuid,
                nickname,
                RangerLevels.INSTANCE.getExpConfig().getLevels().getStartingLevel(), // ya devuelve int
                RangerLevels.INSTANCE.getExpConfig().getLevels().getStartingExperience(),
                RangerLevels.INSTANCE.getExpConfig().getPlayerDefaultMultiplier()
        );
        cache.put(uuid, pd);
        savePlayerData(pd);
        return pd;
    }

    @Override
    public void savePlayerData(PlayerData data) {
        cache.put(data.getUuid(), data);
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(dataFile), StandardCharsets.UTF_8)) {
            gson.toJson(cache, writer);
        } catch (IOException e) {
            RangerLevels.INSTANCE.getLogger().error("[FlatFile] Error guardando JSON", e);
        }
    }

    @Override
    public void unloadPlayerData(UUID uuid) {
        PlayerData pd = cache.remove(uuid);
        if (pd != null) {
            savePlayerData(pd);
        }
    }

    @Override
    public void close() {
        // No-op
    }

    @Override
    public String getSourceName() {
        return "JSON-Backup";
    }
}