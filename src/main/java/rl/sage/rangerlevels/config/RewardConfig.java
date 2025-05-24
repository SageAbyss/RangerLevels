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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Configuración de recompensas en rewards.yml para Minecraft Forge 1.16.5 (Java 8).
 * Server‑side únicamente.
 */
public class RewardConfig {

    // Activos globales
    public boolean Exact      = true;
    public boolean Packages   = true;
    public boolean EveryLevel = true;

    /** Sonidos configurables por tipo de recompensa */
    public SoundsBlock Sounds = new SoundsBlock();

    /** Recompensas organizadas por tipo */
    public RewardsBlock Rewards = new RewardsBlock();

    /** Sonidos que suenan al reclamar cada tipo */
    public static class SoundsBlock {
        /** Sonido al reclamar EveryLevel */
        public String EveryLevel = "minecraft:entity.player.levelup";
        /** Sonido al reclamar Exact */
        public String Exact      = "minecraft:block.note_block.pling";
        /** Sonido al reclamar Packages */
        public String Packages   = "minecraft:block.chest.open";
    }

    /** Bloque principal de recompensas */
    public static class RewardsBlock {
        /** EveryLevel: ruta → RouteRewards */
        public LinkedHashMap<String, RouteRewards> EveryLevel   = new LinkedHashMap<>();

        /** Exact: nivel → (ruta → RouteRewards) */
        public LinkedHashMap<String, LinkedHashMap<String, RouteRewards>> Exact    = new LinkedHashMap<>();

        /** Packages: nivel → (ruta → RouteRewards) */
        public LinkedHashMap<String, LinkedHashMap<String, RouteRewards>> Packages = new LinkedHashMap<>();
    }

    /**
     * Recompensas para una ruta concreta (Free, Super, Ultra, Master).
     * - En EveryLevel usa el campo Enable para activar/desactivar.
     * - En Exact/Packages, si la ruta no está presente, no se entrega nada.
     */
    public static class RouteRewards {
        /** Solo EveryLevel: activar o desactivar esta ruta */
        public Boolean Enable = true;
        /** Ítems tipo "modid:item amount" */
        public List<String> items;
        /** Comandos a ejecutar, con {PLAYER} y/o {ITEMS} de variable */
        public List<String> commands;
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

        // Sonidos por defecto
        cfg.Sounds.EveryLevel = "minecraft:entity.player.levelup";
        cfg.Sounds.Exact      = "minecraft:block.note_block.pling";
        cfg.Sounds.Packages   = "minecraft:block.chest.open";

        // === EveryLevel por ruta ===
        for (String route : Arrays.asList("Free", "Super", "Ultra", "Master")) {
            RouteRewards rr = new RouteRewards();
            // Enable: Free y Master activos por defecto
            rr.Enable = "Free".equals(route) || "Master".equals(route);
            // Ejemplo de ítems y comandos
            switch (route) {
                case "Free":
                    rr.items = Arrays.asList("minecraft:emerald 1");
                    rr.commands = Arrays.asList("tell {PLAYER} ¡Subiste un nivel! (Free)");
                    break;
                case "Super":
                    rr.items = Arrays.asList("minecraft:diamond 1");
                    rr.commands = Arrays.asList("tell {PLAYER} ¡Subiste un nivel! (Super)");
                    break;
                case "Ultra":
                    rr.items = Arrays.asList("minecraft:diamond 2");
                    rr.commands = Arrays.asList("tell {PLAYER} ¡Subiste un nivel! (Ultra)");
                    break;
                default:  // Master
                    rr.items = Arrays.asList("minecraft:netherite_ingot 1");
                    rr.commands = Arrays.asList("tell {PLAYER} ¡Subiste un nivel! (Master)");
                    break;
            }
            cfg.Rewards.EveryLevel.put(route, rr);
        }

        // === Exact nivel 5 ===
        LinkedHashMap<String, RouteRewards> lvl5 = new LinkedHashMap<>();
        // Free
        RouteRewards free5 = new RouteRewards();
        free5.items = Arrays.asList("minecraft:paper 5");
        free5.commands = Arrays.asList("tell {PLAYER} ¡Desbloqueaste nivel 5 (Free)!");
        lvl5.put("Free", free5);
        // Super (ejemplo comentado en YAML)
        // Ultra (ejemplo comentado en YAML)
        // Master
        RouteRewards master5 = new RouteRewards();
        master5.items = Arrays.asList("minecraft:netherite_scrap 2");
        master5.commands = Arrays.asList("tell {PLAYER} ¡Desbloqueaste nivel 5 (Master)!");
        lvl5.put("Master", master5);
        cfg.Rewards.Exact.put("5", lvl5);

        // === Packages nivel 5 ===
        LinkedHashMap<String, RouteRewards> pkg5 = new LinkedHashMap<>();
        // Free
        RouteRewards pf5 = new RouteRewards();
        pf5.items = Arrays.asList("minecraft:iron_ingot 10");
        pf5.commands = Arrays.asList("tell {PLAYER} ¡Recibiste paquete por 5 niveles! (Free)");
        pkg5.put("Free", pf5);
        // Super
        RouteRewards ps5 = new RouteRewards();
        ps5.items = Arrays.asList("minecraft:gold_ingot 5");
        ps5.commands = Arrays.asList("tell {PLAYER} ¡Recibiste paquete por 5 niveles! (Super)");
        pkg5.put("Super", ps5);
        // Ultra
        RouteRewards pu5 = new RouteRewards();
        pu5.items = Arrays.asList("minecraft:diamond 2");
        pu5.commands = Arrays.asList("tell {PLAYER} ¡Recibiste paquete por 5 niveles! (Ultra)");
        pkg5.put("Ultra", pu5);
        // Master (ejemplo comentado en YAML)
        cfg.Rewards.Packages.put("5", pkg5);

        return cfg;
    }
}
