package rl.sage.rangerlevels.capability;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.config.RewardConfig;

import java.util.Map;

@Mod.EventBusSubscriber
public class PlayerRewardsEvents {

    private static final String INIT_TAG = "RangerLevels:RewardsInit";

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

        // Solo inicializa la primera vez que entra (o tras borrarse sus NBT)
        if (player.getPersistentData().getBoolean(INIT_TAG)) return;
        player.getPersistentData().putBoolean(INIT_TAG, true);

        player.getCapability(PlayerRewardsProvider.REWARDS_CAP).ifPresent(cap -> {
            RewardConfig cfg = RewardConfig.get();
            // EveryLevel
            for (String route : cfg.Rewards.EveryLevel.keySet()) {
                String key = "EveryLevel:" + route;
                boolean enable = cfg.Rewards.EveryLevel.get(route).Enable;
                cap.setStatus(key, enable ? RewardStatus.UNBLOCKED : RewardStatus.BLOCKED);
            }
            // Exact
            for (String lvl : cfg.Rewards.Exact.keySet()) {
                for (String route : cfg.Rewards.Exact.get(lvl).keySet()) {
                    String key = "Exact:" + lvl + ":" + route;
                    cap.setStatus(key, RewardStatus.BLOCKED);
                }
            }
            // Packages
            for (String interval : cfg.Rewards.Packages.keySet()) {
                for (String route : cfg.Rewards.Packages.get(interval).keySet()) {
                    String key = "Packages:" + interval + ":" + route;
                    cap.setStatus(key, RewardStatus.BLOCKED);
                }
            }
        });
    }
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        // Solo copiar si es por muerte
        if (!event.isWasDeath()) return;
        event.getOriginal().getCapability(PlayerRewardsProvider.REWARDS_CAP).ifPresent(oldCap ->
                event.getPlayer().getCapability(PlayerRewardsProvider.REWARDS_CAP).ifPresent(newCap -> {
                    // Copiamos todo el mapa
                    for (Map.Entry<String, RewardStatus> e : oldCap.getStatusMap().entrySet()) {
                        newCap.setStatus(e.getKey(), e.getValue());
                    }
                })
        );
    }
}

