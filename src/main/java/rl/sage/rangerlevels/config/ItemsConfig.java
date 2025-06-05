// File: rl/sage/rangerlevels/config/ItemsConfig.java
package rl.sage.rangerlevels.config;

import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

/**
 * Configuración de items para el mod RangerLevels.
 * Genera/lee ItemsConfig.yml en config/rangerlevels/.
 */
public class ItemsConfig {
    private static final Logger LOGGER = LogManager.getLogger(ItemsConfig.class);

    /** Configuración específica del Amuleto de Campeón. */
    public ChampionAmuletConfig championAmulet = ChampionAmuletConfig.createDefault();

    /** Configuración específica del Amuleto Shiny. */
    public ShinyAmuletConfig shinyAmulet = ShinyAmuletConfig.createDefault();

    private static ItemsConfig INSTANCE;

    /**
     * Carga o crea ItemsConfig.yml con valores por defecto y comentarios.
     */
    public static ItemsConfig load() {
        try {
            File cfgDir = FMLPaths.CONFIGDIR.get().resolve("rangerlevels").toFile();
            if (!cfgDir.exists()) cfgDir.mkdirs();
            File cfgFile = new File(cfgDir, "ItemsConfig.yml");

            DumperOptions opts = new DumperOptions();
            opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            opts.setIndent(2);
            opts.setPrettyFlow(true);

            LoaderOptions loaderOptions = new LoaderOptions();
            Constructor constructor = new Constructor(ItemsConfig.class, loaderOptions);
            Representer representer = new Representer(opts);
            Yaml yaml = new Yaml(constructor, representer, opts, loaderOptions);

            if (!cfgFile.exists()) {
                // Si no existe, creamos uno con valores por defecto
                INSTANCE = createDefault();
                try (Writer w = new FileWriter(cfgFile)) {
                    w.write("# Configuración de items para RangerLevels\n");
                    yaml.dump(INSTANCE, w);
                }
                LOGGER.info("ItemsConfig.yml no existía. Se ha creado con valores por defecto.");
            } else {
                // Si ya existe, lo cargamos
                try (Reader r = new FileReader(cfgFile)) {
                    INSTANCE = yaml.load(r);
                    if (INSTANCE == null) {
                        INSTANCE = createDefault();
                        LOGGER.warn("ItemsConfig.yml cargado como null, se regeneran valores por defecto.");
                    } else {
                        LOGGER.info("ItemsConfig.yml cargado correctamente.");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Error cargando ItemsConfig.yml, se usan valores por defecto.", e);
            INSTANCE = createDefault();
        }

        return INSTANCE;
    }

    /** Devuelve la instancia cargada (la carga si aún no existe). */
    public static ItemsConfig get() {
        return (INSTANCE != null) ? INSTANCE : load();
    }

    /** Fuerza recarga desde disco en la próxima llamada a get(). */
    public static void reload() {
        INSTANCE = null;
        load();
    }

    /**
     * Valores por defecto para ItemsConfig.
     * Se usan si no existe todavía el archivo o hay error al leerlo.
     */
    private static ItemsConfig createDefault() {
        ItemsConfig cfg = new ItemsConfig();
        cfg.championAmulet = ChampionAmuletConfig.createDefault();
        cfg.shinyAmulet    = ShinyAmuletConfig.createDefault();
        return cfg;
    }

    // === Clase interna para la configuración del Amuleto de Campeón ===

    public static class ChampionAmuletConfig {
        /** % extra de EXP al derrotar (igual que antes). */
        public double xpPercent;

        /**
         * Lista de comandos + probabilidad.
         * Cada entrada tiene su propia probabilidad “chancePercent” y su “command”.
         */
        public java.util.List<CommandEntry> commands;

        public static ChampionAmuletConfig createDefault() {
            ChampionAmuletConfig c = new ChampionAmuletConfig();
            c.xpPercent = 10.0; // valor por defecto

            // Por defecto, sólo una entrada:
            CommandEntry entry = new CommandEntry();
            entry.command = "say ¡Felicitaciones, %player%! Has activado el Amuleto de Campeón.";
            entry.chancePercent = 0.5;
            c.commands = java.util.Arrays.asList(entry);

            return c;
        }

        /** Clase que agrupa un comando y su probabilidad (en %). */
        public static class CommandEntry {
            public double chancePercent;
            public String command;
        }
    }

    // === Clase interna para la configuración del Amuleto Shiny ===

    public static class ShinyAmuletConfig {
        /** % de probabilidad para convertir en Shiny (Legendaria). */
        public double legendariaPercent;
        /** % de probabilidad para convertir en Shiny (Estelar). */
        public double estelarPercent;

        public double miticoPercent;


        public static ShinyAmuletConfig createDefault() {
            ShinyAmuletConfig c = new ShinyAmuletConfig();
            c.legendariaPercent = 5.0;  // 5 % por defecto para Tier Legendaria
            c.estelarPercent    = 10.0; // 10 % por defecto para Tier Estelar
            c.miticoPercent     = 100.0;
            return c;
        }
    }
}
