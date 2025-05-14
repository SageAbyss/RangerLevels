package rl.sage.rangerlevels;

import com.pixelmonmod.pixelmon.Pixelmon;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import rl.sage.rangerlevels.capability.LevelProvider;
import rl.sage.rangerlevels.capability.LimiterProvider;
import rl.sage.rangerlevels.commands.CommandRegistry;
import rl.sage.rangerlevels.config.ConfigLoader;
import rl.sage.rangerlevels.config.ExpConfig;
import rl.sage.rangerlevels.database.AutoSaveTask;
import rl.sage.rangerlevels.database.IPlayerDataManager;
import rl.sage.rangerlevels.database.JSONBackupManager;
import rl.sage.rangerlevels.database.MySQLManager;
import rl.sage.rangerlevels.events.ExpEventHandler;
import rl.sage.rangerlevels.events.PixelmonEventHandler;
import rl.sage.rangerlevels.limiter.LimiterWorldData;
import rl.sage.rangerlevels.limiter.LimiterManager;
import rl.sage.rangerlevels.pass.PassManager;
import rl.sage.rangerlevels.util.GradientText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(RangerLevels.MODID)
public class RangerLevels {
    public static final String MODID = "rangerlevels";
    public static final IFormattableTextComponent PREFIX =
            GradientText.of("RangerLevels", "#C397F1", "#A4DDE1", "#A671BD")
                    .withStyle(Style.EMPTY.withBold(true));

    private static final Logger LOGGER = LogManager.getLogger(MODID);
    public static RangerLevels INSTANCE;

    private IPlayerDataManager dataManager;
    private IPlayerDataManager backupManager;
    private MySQLManager mysqlManager;
    private AutoSaveTask autoSaveTask;


    public RangerLevels() {
        INSTANCE = this;
        ExpConfig.load();

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::setup);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new CommandRegistry());
        MinecraftForge.EVENT_BUS.register(ExpEventHandler.class);
        Pixelmon.EVENT_BUS.register(new PixelmonEventHandler());
    }

    private void setup(final FMLCommonSetupEvent event) {
        ConfigLoader.load();
        ExpConfig.load();
        initializeDataManagers();

        LevelProvider.register();
        LimiterProvider.register();
        PassManager.registerPermissions();

        this.autoSaveTask = new AutoSaveTask(this);
        MinecraftForge.EVENT_BUS.register(this.autoSaveTask);

        LOGGER.info("§7§m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        LOGGER.info(PREFIX.copy().append(new StringTextComponent(" §7- §aInicio del servidor")).getString());
        LOGGER.info("§aCARGANDO §eSISTEMA DE NIVELES §ay §eCONFIGURACIONES...");
        LOGGER.info("§d¡LISTOS PARA LA ACCIÓN!");
        LOGGER.info("§7§m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
    }

    private void initializeDataManagers() {
        String dbType = ExpConfig.get().getDatabaseType().toLowerCase();
        if ("mysql".equals(dbType)) {
            mysqlManager = new MySQLManager(this);
            dataManager  = mysqlManager;
            LOGGER.info("[RangerLevels] Usando MySQL para almacenamiento");
        } else {
            dataManager = new JSONBackupManager(this);
            LOGGER.info("[RangerLevels] Usando JSON local para almacenamiento");
        }
        backupManager = new JSONBackupManager(this);
        LOGGER.info("[RangerLevels] Sistema de respaldo JSON activo");
    }

    @SubscribeEvent
    public void onServerStopping(FMLServerStoppingEvent event) {
        shutdownSystems();
        LOGGER.info("§7§m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        LOGGER.info(PREFIX.copy().append(new StringTextComponent(" §7- §aCierre del servidor")).getString());
        LOGGER.info("§6LIBERANDO §eRECURSOS §6Y §eGUARDANDO §6DATOS...");
        LOGGER.info("§a¡MISIÓN CUMPLIDA! §7Nos vemos la proxima!");
        LOGGER.info("§7§m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
    }

    private void shutdownSystems() {
        if (dataManager   != null) dataManager.close();
        if (backupManager != null) backupManager.close();
        if (mysqlManager  != null) mysqlManager.close();
    }

    // Getters
    public ExpConfig getExpConfig() { return ExpConfig.get(); }
    public Logger getLogger() { return LOGGER; }
    public IPlayerDataManager getDataManager() { return dataManager; }
    public IPlayerDataManager getBackupManager() { return backupManager; }
    public MySQLManager getMySQL() { return mysqlManager; }
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

        LOGGER.info("§8[Limiter] §aReseteando el tiempo: próximo reset en {} segundos", window);
    }
}
