// File: rl/sage/rangerlevels/config/ShopConfig.java
package rl.sage.rangerlevels.config;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import rl.sage.rangerlevels.RangerLevels;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.*;

/**
 * Configuración de la tienda rotativa:
 * - rotation.interval: intervalo (parseable “30s”, “1h”, “1d”).
 * - rotation.selectionSize: cuántas especies elegir cada rotación.
 * - cost: lista de ítems y cantidades (AND) para selección.
 * - randomCost: lista de ítems y cantidades (AND) para botón Random.
 * - menu: título, tamaño, slots e íconos.
 * - messages: textos de feedback.
 * - pokemonLevel: nivel por defecto.
 */
public class ShopConfig {
    private static final Logger LOGGER = LogManager.getLogger(ShopConfig.class);
    private static ShopConfig INSTANCE;

    public RotationConfig rotation = RotationConfig.createDefault();
    public CostConfig     cost     = CostConfig.createDefault();
    public MenuConfig     menu     = MenuConfig.createDefault();
    public MessagesConfig messages = MessagesConfig.createDefault();

    /** Nivel por defecto con que se da el Pokémon legendario */
    public int pokemonLevel = 70;

    public static ShopConfig get() {
        return INSTANCE != null ? INSTANCE : load();
    }

    @SuppressWarnings("unchecked")
    public static ShopConfig load() {
        File cfgDir = FMLPaths.CONFIGDIR.get().resolve("rangerlevels").toFile();
        if (!cfgDir.exists()) cfgDir.mkdirs();
        File cfgFile = new File(cfgDir, "ShopConfig.yml");

        DumperOptions opts = new DumperOptions();
        opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        opts.setIndent(2);
        opts.setPrettyFlow(true);
        Yaml yaml = new Yaml(opts);

        if (!cfgFile.exists()) {
            INSTANCE = createDefault();
            dumpDefaultConfig(cfgFile, INSTANCE, yaml);
            LOGGER.info("ShopConfig.yml creado con valores por defecto.");
        } else {
            try (Reader r = new FileReader(cfgFile)) {
                Map<String,Object> root = yaml.load(r);
                INSTANCE = createFromMap(root);
                LOGGER.info("ShopConfig.yml cargado correctamente.");
            } catch (Exception e) {
                LOGGER.error("Error cargando ShopConfig.yml, usando defaults.", e);
                INSTANCE = createDefault();
            }
        }
        return INSTANCE;
    }

    private static void dumpDefaultConfig(File file, ShopConfig cfg, Yaml yaml) {
        try (Writer w = new FileWriter(file)) {
            w.write("# Configuración de la Tienda rotativa para RangerLevels\n\n");
            Map<String,Object> root = new LinkedHashMap<>();

            // rotation
            Map<String,Object> rot = new LinkedHashMap<>();
            rot.put("interval",     cfg.rotation.interval);
            rot.put("selectionSize",cfg.rotation.selectionSize);
            root.put("rotation", rot);

            // cost (legendarios)
            List<Map<String,Object>> costs = new ArrayList<>();
            for (CostConfig.CostEntry e : cfg.cost.entries) {
                Map<String,Object> m = new LinkedHashMap<>();
                m.put("item",   e.item);
                m.put("amount", e.amount);
                costs.add(m);
            }
            root.put("cost", costs);

            // randomCost (botón Random)
            List<Map<String,Object>> randCosts = new ArrayList<>();
            for (CostConfig.CostEntry e : cfg.cost.randomEntries) {
                Map<String,Object> m = new LinkedHashMap<>();
                m.put("item",   e.item);
                m.put("amount", e.amount);
                randCosts.add(m);
            }
            root.put("randomCost", randCosts);

            // menu
            Map<String,Object> menu = new LinkedHashMap<>();
            menu.put("title", cfg.menu.title);
            menu.put("size",  cfg.menu.size);
            Map<String,Object> slots = new LinkedHashMap<>();
            slots.put("items",        cfg.menu.slots.items);
            slots.put("randomButton", cfg.menu.slots.randomButton);
            slots.put("timeDisplay",  cfg.menu.slots.timeDisplay);
            slots.put("backButton",   cfg.menu.slots.backButton);
            menu.put("slots", slots);
            Map<String,Object> icons = new LinkedHashMap<>();
            icons.put("random",        cfg.menu.icons.random);
            icons.put("time",          cfg.menu.icons.time);
            icons.put("back",          cfg.menu.icons.back);
            icons.put("legendGeneric", cfg.menu.icons.legendGeneric);
            menu.put("icons", icons);
            root.put("menu", menu);

            // messages
            Map<String,Object> msgs = new LinkedHashMap<>();
            msgs.put("noEssence",     cfg.messages.noEssence);
            msgs.put("bought",        cfg.messages.bought);
            msgs.put("error",         cfg.messages.error);
            msgs.put("rotationReset", cfg.messages.rotationReset);
            root.put("messages", msgs);

            // pokemonLevel
            root.put("pokemonLevel", cfg.pokemonLevel);

            yaml.dump(root, w);
        } catch (Exception e) {
            LOGGER.error("Error escribiendo ShopConfig.yml por defecto.", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static ShopConfig createFromMap(Map<String,Object> root) {
        ShopConfig cfg = new ShopConfig();
        if (root == null) return cfg;

        // rotation
        Map<String,Object> rot = (Map<String,Object>) root.get("rotation");
        if (rot != null) {
            Object iv = rot.get("interval");
            if (iv instanceof String) cfg.rotation.interval = (String) iv;
            Object ss = rot.get("selectionSize");
            if (ss instanceof Integer) cfg.rotation.selectionSize = (Integer) ss;
        }

        // cost (legendarios)
        Object costObj = root.get("cost");
        cfg.cost.entries.clear();
        if (costObj instanceof List) {
            for (Object o : (List<?>) costObj) {
                if (o instanceof Map) {
                    Map<String,Object> m = (Map<String,Object>) o;
                    Object itemObj = m.get("item");
                    Object amtObj  = m.get("amount");
                    if (itemObj instanceof String && amtObj instanceof Integer) {
                        cfg.cost.entries.add(
                                new CostConfig.CostEntry((String) itemObj, (Integer) amtObj)
                        );
                    }
                }
            }
        }

        // randomCost (botón Random)
        Object randObj = root.get("randomCost");
        cfg.cost.randomEntries.clear();
        if (randObj instanceof List) {
            for (Object o : (List<?>) randObj) {
                if (o instanceof Map) {
                    Map<String,Object> m = (Map<String,Object>) o;
                    Object itemObj = m.get("item");
                    Object amtObj  = m.get("amount");
                    if (itemObj instanceof String && amtObj instanceof Integer) {
                        cfg.cost.randomEntries.add(
                                new CostConfig.CostEntry((String) itemObj, (Integer) amtObj)
                        );
                    }
                }
            }
        }

        // menu
        Map<String,Object> menu = (Map<String,Object>) root.get("menu");
        if (menu != null) {
            Object t = menu.get("title");
            if (t instanceof String) cfg.menu.title = (String) t;
            Object s = menu.get("size");
            if (s instanceof Integer) cfg.menu.size = (Integer) s;
            Map<String,Object> slots = (Map<String,Object>) menu.get("slots");
            if (slots != null) {
                Object itemsObj = slots.get("items");
                if (itemsObj instanceof List) {
                    cfg.menu.slots.items = (List<Integer>) itemsObj;
                }
                Object rb = slots.get("randomButton");
                if (rb instanceof Integer) cfg.menu.slots.randomButton = (Integer) rb;
                Object td = slots.get("timeDisplay");
                if (td instanceof Integer) cfg.menu.slots.timeDisplay = (Integer) td;
                Object bb = slots.get("backButton");
                if (bb instanceof Integer) cfg.menu.slots.backButton = (Integer) bb;
            }
            Map<String,Object> icons = (Map<String,Object>) menu.get("icons");
            if (icons != null) {
                Object randI = icons.get("random");
                if (randI instanceof String) cfg.menu.icons.random = (String) randI;
                Object timeI = icons.get("time");
                if (timeI instanceof String) cfg.menu.icons.time = (String) timeI;
                Object backI = icons.get("back");
                if (backI instanceof String) cfg.menu.icons.back = (String) backI;
                Object legendI = icons.get("legendGeneric");
                if (legendI instanceof String) cfg.menu.icons.legendGeneric = (String) legendI;
            }
        }

        // messages
        Map<String,Object> msgs = (Map<String,Object>) root.get("messages");
        if (msgs != null) {
            Object ne = msgs.get("noEssence");
            if (ne instanceof String) cfg.messages.noEssence = (String) ne;
            Object bo = msgs.get("bought");
            if (bo instanceof String) cfg.messages.bought = (String) bo;
            Object er = msgs.get("error");
            if (er instanceof String) cfg.messages.error = (String) er;
            Object rr = msgs.get("rotationReset");
            if (rr instanceof String) cfg.messages.rotationReset = (String) rr;
        }

        // pokemonLevel
        Object pl = root.get("pokemonLevel");
        if (pl instanceof Integer) {
            cfg.pokemonLevel = (Integer) pl;
        } else if (pl instanceof String) {
            try {
                cfg.pokemonLevel = Integer.parseInt((String) pl);
            } catch (NumberFormatException ignored) {}
        }

        return cfg;
    }

    public static void reload() {
        // 1) Guarda el valor previo
        String oldInterval = (INSTANCE != null ? INSTANCE.rotation.interval : null);

        // 2) Fuerza recarga de config
        INSTANCE = null;
        load();

        // 3) Si antes había config y el intervalo cambió, refresca el estado
        String newInterval = INSTANCE.rotation.interval;
        if (oldInterval != null && !oldInterval.equals(newInterval)) {
            ShopState.reload();
        }
        // si oldInterval==null (arranque) o no cambió, NO tocamos ShopState
    }

    private static ShopConfig createDefault() {
        return new ShopConfig();
    }

    // ========== nested ==========

    public static class RotationConfig {
        public String interval      = "1d";
        public int    selectionSize = 4;
        public static RotationConfig createDefault() { return new RotationConfig(); }
    }

    public static class CostConfig {
        public List<CostEntry> entries       = new ArrayList<>(Arrays.asList(
                new CostEntry("concentrado_almas", 2)
        ));
        public List<CostEntry> randomEntries = new ArrayList<>(Arrays.asList(
                new CostEntry("concentrado_almas", 1)  // precio por defecto para Random
        ));
        public static CostConfig createDefault() { return new CostConfig(); }

        public static class CostEntry {
            public String item;
            public int    amount;
            public CostEntry() {}
            public CostEntry(String item, int amount) {
                this.item = item;
                this.amount = amount;
            }
        }
    }

    public static class MenuConfig {
        public String title = "§8✦ Tienda Rotativa ✦";
        public int    size  = 54;
        public SlotsConfig slots = new SlotsConfig();
        public IconsConfig icons = new IconsConfig();
        public static MenuConfig createDefault() { return new MenuConfig(); }

        public static class SlotsConfig {
            public List<Integer> items        = Arrays.asList(10,12,14,16);
            public int randomButton = 22;
            public int timeDisplay  = 4;
            public int backButton   = 53;
        }
        public static class IconsConfig {
            public String random       = "minecraft:nether_star";
            public String time         = "minecraft:clock";
            public String back         = "minecraft:arrow";
            public String legendGeneric= "minecraft:oxeye_daisy";
        }
    }

    public static class MessagesConfig {
        public String noEssence     = "§cNo te alcanza para comprar esto.";
        public String bought        = "§a¡Has comprado un %pokemon%! Revisa tu equipo.";
        public String error         = "§cOcurrió un error al procesar la compra.";
        public String rotationReset = "§6¡La tienda rotativa se ha actualizado! Próximo reinicio en §e%time%§6.";
        public static MessagesConfig createDefault() { return new MessagesConfig(); }
    }

    // ================= Helper para resolver IDs =================

    public static ResourceLocation resolveItemLocation(String itemId) {
        if (itemId == null || itemId.isEmpty()) {
            throw new IllegalArgumentException("Item ID inválido: vacío");
        }
        if (itemId.contains(":")) {
            return new ResourceLocation(itemId.trim());
        } else {
            return new ResourceLocation(RangerLevels.MODID, itemId.trim());
        }
    }
}
