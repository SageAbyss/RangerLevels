// src/main/java/rl/sage/rangerlevels/config/MysteryBoxesConfig.java
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
import java.util.*;

public class MysteryBoxesConfig {
    private static final Logger LOGGER = LogManager.getLogger(MysteryBoxesConfig.class);
    private static MysteryBoxesConfig INSTANCE;

    public ChampionAmuletConfig championAmulet = ChampionAmuletConfig.createDefault();
    public ShinyAmuletConfig shinyAmulet       = ShinyAmuletConfig.createDefault();
    public MysteryBoxConfig   mysteryBox       = MysteryBoxConfig.createDefault();

    public static MysteryBoxesConfig get() {
        return INSTANCE != null ? INSTANCE : load();
    }

    public static MysteryBoxesConfig load() {
        try {
            File cfgDir = FMLPaths.CONFIGDIR.get().resolve("rangerlevels").toFile();
            if (!cfgDir.exists()) cfgDir.mkdirs();
            File cfgFile = new File(cfgDir, "MysteryBoxesConfig.yml");

            DumperOptions opts = new DumperOptions();
            opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            opts.setIndent(2);
            opts.setPrettyFlow(true);

            LoaderOptions loaderOptions = new LoaderOptions();
            Constructor constructor = new Constructor(MysteryBoxesConfig.class, loaderOptions);
            Representer representer = new Representer(opts);
            Yaml yaml = new Yaml(constructor, representer, opts, loaderOptions);

            if (!cfgFile.exists()) {
                INSTANCE = createDefault();
                try (Writer w = new FileWriter(cfgFile)) {
                    w.write("# Configuración de Mystery Boxes para RangerLevels\n\n");
                    Map<String,Object> root = new LinkedHashMap<>();
                    root.put("mysteryBox", INSTANCE.mysteryBox);
                    root.put("championAmulet", INSTANCE.championAmulet);
                    root.put("shinyAmulet", INSTANCE.shinyAmulet);
                    yaml.dump(root, w);
                }
                LOGGER.info("MysteryBoxesConfig.yml creado con valores por defecto.");
            } else {
                try (Reader r = new FileReader(cfgFile)) {
                    INSTANCE = yaml.load(r);
                    if (INSTANCE == null) {
                        INSTANCE = createDefault();
                        LOGGER.warn("MysteryBoxesConfig.yml cargado como null, regenerando defaults.");
                    } else {
                        LOGGER.info("MysteryBoxesConfig.yml cargado correctamente.");
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error cargando MysteryBoxesConfig.yml, usando defaults.", e);
            INSTANCE = createDefault();
        }
        return INSTANCE;
    }

    public static void reload() {
        INSTANCE = null;
        load();
    }

    private static MysteryBoxesConfig createDefault() {
        MysteryBoxesConfig cfg = new MysteryBoxesConfig();
        cfg.championAmulet = ChampionAmuletConfig.createDefault();
        cfg.shinyAmulet    = ShinyAmuletConfig.createDefault();
        cfg.mysteryBox     = MysteryBoxConfig.createDefault();
        return cfg;
    }

    // =======================
    // a) Champion Amulet
    // =======================
    public static class ChampionAmuletConfig {
        public double xpPercent;
        public List<MysteryBoxConfig.CommandEntry> commands;

        public static ChampionAmuletConfig createDefault() {
            ChampionAmuletConfig c = new ChampionAmuletConfig();
            c.xpPercent = 10.0;
            c.commands = Collections.singletonList(
                    new MysteryBoxConfig.CommandEntry(50,
                            "say ¡Felicitaciones, %player%! Has activado el Amuleto de Campeón.")
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
            c.estelarPercent    = 10.0;
            c.miticoPercent     = 100.0;
            return c;
        }
    }

    // =======================
    // c) MysteryBoxConfig
    // =======================
    public static class MysteryBoxConfig {
        public TierBoxConfig comun;
        public TierBoxConfig raro;
        public TierBoxConfig epico;
        public TierBoxConfig legendario;
        public TierBoxConfig estelar;
        public TierBoxConfig mitico;

        public static MysteryBoxConfig createDefault() {
            MysteryBoxConfig m = new MysteryBoxConfig();
            m.comun      = createTier(true, 15.0, 10.0, 12.0, 80.0,   12, 20, 50,
                    Arrays.asList("caja_misteriosa_raro"),
                    Collections.singletonList(new CommandEntry(100, "say %player% obtuvo sorpresa Común")),
                    map(80,30,10),50.0,  0,3);
            m.raro       = createTier(true, 10.0, 5.0,  8.0,  70.0,   10, 50,100,
                    Arrays.asList("caja_misteriosa_epico"),
                    Collections.singletonList(new CommandEntry(100, "say %player% obtuvo sorpresa Raro")),
                    map(70,30),40.0,     1,3);
            m.epico      = createTier(true,  7.0, 3.0,  5.0,  60.0,    8,100,200,
                    Arrays.asList("caja_misteriosa_legendario"),
                    Collections.singletonList(new CommandEntry(100, "say %player% obtuvo sorpresa Épico")),
                    map(60,40),30.0,     1,3);
            m.legendario = createTier(true,  5.0, 1.5,  4.0,  50.0,    6,200,400,
                    Arrays.asList("caja_misteriosa_estelar"),
                    Collections.singletonList(new CommandEntry(100, "say %player% obtuvo sorpresa Legendario")),
                    map(50,50),15.0,     1,3);
            m.estelar    = createTier(true,  3.0, 1.0,  2.5,  40.0,    4,400,600,
                    Arrays.asList("caja_misteriosa_mitico"),
                    Collections.singletonList(new CommandEntry(100, "say %player% obtuvo sorpresa Estelar")),
                    map(40,60),8.0,     1,3);
            m.mitico     = createTier(true,  2.0, 0.5,  1.0,  30.0,    0,600,1000,
                    Collections.emptyList(),
                    Collections.singletonList(new CommandEntry(100, "say %player% obtuvo sorpresa Mítico")),
                    map(30,70), 3.0,    1,3);
            return m;
        }

        private static LinkedHashMap<String,Integer> map(int a, int b) {
            LinkedHashMap<String,Integer> m = new LinkedHashMap<>();
            m.put("PIXELMON_POKE_BALL", a);
            m.put("DIAMOND", b);
            return m;
        }
        private static LinkedHashMap<String,Integer> map(int a, int b, int c) {
            LinkedHashMap<String,Integer> m = new LinkedHashMap<>();
            m.put("PIXELMON_POKE_BALL", a);
            m.put("DIAMOND", b);
            m.put("EMERALD", c);
            return m;
        }

        private static TierBoxConfig createTier(
                boolean enable,
                double beat, double level, double raid,
                double noDropChance,
                int upChance,
                int expMin, int expMax,
                List<String> upgrades,
                List<CommandEntry> cmds,
                LinkedHashMap<String,Integer> itemsChance,
                double randomItemsNoDropChance,
                int randMin, int randMax
        ) {
            TierBoxConfig c = new TierBoxConfig();
            c.enable             = enable;
            c.dropChanceBeatBoss = beat;
            c.dropChanceLevelUp  = level;
            c.dropChanceRaid     = raid;
            c.noDropChance       = noDropChance;
            c.upgradeBoxChance   = upChance;
            c.expMin             = expMin;
            c.expMax             = expMax;
            c.boxesUpgrade       = upgrades;
            c.commands           = cmds;
            c.itemsChance        = itemsChance;
            c.randomItemsNoDropChance  = randomItemsNoDropChance;
            c.randomItemMin      = randMin;
            c.randomItemMax      = randMax;
            return c;
        }

        public static class TierBoxConfig {
            public boolean enable;
            public double dropChanceBeatBoss;
            public double dropChanceLevelUp;
            public double dropChanceRaid;
            public double noDropChance;
            public int    upgradeBoxChance;
            public int    expMin;
            public int    expMax;
            public List<String> boxesUpgrade;
            public List<CommandEntry> commands;
            public Map<String,Integer> itemsChance;
            public int randomItemMin;
            public int randomItemMax;
            public double randomItemsNoDropChance;
        }

        public static class CommandEntry {
            public int    chancePercent;
            public String command;
            public CommandEntry() {}
            public CommandEntry(int chance, String cmd) {
                this.chancePercent = chance;
                this.command       = cmd;
            }
        }
    }
}
