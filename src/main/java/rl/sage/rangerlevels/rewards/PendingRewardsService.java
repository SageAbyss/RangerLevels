package rl.sage.rangerlevels.rewards;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.util.LazyOptional;
import rl.sage.rangerlevels.capability.IPlayerRewards;
import rl.sage.rangerlevels.capability.PlayerRewardsProvider;
import rl.sage.rangerlevels.capability.RewardStatus;
import rl.sage.rangerlevels.config.RewardConfig;

public class PendingRewardsService {

    public static void unlockRewards(ServerPlayerEntity player, int level) {
        System.out.println("[Rewards] unlockRewards → nivel: " + level);

        LazyOptional<IPlayerRewards> opt = player.getCapability(PlayerRewardsProvider.REWARDS_CAP);
        opt.ifPresent(cap -> {
            RewardConfig cfg = RewardConfig.get();

            // EveryLevel
            if (cfg.EveryLevel) {
                for (String route : cfg.Rewards.EveryLevel.keySet()) {
                    RewardConfig.RouteRewards rr = cfg.Rewards.EveryLevel.get(route);
                    if (rr.Enable && RewardManager.routeAllowed(player, route)) {
                        String key = "EveryLevel:" + level + ":" + route;
                        cap.setStatus(key, RewardStatus.PENDING);
                        log(player, key);
                    }
                }
            }

// ——— Exact ———
            if (cfg.Exact) {
                String lvlKey = String.valueOf(level);
                if (cfg.Rewards.Exact.containsKey(lvlKey)) {
                    for (String route : cfg.Rewards.Exact.get(lvlKey).keySet()) {
                        if (!RewardManager.routeAllowed(player, route)) {
                            continue;
                        }
                        String key = "Exact:" + lvlKey + ":" + route;
                        cap.setStatus(key, RewardStatus.PENDING);
                        log(player, key);
                    }
                }
            }


// ——— Packages ———
            if (cfg.Packages) {
                for (String interval : cfg.Rewards.Packages.keySet()) {
                    int iv;
                    try {
                        iv = Integer.parseInt(interval);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                    if (level % iv == 0) {
                        for (String route : cfg.Rewards.Packages.get(interval).keySet()) {
                            if (!RewardManager.routeAllowed(player, route)) {
                                continue;
                            }
                            String key = "Packages:" + iv + ":" + level + ":" + route;
                            cap.setStatus(key, RewardStatus.PENDING);
                            log(player, key);
                        }
                    }
                }
            }

        });
    }

    private static void log(ServerPlayerEntity player, String key) {
        System.out.println("[Rewards] " + player.getName().getString() + " → " + key + " PENDING");
    }
}
