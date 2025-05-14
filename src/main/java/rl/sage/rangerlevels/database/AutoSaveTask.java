package rl.sage.rangerlevels.database;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.config.ExpConfig;
import rl.sage.rangerlevels.capability.ILevel;

import java.util.UUID;

public class AutoSaveTask {
    private final RangerLevels mod;
    private int tickCounter = 0;

    public AutoSaveTask(RangerLevels mod) {
        this.mod = mod;
    }
    public void resetCounter() {
        this.tickCounter = 0;
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // Cada tick chequeamos si está habilitado
        if (!ExpConfig.get().isAutoSaveEnabled()) {
            tickCounter = 0; // reseteamos el contador si lo deshabilitan
            return;
        }

        tickCounter++;
        // Leemos el intervalo dinámicamente
        int intervalTicks = ExpConfig.get().getAutoSaveInterval() * 20;

        if (tickCounter < intervalTicks) return;

        tickCounter = 0;
        saveAllPlayers();
    }

    private void saveAllPlayers() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        mod.getLogger().info("§8[AutoSave] §aGuardando datos de todos los jugadores");

        for (ServerPlayerEntity player : server.getPlayerList().getPlayers()) {
            ILevel cap = player.getCapability(ILevel.CAPABILITY).orElse(null);
            if (cap != null) {
                PlayerData data = new PlayerData(
                        player.getUUID(),
                        player.getName().getString(),
                        cap.getLevel(),
                        cap.getExp(),
                        cap.getPlayerMultiplier()
                );
                mod.getDataManager().savePlayerData(data);
                mod.getBackupManager().savePlayerData(data);
            }
        }
    }
}
