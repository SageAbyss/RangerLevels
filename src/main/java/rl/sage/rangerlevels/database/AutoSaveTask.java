package rl.sage.rangerlevels.database;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.config.ExpConfig;
import rl.sage.rangerlevels.capability.ILevel;

import static rl.sage.rangerlevels.util.DataSyncHelper.syncCapabilityToJson;

/**
 * Tarea de autosave: cada X ticks (definidos en Config.yml),
 * recorre todos los jugadores en línea y vuelca sus datos (nivel, exp, multiplier)
 * a Data.json (vía dataManager) y crea un backup individual.
 */
public class AutoSaveTask {

    private final RangerLevels mod;              // Referencia a tu clase principal
    private final IPlayerDataManager dataManager;
    private final IBackupManager backupManager;

    private int tickCounter = 0;

    public AutoSaveTask(RangerLevels mod) {
        this.mod = mod;
        this.dataManager   = mod.getDataManager();
        this.backupManager = mod.getBackupManager();
    }

    public void resetCounter() {
        this.tickCounter = 0;
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if (!ExpConfig.get().autoSave.enable) {
            tickCounter = 0;
            return;
        }

        tickCounter++;
        int intervalTicks = ExpConfig.get().autoSave.interval * 20;
        if (tickCounter < intervalTicks) return;

        tickCounter = 0;
        saveAllPlayers();
    }

    private void saveAllPlayers() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        // 1) Iniciamos el temporizador
        long startTime = System.currentTimeMillis();

        for (ServerPlayerEntity player : server.getPlayerList().getPlayers()) {
            // Sincronizamos la capability del jugador con el JSON
            syncCapabilityToJson(player);

            // Para depuración, si quieres ver datos individuales:
            ILevel cap = player.getCapability(ILevel.CAPABILITY).orElse(null);
            if (cap != null) {
                mod.getLogger().info("§8[AutoSave DEBUG] §aJugador={} lvl={} exp={}",
                        player.getName().getString(),
                        cap.getLevel(),
                        cap.getExp());
            }
        }

        // 2) Calculamos cuánto tiempo tardó en milisegundos
        long elapsed = System.currentTimeMillis() - startTime;

        // 3) Imprimimos el log final con el tiempo transcurrido
        mod.getLogger().info("§8[AutoSave] §aGuardado de datos completado (§f{}ms§a)", elapsed);
    }

}
