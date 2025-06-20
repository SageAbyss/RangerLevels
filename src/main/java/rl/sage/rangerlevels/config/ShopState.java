package rl.sage.rangerlevels.config;

import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import rl.sage.rangerlevels.util.TimeUtil;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.time.Instant;
import java.util.*;

public class ShopState {
    private static final Logger LOGGER = LogManager.getLogger(ShopState.class);
    private static ShopState INSTANCE;

    public List<String> currentSelection;
    public long nextRotationEpoch;
    public String lastInterval;
    public Set<UUID> purchasedPlayers;

    private ShopState() {}

    public static ShopState get() {
        return INSTANCE != null ? INSTANCE : load();
    }

    private static File getStateFile() {
        File dir = FMLPaths.CONFIGDIR.get().resolve("rangerlevels").toFile();
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, "ShopState.yml");
    }

    @SuppressWarnings("unchecked")
    public static ShopState load() {
        File f = getStateFile();
        Yaml yaml = new Yaml(new DumperOptions());
        try {
            if (!f.exists()) {
                INSTANCE = defaultState();
                save();
                LOGGER.info("ShopState.yml creado con estado por defecto. nextRotationEpoch={}", INSTANCE.nextRotationEpoch);
            } else {
                try (Reader r = new FileReader(f)) {
                    Map<String,Object> map = yaml.load(r);
                    INSTANCE = fromMap(map);
                    LOGGER.info("ShopState.yml cargado: nextRotationEpoch={}, purchases={} ",
                            INSTANCE.nextRotationEpoch, INSTANCE.purchasedPlayers.size());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error cargando ShopState, usando defaults.", e);
            INSTANCE = defaultState();
        }
        return INSTANCE;
    }

    public static void save() {
        File f = getStateFile();
        Yaml yaml = new Yaml(new DumperOptions());
        Map<String,Object> out = new LinkedHashMap<>();
        out.put("currentSelection", get().currentSelection);
        out.put("nextRotationEpoch", get().nextRotationEpoch);
        out.put("lastInterval", get().lastInterval);
        List<String> uuids = new ArrayList<>();
        for (UUID id : get().purchasedPlayers) uuids.add(id.toString());
        out.put("purchasedPlayers", uuids);

        try (Writer w = new FileWriter(f)) {
            yaml.dump(out, w);
        } catch (Exception e) {
            LOGGER.error("Error guardando ShopState.", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static ShopState fromMap(Map<String,Object> m) {
        ShopState s = new ShopState();
        // Cargar valores básicos
        s.currentSelection = (List<String>) m.getOrDefault("currentSelection", new ArrayList<>());
        Object epochObj = m.get("nextRotationEpoch");
        long now = Instant.now().getEpochSecond();
        if (epochObj instanceof Number) {
            s.nextRotationEpoch = ((Number) epochObj).longValue();
        } else {
            s.nextRotationEpoch = now;
        }
        s.lastInterval = (String) m.get("lastInterval");
        s.purchasedPlayers = new HashSet<>();
        Object pp = m.get("purchasedPlayers");
        if (pp instanceof List) {
            for (Object o : (List<?>) pp) {
                try { s.purchasedPlayers.add(UUID.fromString(o.toString())); }
                catch (Exception ignored) {}
            }
        }

        // Comportamiento de reinicio según intervalo
        String cfgInterval = ShopConfig.get().rotation.interval;
        long intervalSec;
        try {
            intervalSec = TimeUtil.parseDuration(cfgInterval);
        } catch (IllegalArgumentException ex) {
            LOGGER.error("Intervalo inválido '{}' al recargar state, usando 1d.", cfgInterval, ex);
            intervalSec = TimeUtil.parseDuration("1d");
        }

        if (s.lastInterval == null) {
            // Primera carga: inicializar nextRotationEpoch
            s.nextRotationEpoch = now + intervalSec;
            s.lastInterval = cfgInterval;
            LOGGER.info("ShopState: lastInterval era null; inicializando nextRotationEpoch a {}", s.nextRotationEpoch);
        } else if (!cfgInterval.equals(s.lastInterval)) {
            // El admin cambió rotation.interval → reiniciar contador
            s.nextRotationEpoch = now + intervalSec;
            s.lastInterval = cfgInterval;
            LOGGER.info("ShopState: intervalo cambió en config; nextRotationEpoch reiniciado a {}", s.nextRotationEpoch);
        } // Si el intervalo es el mismo, dejamos nextRotationEpoch tal cual viene del archivo.

        return s;
    }

    private static ShopState defaultState() {
        ShopConfig.reload();
        String intervalStr = ShopConfig.get().rotation.interval;
        long intervalSec;
        try {
            intervalSec = TimeUtil.parseDuration(intervalStr);
        } catch (IllegalArgumentException ex) {
            LOGGER.error("Intervalo inválido '{}' en defaultState, usando 1d.", intervalStr, ex);
            intervalSec = TimeUtil.parseDuration("1d");
        }
        long now = Instant.now().getEpochSecond();
        ShopState s = new ShopState();
        s.currentSelection = new ArrayList<>();
        s.nextRotationEpoch = now + intervalSec;
        s.lastInterval = intervalStr;
        s.purchasedPlayers = new HashSet<>();
        return s;
    }
    public static void reload() {
        INSTANCE = null;
        load();
        save();  // guarda para persistir cualquier ajuste (p. ej. nextRotationEpoch si cambió)
    }
}
