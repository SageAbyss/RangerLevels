package rl.sage.rangerlevels;

import com.pixelmonmod.pixelmon.Pixelmon;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.server.permission.PermissionAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rl.sage.rangerlevels.commands.CommandRegistry;
import rl.sage.rangerlevels.capability.*;  // incluye IPassCapability, PassStorage, PassCapability, PassCapabilityProvider, PlayerRewardsProvider
import rl.sage.rangerlevels.config.ConfigLoader;
import rl.sage.rangerlevels.config.ExpConfig;
import rl.sage.rangerlevels.config.RewardConfig;
import rl.sage.rangerlevels.database.*;
import rl.sage.rangerlevels.events.ExpEventHandler;
import rl.sage.rangerlevels.events.PixelmonEventHandler;
import rl.sage.rangerlevels.limiter.LimiterManager;
import rl.sage.rangerlevels.limiter.LimiterWorldData;
import rl.sage.rangerlevels.pass.PassManager;
import rl.sage.rangerlevels.permissions.PermissionRegistrar;
import rl.sage.rangerlevels.util.GradientText;

@Mod(RangerLevels.MODID)
public class RangerLevels {
    public static final String MODID = "rangerlevels";
    public static final IFormattableTextComponent PREFIX =
            GradientText.of("RangerLevels", "#C397F1", "#A4DDE1", "#A671BD")
                    .withStyle(Style.EMPTY.withBold(true));
    private static final Logger LOGGER = LogManager.getLogger(MODID);
    public static RangerLevels INSTANCE;

    // Managers y tasks...
    private AutoSaveTask autoSaveTask;
    private IPlayerDataManager dataManager, backupManager;
    private MySQLManager mysqlManager;

    public RangerLevels() {
        INSTANCE = this;
        ExpConfig.load();  // Pre-carga de config

        // 1) Listener de setup
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::setup);

        // 2) Registra al bus de Forge tus handlers (no repitas PassCapabilityProvider)
        MinecraftForge.EVENT_BUS.register(this);                       // FMLServerStoppingEvent
        MinecraftForge.EVENT_BUS.register(ExpEventHandler.class);
        MinecraftForge.EVENT_BUS.register(CommandRegistry.class);
        Pixelmon.EVENT_BUS.register(PixelmonEventHandler.class);
        // El PassCapabilityProvider y PlayerRewardsAttach solo necesitan la anotación @EventBusSubscriber
    }

    private void setup(final FMLCommonSetupEvent event) {
        // **Aquí** registramos las capabilities, garantizando que la inyección ya ocurra
        LOGGER.info("Registrando capacidad de pase...");
        CapabilityManager.INSTANCE.register(
                IPassCapability.class,
                new PassStorage(),
                PassCapability::new
        );

        LOGGER.info("Registrando capacidad de recompensas...");
        PlayerRewardsProvider.register();

        // Carga configs, data managers, providers y permisos
        ConfigLoader.load();
        ExpConfig.load();
        RewardConfig.load();
        initializeDataManagers();

        LevelProvider.register();
        LimiterProvider.register();
        PassManager.registerPermissions();
        // No es necesario registrar de nuevo PlayerRewardsAttach, está anotado con @EventBusSubscriber

        // Tarea de autosave
        this.autoSaveTask = new AutoSaveTask(this);
        MinecraftForge.EVENT_BUS.register(this.autoSaveTask);

        LOGGER.info("¡RangerLevels listo!");
    }

    private void initializeDataManagers() {
        String dbType = ExpConfig.get().getDatabaseType().toLowerCase();
        if ("mysql".equals(dbType)) {
            mysqlManager = new MySQLManager(this);
            dataManager = mysqlManager;
            LOGGER.info("Usando MySQL como almacenamiento");
        } else {
            dataManager = new JSONBackupManager(this);
            LOGGER.info("Usando JSON local como almacenamiento");
        }
        backupManager = new JSONBackupManager(this);
        LOGGER.info("Sistema de respaldo JSON activo");
    }

    @SubscribeEvent
    public void onServerStopping(FMLServerStoppingEvent event) {
        if (dataManager   != null) dataManager.close();
        if (backupManager != null) backupManager.close();
        if (mysqlManager  != null) mysqlManager.close();
        LOGGER.info("Servidor detenido, recursos liberados y datos guardados.");
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
