// File: src/main/java/rl/sage/rangerlevels/config/MysteryBoxesConfig.java
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

    public MysteryBoxConfig mysteryBox = MysteryBoxConfig.createDefault();
    public SpawnBoxesConfig spawnBoxes = SpawnBoxesConfig.createDefault();

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
                    Map<String, Object> root = new LinkedHashMap<>();
                    root.put("mysteryBox", INSTANCE.mysteryBox);
                    root.put("spawnBoxes",  INSTANCE.spawnBoxes);
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
        normalizeMysteryBoxConfig(INSTANCE);
        return INSTANCE;
    }

    public static void reload() {
        INSTANCE = null;
        load();
    }

    private static void normalizeMysteryBoxConfig(MysteryBoxesConfig cfg) {
        MysteryBoxConfig mb = cfg.mysteryBox;
        List<MysteryBoxConfig.TierBoxConfig> tiers = Arrays.asList(
                mb.comun, mb.raro, mb.epico, mb.legendario, mb.estelar, mb.mitico
        );
        for (MysteryBoxConfig.TierBoxConfig t : tiers) {
            if (t.commands == null) t.commands = Collections.emptyList();
            if (t.boxesUpgrade == null) t.boxesUpgrade = Collections.emptyList();
            if (t.itemsChance == null) t.itemsChance = Collections.emptyMap();
            if (t.randomItemTierWeights == null) t.randomItemTierWeights = Collections.emptyMap();
        }
        if (cfg.spawnBoxes.tierWeights == null) {
            cfg.spawnBoxes.tierWeights = Collections.emptyMap();
        }
    }

    private static MysteryBoxesConfig createDefault() {
        MysteryBoxesConfig cfg = new MysteryBoxesConfig();
        cfg.mysteryBox = MysteryBoxConfig.createDefault();
        return cfg;
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
            // Pesos de ítem por tier para caja COMÚN
            LinkedHashMap<String, Double> commonItemTiers = new LinkedHashMap<>();
            commonItemTiers.put("comun", 90.0);
            commonItemTiers.put("raro", 60.0);
            commonItemTiers.put("epico", 10.0);
            commonItemTiers.put("legendario", 1.0);
            commonItemTiers.put("estelar", 0.1);
            commonItemTiers.put("mitico", 0.1);
            // Pesos de ítem por tier para caja COMÚN
            LinkedHashMap<String, Double> rareItemTiers = new LinkedHashMap<>();
            rareItemTiers.put("comun", 80.0);
            rareItemTiers.put("raro", 50.0);
            rareItemTiers.put("epico", 15.0);
            rareItemTiers.put("legendario", 2.0);
            rareItemTiers.put("estelar", 0.5);
            rareItemTiers.put("mitico", 0.1);
            // Pesos de ítem por tier para caja COMÚN
            LinkedHashMap<String, Double> epicItemTiers = new LinkedHashMap<>();
            epicItemTiers.put("comun", 65.0);
            epicItemTiers.put("raro", 30.0);
            epicItemTiers.put("epico", 15.0);
            epicItemTiers.put("legendario", 8.0);
            epicItemTiers.put("estelar", 2.5);
            epicItemTiers.put("mitico", 0.1);
            // Pesos de ítem por tier para caja COMÚN
            LinkedHashMap<String, Double> legendItemTiers = new LinkedHashMap<>();
            legendItemTiers.put("comun", 50.0);
            legendItemTiers.put("raro", 20.0);
            legendItemTiers.put("epico", 15.0);
            legendItemTiers.put("legendario", 13.0);
            legendItemTiers.put("estelar", 8.5);
            legendItemTiers.put("mitico", 1.5);
            // Pesos de ítem por tier para caja COMÚN
            LinkedHashMap<String, Double> estelarItemTiers = new LinkedHashMap<>();
            estelarItemTiers.put("comun", 40.0);
            estelarItemTiers.put("raro", 20.0);
            estelarItemTiers.put("epico", 15.0);
            estelarItemTiers.put("legendario", 18.0);
            estelarItemTiers.put("estelar", 17.5);
            estelarItemTiers.put("mitico", 4.5);
            // Pesos de ítem por tier para caja COMÚN
            LinkedHashMap<String, Double> mythicItemTiers = new LinkedHashMap<>();
            mythicItemTiers.put("comun", 20.0);
            mythicItemTiers.put("raro", 20.0);
            mythicItemTiers.put("epico", 15.0);
            mythicItemTiers.put("legendario", 13.0);
            mythicItemTiers.put("estelar", 11.5);
            mythicItemTiers.put("mitico", 9.5);

            MysteryBoxConfig m = new MysteryBoxConfig();
            m.comun = createTier(true, 15.0, 10.0, 12.0, 80.0, 12, 20, 50,
                    Arrays.asList("caja_misteriosa_raro"),
                    Collections.singletonList(new CommandEntry(100, "say %player% obtuvo sorpresa Común")),
                    map(80, 30, 10), 50.0, commonItemTiers, 0, 3);
            m.raro = createTier(true, 10.0, 5.0, 8.0, 70.0, 10, 50, 100,
                    Arrays.asList("caja_misteriosa_epico"),
                    Collections.singletonList(new CommandEntry(100, "say %player% obtuvo sorpresa Raro")),
                    map(70, 30), 40.0, rareItemTiers, 1, 3);
            m.epico = createTier(true, 7.0, 3.0, 5.0, 60.0, 8, 100, 200,
                    Arrays.asList("caja_misteriosa_legendario"),
                    Collections.singletonList(new CommandEntry(100, "say %player% obtuvo sorpresa Épico")),
                    map(60, 40), 30.0, epicItemTiers, 1, 3);
            m.legendario = createTier(true, 5.0, 1.5, 4.0, 50.0, 6, 200, 400,
                    Arrays.asList("caja_misteriosa_estelar"),
                    Collections.singletonList(new CommandEntry(100, "say %player% obtuvo sorpresa Legendario")),
                    map(50, 50), 15.0, legendItemTiers, 1, 3);
            m.estelar = createTier(true, 3.0, 1.0, 2.5, 40.0, 4, 400, 600,
                    Arrays.asList("caja_misteriosa_mitico"),
                    Collections.singletonList(new CommandEntry(100, "say %player% obtuvo sorpresa Estelar")),
                    map(40, 60), 8.0, estelarItemTiers, 1, 3);
            m.mitico = createTier(true, 2.0, 0.5, 1.0, 30.0, 0, 600, 1000,
                    Collections.emptyList(),
                    Collections.singletonList(new CommandEntry(100, "say %player% obtuvo sorpresa Mítico")),
                    map(30, 70), 3.0, mythicItemTiers, 1, 3);
            return m;
        }

        private static LinkedHashMap<String, Integer> map(int a, int b) {
            LinkedHashMap<String, Integer> m = new LinkedHashMap<>();
            m.put("PIXELMON_POKE_BALL", a);
            m.put("DIAMOND", b);
            return m;
        }

        private static LinkedHashMap<String, Integer> map(int a, int b, int c) {
            LinkedHashMap<String, Integer> m = new LinkedHashMap<>();
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
                LinkedHashMap<String, Integer> itemsChance,
                double randomItemsNoDropChance,
                Map<String, Double> randomItemTierWeights,
                int randMin, int randMax
        ) {
            TierBoxConfig c = new TierBoxConfig();
            c.enable = enable;
            c.dropChanceBeatBoss = beat;
            c.dropChanceLevelUp = level;
            c.dropChanceRaid = raid;
            c.noDropChance = noDropChance;
            c.upgradeBoxChance = upChance;
            c.expMin = expMin;
            c.expMax = expMax;
            c.boxesUpgrade = upgrades;
            c.commands = cmds;
            c.itemsChance = itemsChance;
            c.randomItemsNoDropChance = randomItemsNoDropChance;
            c.randomItemTierWeights = randomItemTierWeights;
            c.randomItemMin = randMin;
            c.randomItemMax = randMax;
            return c;
        }

        public static class TierBoxConfig {
            public boolean enable;
            public double dropChanceBeatBoss;
            public double dropChanceLevelUp;
            public double dropChanceRaid;
            public double noDropChance;
            public int upgradeBoxChance;
            public int expMin;
            public int expMax;
            public List<String> boxesUpgrade;
            public List<CommandEntry> commands;
            public Map<String, Integer> itemsChance;
            public int randomItemMin;
            public int randomItemMax;
            public double randomItemsNoDropChance;
            public Map<String, Double> randomItemTierWeights;
        }

        public static class CommandEntry {
            public int chancePercent;
            public String command;

            public CommandEntry() { }

            public CommandEntry(int chance, String cmd) {
                this.chancePercent = chance;
                this.command = cmd;
            }
        }
    }

    // =======================
    // d) SpawnBoxesConfig
    // =======================
    public static class SpawnBoxesConfig {
        /** Cada cuántos minutos tratar de spawnear una caja */
        public int intervalMinutes;
        /** Radio alrededor del jugador donde aparecer la caja */
        public double spawnRadius;
        /** Probabilidad (%) total de que aparezca una caja en cada intervalo */
        public double globalSpawnChance;
        /** Pesos (%) por tier de caja para el spawn */
        public Map<String, Double> tierWeights;
        public int unclaimedMinutes;

        public static SpawnBoxesConfig createDefault() {
            SpawnBoxesConfig s = new SpawnBoxesConfig();
            s.intervalMinutes     = 30;
            s.spawnRadius         = 50.0;
            s.globalSpawnChance   = 80.0;
            s.unclaimedMinutes   = 5;
            // Por defecto, pesos iguales para cada tier
            Map<String, Double> weights = new LinkedHashMap<>();
            weights.put("comun",       80.0);
            weights.put("raro",        70.0);
            weights.put("epico",       30.0);
            weights.put("legendario",   5.0);
            weights.put("estelar",      1.5);
            weights.put("mitico",       0.1);
            s.tierWeights = weights;
            return s;
        }
    }
}

