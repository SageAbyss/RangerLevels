package rl.sage.rangerlevels.config;

import net.minecraftforge.fml.loading.FMLPaths;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.*;

public class AdminConfig {
    private static final File FILE =
            FMLPaths.CONFIGDIR.get().resolve("rangerlevels/admins.yml").toFile();

    private static final Set<String> ADMINS = new HashSet<>();

    /** Llamar en setup para cargar/clonar el fichero */
    public static void load() {
        try {
            // Si no existe, crea uno por defecto
            if (!FILE.exists()) {
                FILE.getParentFile().mkdirs();
                Yaml yaml = new Yaml();
                Map<String,Object> def = new HashMap<>();
                def.put("admins", Collections.emptyList());
                try (FileWriter writer = new FileWriter(FILE)) {
                    yaml.dump(def, writer);
                }
            }

            // Cargar YAML
            Yaml yaml = new Yaml();
            try (FileInputStream in = new FileInputStream(FILE)) {
                Map<?,?> data = yaml.load(in);
                Object list = data.get("admins");
                ADMINS.clear();
                if (list instanceof List) {
                    for (Object o : (List<?>) list) {
                        // Convertir a String y limpiar espacios
                        String name = o.toString().trim();
                        if (!name.isEmpty()) {
                            ADMINS.add(name);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static boolean addAdmin(String name) {
        boolean added = ADMINS.add(name);
        if (added) save(); // guarda si fue nuevo
        return added;
    }

    public static boolean removeAdmin(String name) {
        boolean removed = ADMINS.remove(name);
        if (removed) save(); // guarda si fue eliminado
        return removed;
    }

    private static void save() {
        try {
            Yaml yaml = new Yaml();
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("admins", new ArrayList<>(ADMINS));
            try (FileWriter writer = new FileWriter(FILE)) {
                yaml.dump(data, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /** Comprueba si el nombre de jugador está en la lista */
    public static boolean isAdmin(String playerName) {
        return ADMINS.contains(playerName);
    }
}
