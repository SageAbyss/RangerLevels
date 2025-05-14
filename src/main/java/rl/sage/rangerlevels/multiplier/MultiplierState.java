package rl.sage.rangerlevels.multiplier;

import net.minecraftforge.fml.loading.FMLPaths;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Guarda el estado de los multiplicadores:
 * - globalMultiplier + globalExpiry (epoch seconds, -1 = indefinido)
 * - playerMultipliers: para cada jugador, value + expiry
 *
 * Persiste en rangerlevels/multipliers.yml
 */
public class MultiplierState {
    // Multiplicador global y su expiración
    public double globalMultiplier = 1.0;
    public long   globalExpiry     = -1;

    // Multiplicadores por jugador
    public Map<String, PlayerEntry> playerMultipliers = new LinkedHashMap<>();

    /** Entrada de multiplicador privado: valor + expiración */
    public static class PlayerEntry {
        public double value  = 1.0;
        public long   expiry = -1;
    }

    private static MultiplierState INSTANCE;

    /** Carga o crea el archivo multipliers.yml */
    public static MultiplierState load() {
        try {
            // Prepara carpeta y fichero
            File dir = FMLPaths.CONFIGDIR.get().resolve("rangerlevels").toFile();
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, "multipliers.yml");

            // 1) Opciones de escritura YAML
            DumperOptions opts = new DumperOptions();
            opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            opts.setIndent(2);
            opts.setPrettyFlow(true);

            // 2) Opciones de lectura YAML
            LoaderOptions loaderOptions = new LoaderOptions();

            // 3) Constructor con LoaderOptions apuntando a esta clase
            Constructor constructor = new Constructor(MultiplierState.class, loaderOptions);

            // 4) Representer con DumperOptions
            Representer representer = new Representer(opts);

            // 5) Instancia Yaml con constructor, representer, dumper y loader
            Yaml yaml = new Yaml(constructor, representer, opts, loaderOptions);

            if (!file.exists()) {
                // Si no existe, creamos default y guardamos
                INSTANCE = new MultiplierState();
                INSTANCE.save(); // usará el método save() abajo
            } else {
                // Si existe, cargamos
                try (Reader reader = new FileReader(file)) {
                    INSTANCE = yaml.load(reader);
                    if (INSTANCE == null) {
                        INSTANCE = new MultiplierState();
                        INSTANCE.save();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            INSTANCE = new MultiplierState();
        }
        return INSTANCE;
    }

    /** Guarda la instancia actual en multipliers.yml */
    public void save() {
        try {
            // Prepara carpeta y fichero
            File dir  = FMLPaths.CONFIGDIR.get().resolve("rangerlevels").toFile();
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, "multipliers.yml");

            // 1) Opciones de escritura YAML
            DumperOptions opts = new DumperOptions();
            opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            opts.setIndent(2);
            opts.setPrettyFlow(true);

            // 2) Opciones de lectura (no estrictas, pero necesarias para la firma)
            LoaderOptions loaderOptions = new LoaderOptions();

            // 3) Constructor para la misma clase
            Constructor constructor = new Constructor(MultiplierState.class, loaderOptions);

            // 4) Representer con DumperOptions
            Representer representer = new Representer(opts);

            // 5) Instancia Yaml
            Yaml yaml = new Yaml(constructor, representer, opts, loaderOptions);

            // 6) Escribimos el fichero
            try (Writer writer = new FileWriter(file)) {
                yaml.dump(this, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Devuelve la instancia singleton, cargando si no existe */
    public static MultiplierState get() {
        return INSTANCE != null ? INSTANCE : load();
    }

    /** Actualiza el multiplicador global (y su expiración) en memoria */
    public void setGlobal(double val, long expiry) {
        this.globalMultiplier = val;
        this.globalExpiry     = expiry;
    }

    /** Actualiza o crea la entrada de multiplicador privado para un jugador */
    public void setPlayer(String playerName, double val, long expiry) {
        PlayerEntry entry = new PlayerEntry();
        entry.value  = val;
        entry.expiry = expiry;
        playerMultipliers.put(playerName, entry);
    }
}
