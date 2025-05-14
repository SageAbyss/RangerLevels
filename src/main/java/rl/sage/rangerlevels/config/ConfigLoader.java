package rl.sage.rangerlevels.config;

import net.minecraftforge.fml.loading.FMLPaths;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;

public class ConfigLoader {
    private static final Path BASE_DIR =
            FMLPaths.CONFIGDIR.get().resolve("rangerlevels");
    private static final Path CONFIG_PATH =
            BASE_DIR.resolve("Earn-Exp-Config.yml");

    /** La configuración cargada */
    public static EarnExpConfig CONFIG;

    public static void load() {
        try {
            // 1) Asegurar directorio de config/rangerlevels
            if (Files.notExists(BASE_DIR)) {
                Files.createDirectories(BASE_DIR);
            }

            // 2) Preparar SnakeYAML
            LoaderOptions loaderOptions = new LoaderOptions();
            Constructor constructor = new Constructor(EarnExpConfig.class, loaderOptions);

            DumperOptions dumperOptions = new DumperOptions();
            dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            dumperOptions.setIndent(2);
            dumperOptions.setPrettyFlow(true);

            Representer representer = new Representer(dumperOptions);
            Yaml yaml = new Yaml(constructor, representer, dumperOptions, loaderOptions);

            // 3) Si no existe el archivo en disco, crearlo
            if (Files.notExists(CONFIG_PATH)) {
                // Intentar copiar recurso interno desde config/Earn-Exp-Config.yml
                try (InputStream resource = Thread.currentThread()
                        .getContextClassLoader()
                        .getResourceAsStream("config/Earn-Exp-Config.yml")) {
                    if (resource != null) {
                        Files.copy(resource, CONFIG_PATH);
                    } else {
                        // Generar desde valores por defecto
                        EarnExpConfig defaults = new EarnExpConfig();
                        try (Writer writer = Files.newBufferedWriter(
                                CONFIG_PATH, StandardOpenOption.CREATE_NEW)) {
                            yaml.dump(defaults, writer);
                        }
                    }
                }
            }

            // 4) Cargar desde el archivo en disco
            try (InputStream inFile = Files.newInputStream(CONFIG_PATH)) {
                CONFIG = yaml.load(inFile);
            }
            if (CONFIG == null) {
                CONFIG = new EarnExpConfig();
            }

            // 5) Asegurar que los mapas no sean nulos
            if (CONFIG.getPixelmonEvents() == null) {
                CONFIG.setPixelmonEvents(new HashMap<>());
            }
            if (CONFIG.getMinecraftEvents() == null) {
                CONFIG.setMinecraftEvents(new HashMap<>());
            }

        } catch (IOException e) {
            throw new RuntimeException("Error cargando Earn-Exp-Config.yml", e);
        }
    }
}
