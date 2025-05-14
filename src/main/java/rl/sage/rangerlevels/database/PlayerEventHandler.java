// PlayerEventHandler.java
package rl.sage.rangerlevels.database;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import org.apache.logging.log4j.Logger;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.capability.ILevel;

public class PlayerEventHandler {
    private final IPlayerDataManager dataManager;
    private final IPlayerDataManager backupManager;
    private final Map<UUID, PlayerData> cache;
    private final Logger log;

    public PlayerEventHandler() {
        this.dataManager = RangerLevels.INSTANCE.getDataManager();
        this.backupManager = RangerLevels.INSTANCE.getBackupManager();
        this.cache = new ConcurrentHashMap<>();
        this.log = RangerLevels.INSTANCE.getLogger();
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayerEntity)) {
            return;
        }
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        UUID uuid = player.getUUID();
        String name = player.getName().getString();
        PlayerData pd = dataManager.loadPlayerData(uuid, name);
        cache.put(uuid, pd);

        ILevel cap = player.getCapability(ILevel.CAPABILITY).orElse(null);
        if (cap != null) {
            cap.setLevel(pd.getLevel());
            cap.setExp(pd.getExp());
            cap.setPlayerMultiplier(pd.getMultiplier());
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayerEntity)) {
            return;
        }
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        UUID uuid = player.getUUID();
        PlayerData pd = cache.get(uuid);

        ILevel cap = player.getCapability(ILevel.CAPABILITY).orElse(null);
        if (cap != null && pd != null) {
            pd.setLevel(cap.getLevel());
            pd.setExp(cap.getExp());
            pd.setMultiplier(cap.getPlayerMultiplier());
            dataManager.savePlayerData(pd);
            backupManager.savePlayerData(pd);
        }
        cache.remove(uuid);
    }

    @SubscribeEvent
    public void onServerStopping(FMLServerStoppingEvent event) {
        MinecraftServer server = event.getServer();
        for (ServerPlayerEntity player : server.getPlayerList().getPlayers()) {
            UUID uuid = player.getUUID();
            PlayerData pd = cache.get(uuid);
            ILevel cap = player.getCapability(ILevel.CAPABILITY).orElse(null);
            if (cap != null && pd != null) {
                pd.setLevel(cap.getLevel());
                pd.setExp(cap.getExp());
                pd.setMultiplier(cap.getPlayerMultiplier());
                dataManager.savePlayerData(pd);
                backupManager.savePlayerData(pd);
            }
        }
        log.info("[RangerLevels] Todos los datos guardados al detener servidor");
    }
}