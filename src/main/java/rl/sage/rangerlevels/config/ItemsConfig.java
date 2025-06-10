// File: src/main/java/rl/sage/rangerlevels/config/ItemsConfig.java
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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ItemsConfig {
    private static final Logger LOGGER = LogManager.getLogger(ItemsConfig.class);
    private static ItemsConfig INSTANCE;

    // =======================
    // Configuración de Items
    // =======================
    public ChampionAmuletConfig championAmulet = ChampionAmuletConfig.createDefault();
    public ShinyAmuletConfig shinyAmulet = ShinyAmuletConfig.createDefault();

    // =======================
    // Carga y acceso singleton
    // =======================
    public static ItemsConfig get() {
        return INSTANCE != null ? INSTANCE : load();
    }

    public static ItemsConfig load() {
        try {
            File cfgDir = FMLPaths.CONFIGDIR.get().resolve("rangerlevels").toFile();
            if (!cfgDir.exists()) cfgDir.mkdirs();
            File cfgFile = new File(cfgDir, "ItemsConfig.yml");

            // YAML settings
            DumperOptions opts = new DumperOptions();
            opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            opts.setIndent(2);
            opts.setPrettyFlow(true);

            LoaderOptions loaderOptions = new LoaderOptions();
            Constructor constructor = new Constructor(ItemsConfig.class, loaderOptions);
            Representer representer = new Representer(opts);
            Yaml yaml = new Yaml(constructor, representer, opts, loaderOptions);

            if (!cfgFile.exists()) {
                // Generar archivo con valores por defecto
                INSTANCE = createDefault();
                try (Writer w = new FileWriter(cfgFile)) {
                    w.write("# Configuración de Items para RangerLevels\n\n");
                    Map<String, Object> root = new LinkedHashMap<>();
                    root.put("championAmulet", INSTANCE.championAmulet);
                    root.put("shinyAmulet", INSTANCE.shinyAmulet);
                    root.put("battleBanner", INSTANCE.battleBanner);
                    yaml.dump(root, w);
                }
                LOGGER.info("ItemsConfig.yml creado con valores por defecto.");
            } else {
                // Cargar desde archivo existente
                try (Reader r = new FileReader(cfgFile)) {
                    INSTANCE = yaml.load(r);
                    if (INSTANCE == null) {
                        INSTANCE = createDefault();
                        LOGGER.warn("ItemsConfig.yml cargado como null, regenerando defaults.");
                    } else {
                        LOGGER.info("ItemsConfig.yml cargado correctamente.");
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error cargando ItemsConfig.yml, usando defaults.", e);
            INSTANCE = createDefault();
        }
        return INSTANCE;
    }

    public static void reload() {
        INSTANCE = null;
        load();
    }

    private static ItemsConfig createDefault() {
        ItemsConfig cfg = new ItemsConfig();
        cfg.championAmulet = ChampionAmuletConfig.createDefault();
        cfg.shinyAmulet = ShinyAmuletConfig.createDefault();
        return cfg;
    }

    // =======================
    // a) Champion Amulet
    // =======================
    public static class ChampionAmuletConfig {
        public double xpPercent;
        public java.util.List<MysteryBoxesConfig.MysteryBoxConfig.CommandEntry> commands;

        public static ChampionAmuletConfig createDefault() {
            ChampionAmuletConfig c = new ChampionAmuletConfig();
            c.xpPercent = 10.0;
            c.commands = Collections.singletonList(
                    new MysteryBoxesConfig.MysteryBoxConfig.CommandEntry(
                            50,
                            "say ¡Felicitaciones, %player%! Has activado el Amuleto de Campeón."
                    )
            );
            return c;
        }
    }

    // =======================
    // b) Shiny Amulet
    // =======================
    public static class ShinyAmuletConfig {
        public double legendariaPercent;
        public double estelarPercent;
        public double miticoPercent;

        public static ShinyAmuletConfig createDefault() {
            ShinyAmuletConfig c = new ShinyAmuletConfig();
            c.legendariaPercent = 5.0;
            c.estelarPercent = 10.0;
            c.miticoPercent = 100.0;
            return c;
        }
    }
    // =======================
    // b) Bandera de Batalla
    // =======================
    public BannerConfig battleBanner = BannerConfig.createDefault();

    public static class BannerConfig {
        public double radiusDefault;
        public int   durationDefault;   // en minutos
        public double radiusEpic;
        public int   durationEpic;
        public double radiusStellar;
        public int   durationStellar;
        public double radiusMythic;
        public int   durationMythic;
        public static BannerConfig createDefault() {
            BannerConfig c = new BannerConfig();
            c.radiusDefault   = 15.0;
            c.durationDefault = 30;
            c.radiusEpic     = 15.0;
            c.durationEpic   = 30;
            c.radiusStellar  = 15.0;
            c.durationStellar= 30;
            c.radiusMythic   = 15.0;
            c.durationMythic = 30;
            return c;
        }
    }
}
