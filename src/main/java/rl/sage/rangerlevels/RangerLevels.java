package rl.sage.rangerlevels;

import com.pixelmonmod.pixelmon.Pixelmon;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rl.sage.rangerlevels.commands.CommandRegistry;
import rl.sage.rangerlevels.capability.*;
import rl.sage.rangerlevels.config.AdminConfig;
import rl.sage.rangerlevels.config.ConfigLoader;
import rl.sage.rangerlevels.config.ExpConfig;
import rl.sage.rangerlevels.config.RewardConfig;
import rl.sage.rangerlevels.database.*;
import rl.sage.rangerlevels.events.ExpEventHandler;
import rl.sage.rangerlevels.events.PixelmonEventHandler;
import rl.sage.rangerlevels.limiter.LimiterManager;
import rl.sage.rangerlevels.limiter.LimiterWorldData;
import rl.sage.rangerlevels.util.GradientText;

@Mod(RangerLevels.MODID)
public class RangerLevels {
    public static final String MODID = "rangerlevels";
    public static final IFormattableTextComponent PREFIX =
            GradientText.of("RangerLevels", "#C397F1", "#A4DDE1", "#A671BD")
                    .withStyle(Style.EMPTY.withBold(true));
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static RangerLevels INSTANCE;

    private final IPlayerDataManager dataManager;
    private final IBackupManager backupManager;
    private final AutoSaveTask autoSaveTask;


    public RangerLevels() {
        INSTANCE = this;
        ExpConfig.load();  // Pre-carga de config

        // 3) Inicializar gestores de datos
        this.dataManager   = FlatFilePlayerDataManager.getInstance();
        this.backupManager = new JSONBackupManager();

        // 4) Registrar tarea de autosave
        this.autoSaveTask = new AutoSaveTask(this);
        MinecraftForge.EVENT_BUS.register(this.autoSaveTask);

        // Listener para setup y servidor en mod-event bus
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::setup);

        // Registros de EVENT_BUS
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(ExpEventHandler.class);
        MinecraftForge.EVENT_BUS.register(CommandRegistry.class);
        Pixelmon.EVENT_BUS.register(PixelmonEventHandler.class);

    }

    private void setup(final FMLCommonSetupEvent event) {

        // Capacities
        LOGGER.info("Registrando capacidad de pase...");
        CapabilityManager.INSTANCE.register(
                IPassCapability.class,
                new PassStorage(),
                PassCapability::new
        );

        LOGGER.info("Registrando capacidad de recompensas...");
        PlayerRewardsProvider.register();


        // Configs y gestores de datos
        ConfigLoader.load();
        ExpConfig.load();
        RewardConfig.load();
        AdminConfig.load();

        LevelProvider.register();
        LimiterProvider.register();
        String version = ModLoadingContext.get()
                .getActiveContainer()
                .getModInfo()
                .getVersion()
                .toString();
        LOGGER.info("\n" + "\n" +
                "                 §5██████╗  §b██╗      \n" +
                "                 §5██╔══██╗ §b██║      \n" +
                "                 §5██████╔╝ §b██║      \n" +
                "                 §5██╔══██╗ §b██║      \n" +
                "                 §5██   ██╝ §b███████╗ \n" +
                "                 §5╚═════╝  §b╚══════╝ \n" +
                "\n" +
                "     §d»»» §f¡RangerLevels v" + version + " cargándose! §d«««\n"
        );

       // LOGGER.info("¡RangerLevels listo!");
    }

    @SubscribeEvent
    public void onServerStarted(FMLServerStartedEvent event) {
        // 1) Cargar datos desde disk a memoria
        dataManager.loadAll();

        // 2) Informar cuántos registros cargó
        if (dataManager instanceof FlatFilePlayerDataManager) {
            int count = ((FlatFilePlayerDataManager) dataManager).getLoadedCount();
        } else {
            LOGGER.info("§8onServerStarted §a→ dataManager.loadAll() ejecutado.");
        }
    }


    @SubscribeEvent
    public void onServerStopping(FMLServerStoppingEvent event) {
        dataManager.saveAll();
        LOGGER.info("Deteniendo servidor → Data.json guardado.");
        LOGGER.info("\n" + "\n" +
                "                  §5██████╗  §b██╗      \n" +
                "                  §5██╔══██╗ §b██║      \n" +
                "                  §5██████╔╝ §b██║      \n" +
                "                  §5██╔══██╗ §b██║      \n" +
                "                  §5██   ██╝ §b███████╗ \n" +
                "                  §5╚═════╝  §b╚══════╝ \n" +
                "\n" +
                "           §c¡RangerLevels detenido, gracias por usarlo!§r\n"
        );
    }

    // Getters
    public ExpConfig getExpConfig() { return ExpConfig.get(); }
    public Logger getLogger() { return LOGGER; }
    // Getters para inyectar en otras clases
    public IPlayerDataManager getDataManager() { return dataManager; }
    public IBackupManager getBackupManager() { return backupManager; }
    public AutoSaveTask getAutoSaveTask() { return autoSaveTask; }


    public void resetLimiterSchedule() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        ServerWorld overworld = server.getLevel(ServerWorld.OVERWORLD);
        if (overworld == null) return;

        long now = System.currentTimeMillis() / 1000L;
        long window = LimiterManager.getWindowSeconds();
        LimiterWorldData data = LimiterWorldData.get(overworld);
        data.setNextResetTime(now + window);
    }
}
