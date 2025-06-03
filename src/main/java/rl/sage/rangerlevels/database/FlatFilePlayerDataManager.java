package rl.sage.rangerlevels.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.capability.ILevel;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementación de IPlayerDataManager que guarda TODO en un único JSON: Data.json.
 * - La carpeta base es: <server_root>/config/rangerlevels
 * - El archivo JSON es:    Data.json
 *
 * Usa Gson para serializar/deserializar, y siempre escribe en un temporal
 * para luego renombrar de forma atómica (evitar JSON corruptos).
 */
public class FlatFilePlayerDataManager implements IPlayerDataManager {

    private static FlatFilePlayerDataManager INSTANCE;

    private static final String SUBDIR    = "rangerlevels";
    private static final String DATA_FILE = "Data.json";

    private final Gson gson;
    private final Path dataFilePath;

    /** Mapa en memoria: clave = UUID del jugador (java.util.UUID), valor = PlayerData */
    private final Map<UUID, PlayerData> playerDataMap;

    /** Logger de tu mod */
    private final RangerLevels mod = RangerLevels.INSTANCE;

    /** Constructor privado (Singleton) */
    private FlatFilePlayerDataManager() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        // -------- Rutas usando FMLPaths.CONFIGDIR --------
        Path configDir = FMLPaths.CONFIGDIR.get();              // → "<server_root>/config"
        Path dir       = configDir.resolve(SUBDIR);             // → "<server_root>/config/rangerlevels"

        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (IOException e) {
            throw new RuntimeException("[RangerLevels] No se pudo crear carpeta config/rangerlevels: " + e.getMessage(), e);
        }

        this.dataFilePath = dir.resolve(DATA_FILE);             // → ".../config/rangerlevels/Data.json"

        // Si no existe Data.json, lo creamos con {} inicial
        try {
            if (!Files.exists(dataFilePath)) {
                try (Writer writer = Files.newBufferedWriter(dataFilePath)) {
                    writer.write("{}");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("[RangerLevels] No se pudo inicializar Data.json: " + e.getMessage(), e);
        }

        this.playerDataMap = new HashMap<>();
    }

    /** Devuelve la instancia singleton */
    public static FlatFilePlayerDataManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FlatFilePlayerDataManager();
        }
        return INSTANCE;
    }

    /** Solo para debug: cuántos registros hay en memoria */
    public int getLoadedCount() {
        return playerDataMap.size();
    }

    /**
     * Carga TODO lo que haya en Data.json y lo coloca en playerDataMap.
     * Si hay error, vacía el mapa y lo informa.
     */
    @Override
    public void loadAll() {
        mod.getLogger().info("§8[DEBUG] §eIntentando cargar Data.json");
        try (Reader reader = Files.newBufferedReader(dataFilePath)) {
            Type type = new TypeToken<Map<String, PlayerData>>() {}.getType();
            Map<String, PlayerData> raw = gson.fromJson(reader, type);

            playerDataMap.clear();
            if (raw != null) {
                for (Map.Entry<String, PlayerData> entry : raw.entrySet()) {
                    PlayerData pd   = entry.getValue();
                    UUID       uuid = pd.getUuidAsObject();
                    playerDataMap.put(uuid, pd);
                }
            }

            mod.getLogger().info("§8[DEBUG] §aCargados {} registros desde Data.json", playerDataMap.size());
        } catch (Exception e) {
            mod.getLogger().error("[RangerLevels] ERROR cargando Data.json: {}", e.getMessage());
            e.printStackTrace();
            playerDataMap.clear();
        }
    }

    /**
     * Escribe TODO playerDataMap en un archivo temporal (Data.json.tmp)
     * y luego renombra atómicamente a Data.json.
     */
    @Override
    public void saveAll() {
        // Creamos un mapa auxiliar donde la clave es el UUID sin guiones
        Map<String, PlayerData> raw = new HashMap<>();
        for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
            raw.put(entry.getKey().toString().replace("-", ""), entry.getValue());
        }

        Path tempFile = dataFilePath.getParent().resolve(DATA_FILE + ".tmp");
        try (Writer writer = Files.newBufferedWriter(tempFile,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            gson.toJson(raw, writer);
            writer.flush();
            Files.move(tempFile, dataFilePath,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);

        } catch (Exception e) {
            mod.getLogger().error("[RangerLevels] ERROR al guardar Data.json: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public PlayerData get(UUID uuid) {
        return playerDataMap.get(uuid);
    }

    /**
     * Si existe un registro para este UUID, devuelve ese PlayerData.
     * Si no existe, crea uno nuevo con:
     *   level=1, exp=0.0, multiplier=1.0, timestamp=ahora, nickname actual.
     * Actualiza el mapa en memoria y lo devuelve.
     */
    @Override
    public PlayerData getOrCreate(UUID uuid, String nickname) {
        PlayerData data = playerDataMap.get(uuid);
        if (data == null) {
            long now = System.currentTimeMillis() / 1000L;
            data = new PlayerData(uuid, nickname, 1, 0.0, 1.0, now);
            playerDataMap.put(uuid, data);
        } else {
            // Actualiza nickname en caso de que haya cambiado
            data.setNickname(nickname);
        }
        return data;
    }

    /**
     * Guarda solo el registro de este jugador:
     *  1) Actualiza el mapa en memoria.
     *  2) Llama a saveAll() para volcar TODO en Data.json.
     */
    @Override
    public void savePlayerData(PlayerData data) {
        UUID uuid = data.getUuidAsObject();
        playerDataMap.put(uuid, data);
        saveAll();
    }

    /**
     * Recarga TODO Data.json desde disco y llena playerDataMap.
     * Además, para cada jugador online, aplica sus valores (nivel/exp/multiplier)
     * a la capacidad ILevel en memoria.
     */
    @Override
    public void reload() {
        loadAll();

        // Para cada jugador que esté conectado, sincronizamos la capability con lo leído
        for (ServerPlayerEntity player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            UUID uuid = player.getUUID();
            PlayerData remoto = playerDataMap.get(uuid);
            if (remoto != null) {
                player.getCapability(ILevel.CAPABILITY).ifPresent(cap -> {
                    cap.setLevel(remoto.getLevel());
                    cap.setExp(remoto.getExp());
                    cap.setPlayerMultiplier(remoto.getMultiplier());
                });
            }
        }

        mod.getLogger().info("§8[DEBUG] §aData.json recargado y aplicado a jugadores online");
    }
    public Collection<PlayerData> getAllData() {
        return playerDataMap.values();
    }
}
