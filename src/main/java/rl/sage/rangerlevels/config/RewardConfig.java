// src/main/java/rl/sage/rangerlevels/config/RewardConfig.java
package rl.sage.rangerlevels.config;

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

/**
 * Configuración de recompensas en rewards.yml.
 */
public class RewardConfig {
    // Switches raíz
    public boolean Exact      = true;
    public boolean Packages   = true;
    public boolean EveryLevel = true;

    public RewardsBlock Rewards = new RewardsBlock();

    public static class RewardsBlock {
        public LinkedHashMap<String, LevelRewards> Exact    = new LinkedHashMap<String, LevelRewards>();
        public LinkedHashMap<String, LevelRewards> Packages = new LinkedHashMap<String, LevelRewards>();
        public LevelRewards EveryLevel = new LevelRewards();
    }

    public static class LevelRewards {
        public java.util.List<String> items    = new java.util.ArrayList<String>();
        public java.util.List<String> commands = new java.util.ArrayList<String>();
    }

    private static RewardConfig INSTANCE;

    /** Carga o crea rewards.yml */
    public static RewardConfig load() {
        try {
            File cfgDir  = FMLPaths.CONFIGDIR.get().resolve("rangerlevels").toFile();
            if (!cfgDir.exists()) cfgDir.mkdirs();
            File cfgFile = new File(cfgDir, "rewards.yml");

            DumperOptions opts = new DumperOptions();
            opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            opts.setIndent(2);
            opts.setPrettyFlow(true);

            LoaderOptions loaderOpts = new LoaderOptions();
            Constructor constructor = new Constructor(RewardConfig.class, loaderOpts);
            Representer representer = new Representer(opts);
            Yaml yaml = new Yaml(constructor, representer, opts, loaderOpts);

            if (!cfgFile.exists()) {
                INSTANCE = createDefault();
                try (Writer w = new FileWriter(cfgFile)) {
                    yaml.dump(INSTANCE, w);
                }
            } else {
                try (Reader r = new FileReader(cfgFile)) {
                    INSTANCE = yaml.load(r);
                    if (INSTANCE == null) {
                        INSTANCE = createDefault();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            INSTANCE = createDefault();
        }
        return INSTANCE;
    }

    /** Recarga la configuración en runtime */
    public static void reload() {
        INSTANCE = null;
        load();
    }

    /** Devuelve la instancia cargada */
    public static RewardConfig get() {
        return INSTANCE != null ? INSTANCE : load();
    }

    /** Valores por defecto para rewards.yml */
    private static RewardConfig createDefault() {
        RewardConfig cfg = new RewardConfig();

        // Nivel exacto
        LevelRewards lvl5 = new LevelRewards();
        lvl5.items.add("minecraft:paper 5");
        lvl5.items.add("minecraft:diamond 1");
        lvl5.commands.add("tell {PLAYER} ¡desbloqueaste el nivel 5!");
        cfg.Rewards.Exact.put("5", lvl5);

        // Paquetes
        LevelRewards pkg5 = new LevelRewards();
        pkg5.items.add("minecraft:emerald 1");
        pkg5.commands.add("tell {PLAYER} recibiste un paquete por 5 niveles!");
        cfg.Rewards.Packages.put("5", pkg5);

        // EveryLevel
        LevelRewards every = new LevelRewards();
        every.items.add("minecraft:emerald_block 1");
        every.commands.add("tell {PLAYER} ¡subiste un nivel!");
        cfg.Rewards.EveryLevel = every;

        return cfg;
    }
}
