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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Configuración general del mod: base de datos, niveles, multiplicadores,
 * auto-save, limitador de EXP y control por dimensiones en YAML.
 */
public class ExpConfig {
    private static final Logger LOGGER = LogManager.getLogger(ExpConfig.class);

    public Database database = new Database();
    public Multipliers multipliers = new Multipliers();
    public LevelsConfig levels = LevelsConfig.createDefault();
    public PurgeConfig purge = new PurgeConfig();

    public AutoSave autoSave = new AutoSave();
    public Limiter limiter = new Limiter();
    public WorldsConfig worlds = new WorldsConfig();
    // Nuevas URLs de compra de pases
    public LinkedHashMap<String, String> passBuyUrls = new LinkedHashMap<>();

    private static ExpConfig INSTANCE;

    /** Carga o crea Config.yml con valores por defecto y comentarios. */
    public static ExpConfig load() {
        try {
            File cfgDir = FMLPaths.CONFIGDIR.get().resolve("rangerlevels").toFile();
            if (!cfgDir.exists()) cfgDir.mkdirs();
            File cfgFile = new File(cfgDir, "Config.yml");

            DumperOptions opts = new DumperOptions();
            opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            opts.setIndent(2);
            opts.setPrettyFlow(true);

            LoaderOptions loaderOptions = new LoaderOptions();
            Constructor constructor = new Constructor(ExpConfig.class, loaderOptions);
            Representer representer = new Representer(opts);
            Yaml yaml = new Yaml(constructor, representer, opts, loaderOptions);

            if (!cfgFile.exists()) {
                INSTANCE = createDefault();
                try (Writer w = new FileWriter(cfgFile)) {
                    w.write("# Configuración del mod RangerLevels\n");
                    yaml.dump(INSTANCE, w);
                }
            } else {
                try (Reader r = new FileReader(cfgFile)) {
                    INSTANCE = yaml.load(r);
                    if (INSTANCE == null) INSTANCE = createDefault();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            INSTANCE = createDefault();
        }

        // DEBUG de todas las secciones
        LOGGER.info("database.type = {}", INSTANCE.database.type);
        LOGGER.info("multipliers.events = {}", INSTANCE.multipliers.events);
        LOGGER.info("levels.starting.level = {} exp = {}",
                INSTANCE.levels.getStartingLevel(), INSTANCE.levels.getStartingExperience());
        LOGGER.info("levels.max.level = {}", INSTANCE.levels.getMaxLevel());
        LOGGER.info("autoSave.enable = {}, interval = {}",
                INSTANCE.autoSave.enable, INSTANCE.autoSave.interval);
        LOGGER.info("limiter.enable = {}, expAmount = {}, timer = {}",
                INSTANCE.limiter.enable, INSTANCE.limiter.expAmount, INSTANCE.limiter.timer);
        LOGGER.info("worlds.enable = {}, whitelist = {}, list = {}",
                INSTANCE.worlds.enable, INSTANCE.worlds.whitelist, INSTANCE.worlds.list);
        LOGGER.info("passBuyUrls = {}", INSTANCE.passBuyUrls);
        return INSTANCE;
    }

    /** Devuelve la configuración cargada. */
    public static ExpConfig get() {
        return INSTANCE != null ? INSTANCE : load();
    }

    /** Fuerza recarga desde disco. */
    public static void reload() {
        INSTANCE = null;
        load();
    }

    /** Comprueba si una dimensión, por nombre, está permitida para otorgar EXP. */
    public static boolean isWorldAllowed(String dimName) {
        return get().worlds.isAllowed(dimName);
    }

    public MaxLevelBroadcastConfig maxLevelBroadcast = MaxLevelBroadcastConfig.createDefault();


    /** Configuración por defecto. */
    private static ExpConfig createDefault() {
        ExpConfig cfg = new ExpConfig();

        // Database por defecto
        cfg.database.type = "flatfile";

        // Multiplicadores global y playerDefault
        cfg.multipliers.global = 1.0f;
        cfg.multipliers.playerDefault = 1.0f;

        // Eventos Pixelmon
        String[] pixelmonKeys = new String[]{
                "raidParticipation", "onCapture", "levelUp", "rareCandy", "eggHatch",
                "evolve", "pokeStop", "apricornPick", "advancement", "pokedexEntry",
                "comboBonus", "beatWild", "beatTrainer", "beatBoss", "arceusPlayFlute",
                "playerActivateShrine", "timespaceAltarSpawn", "pokeLootDrop"
        };
        for (String key : pixelmonKeys) {
            cfg.multipliers.events.put(key, 1.0f);
        }

        // Eventos Minecraft
        String[] minecraftKeys = new String[]{
                "itemFished", "cropBreak", "spawnerBreak", "logBreak",
                "melonPumpkinBreak", "coalOreBreak", "ironOreBreak",
                "diamondEmeraldBreak", "playerKill"
        };
        for (String key : minecraftKeys) {
            cfg.multipliers.events.put(key, 1.0f);
        }

        // AutoSave
        cfg.autoSave.enable = true;
        cfg.autoSave.interval = 600;

        // Limiter
        cfg.limiter.enable = true;
        cfg.limiter.expAmount = 100000;
        cfg.limiter.timer = "24h";

        // Worlds
        cfg.worlds.enable = false;
        cfg.worlds.whitelist = true;
        cfg.worlds.list = Arrays.asList(
                "overworld", "the_nether", "the_end", "ultra_space", "drowned"
        );

        // URLs de compra de pases por defecto
        cfg.passBuyUrls.put("super", "https://tuservidor.com/super");
        cfg.passBuyUrls.put("ultra", "https://tuservidor.com/ultra");
        cfg.passBuyUrls.put("master", "https://tuservidor.com/master");

        cfg.maxLevelBroadcast = MaxLevelBroadcastConfig.createDefault();
        cfg.levelUpSound = SoundConfig.createDefaultLevelUp();



        return cfg;
    }

    // Sonido de subida de nivel normal
    public SoundConfig levelUpSound = SoundConfig.createDefaultLevelUp();

    public static class SoundConfig {
        public String soundEvent;
        public float  volume;
        public float  pitch;

        public static SoundConfig createDefaultLevelUp() {
            SoundConfig cfg = new SoundConfig();
            cfg.soundEvent = "minecraft:entity.player.levelup";
            cfg.volume     = 1.0f;
            cfg.pitch      = 1.0f;
            return cfg;
        }
    }


    public static class MaxLevelBroadcastConfig {
        public boolean enable;
        public List<String> message;
        public String soundEvent;
        public float volume;
        public float pitch;

        public boolean isEnable()            { return enable; }

        public static MaxLevelBroadcastConfig createDefault() {
            MaxLevelBroadcastConfig cfg = new MaxLevelBroadcastConfig();
            cfg.enable     = true;
            cfg.message = Arrays.asList(
                    "§6═══════════════════════════",
                    "§e§l¡Atención aventureros! §r§a%PLAYER% §aha alcanzado el §6§lNIVEL MÁXIMO§a§r (§f%LEVEL%§r)",
                    "§6═══════════════════════════"
            );
            cfg.soundEvent = "minecraft:ui.toast.challenge_complete";
            cfg.volume     = 1.3f;
            cfg.pitch      = 0.5f;
            return cfg;
        }
    }





    public static class PurgeConfig {
        public boolean Enable = true;
        public String  Timer = "30d";

        // De String a List<String> para permitir varias líneas
        public List<String> Reminder   = Arrays.asList(
                "&#FF5555 ¡El pase está por terminar!",
                "&#AAAAAA Aprovecha sus últimos momentos para subir de nivel!"
        );
        public List<String> Broadcast  = Arrays.asList(
                "&#FF0000 El pase ha terminado!",
                "&#CCCCCC La obtención de EXP queda bloqueada hasta la nueva temporada."
        );
    }



    // === Estructuras internas ===

    public static class Database {
        public String type = "flatfile";
        public MySQL mysql = new MySQL();

        public static class MySQL {
            public String host = "localhost";
            public int port = 3306;
            public String database = "rangerlevels";
            public String username = "root";
            public String password = "";
            public boolean useSSL = false;
            public String tablePrefix = "rl_";
            public int maxPoolSize = 10;
            public int minIdle = 2;
        }
    }

    public static class Multipliers {
        public float global = 1.0f;
        public float playerDefault = 1.0f;
        public LinkedHashMap<String, Float> events = new LinkedHashMap<>();
    }

    public static class AutoSave {
        public boolean enable = true;
        public int interval = 600;
    }

    public static class Limiter {
        public boolean enable = true;
        public int expAmount = 100000;
        public String timer = "24h";
    }

    public static class WorldsConfig {
        public boolean enable;
        public boolean whitelist;
        public List<String> list;

        public boolean isAllowed(String dimName) {
            if (!enable) return true;
            boolean inList = list != null && list.contains(dimName);
            return whitelist ? inList : !inList;
        }
    }

    // ===== Getters públicos usados en otras clases =====

    public String getDatabaseType() { return database.type; }
    public Database.MySQL getMySQLConfig() { return database.mysql; }
    public float getGlobalMultiplier() { return multipliers.global; }
    public float getPlayerDefaultMultiplier() { return multipliers.playerDefault; }
    public float getEventMultiplier(String eventKey) {
        return multipliers.events.getOrDefault(eventKey, 1.0f);
    }
    public LevelsConfig getLevels() { return levels; }
    public int getStartingLevel() { return levels.getStartingLevel(); }
    public int getStartingExperience() { return levels.getStartingExperience(); }
    public int getMaxLevel() { return levels.getMaxLevel(); }
    public boolean isAutoSaveEnabled() { return autoSave.enable; }
    public int getAutoSaveInterval() { return autoSave.interval; }
    public boolean isLimiterEnabled() { return limiter.enable; }
    public int getLimiterExpAmount() { return limiter.expAmount; }
    public String getLimiterTimer() { return limiter.timer; }
    public boolean isWorldsEnabled() { return worlds.enable; }
    public boolean isWhitelist() { return worlds.whitelist; }
    public List<String> getWorldList() { return worlds.list; }

    /**
     * Devuelve las URLs de compra de pases (keys: "super","ultra","master").
     */
    public LinkedHashMap<String,String> getPassBuyUrls() {
        return passBuyUrls;
    }
}
