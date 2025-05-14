// src/main/java/rl/sage/rangerlevels/rewards/RewardManager.java
package rl.sage.rangerlevels.rewards;

import net.minecraft.server.MinecraftServer;
import net.minecraft.entity.player.ServerPlayerEntity;
import rl.sage.rangerlevels.config.RewardConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RewardManager {

    /**
     * Se llama al subir de nivel para ejecutar las recompensas según configuración.
     */
    public static void handleLevelUp(ServerPlayerEntity player, int newLevel) {
        RewardConfig cfg = RewardConfig.get();
        String playerName = player.getName().getString();
        MinecraftServer server = player.getCommandSenderWorld().getServer();

        // EveryLevel
        if (cfg.EveryLevel) {
            executeRewards(playerName, server, cfg.Rewards.EveryLevel);
        }

        // Exact
        RewardConfig.LevelRewards exact = cfg.Rewards.Exact.get(String.valueOf(newLevel));
        if (exact != null) {
            executeRewards(playerName, server, exact);
        }

        // Packages
        if (cfg.Packages) {
            for (Map.Entry<String, RewardConfig.LevelRewards> entry : cfg.Rewards.Packages.entrySet()) {
                int interval;
                try {
                    interval = Integer.parseInt(entry.getKey());
                } catch (NumberFormatException e) {
                    continue;
                }
                if (interval > 0 && newLevel % interval == 0) {
                    executeRewards(playerName, server, entry.getValue());
                }
            }
        }
    }

    /**
     * Ejecuta el bloque de recompensas (dar items + comandos).
     */
    public static void executeRewards(String playerName, MinecraftServer server, RewardConfig.LevelRewards rewards) {
        List<String> given = new ArrayList<>();

        // Dar ítems
        for (String def : rewards.items) {
            String[] parts = def.split(" ");
            String itemId = parts[0];
            String qty    = parts.length > 1 ? parts[1] : "1";
            server.getCommands().performCommand(
                    server.createCommandSourceStack(),
                    "give " + playerName + " " + itemId + " " + qty
            );
            given.add(itemId + " x" + qty);
        }

        String itemsList = String.join(", ", given);

        // Ejecutar comandos
        for (String tmpl : rewards.commands) {
            String cmd = tmpl
                    .replace("{PLAYER}", playerName)
                    .replace("{ITEMS}", itemsList);
            server.getCommands().performCommand(
                    server.createCommandSourceStack(),
                    cmd
            );
        }
    }

    /**
     * Devuelve las definiciones de EveryLevel (lista de strings).
     */
    public static List<String> getUnlockedEveryLevel(ServerPlayerEntity player, int level) {
        RewardConfig cfg = RewardConfig.get();
        if (!cfg.EveryLevel) {
            return new ArrayList<String>();
        }
        return new ArrayList<String>(cfg.Rewards.EveryLevel.items);
    }

    /**
     * Devuelve las definiciones de ExactLevel para el nivel dado.
     */
    public static List<String> getUnlockedExactLevel(ServerPlayerEntity player, int level) {
        RewardConfig.LevelRewards exact = RewardConfig.get().Rewards.Exact.get(String.valueOf(level));
        if (exact == null) {
            return new ArrayList<String>();
        }
        return new ArrayList<String>(exact.items);
    }

    /**
     * Devuelve las definiciones de Packages para el nivel dado.
     */
    public static List<String> getUnlockedPackages(ServerPlayerEntity player, int level) {
        RewardConfig cfg = RewardConfig.get();
        List<String> out = new ArrayList<String>();
        if (!cfg.Packages) {
            return out;
        }
        for (Map.Entry<String, RewardConfig.LevelRewards> entry : cfg.Rewards.Packages.entrySet()) {
            int interval;
            try {
                interval = Integer.parseInt(entry.getKey());
            } catch (NumberFormatException e) {
                continue;
            }
            if (interval > 0 && level % interval == 0) {
                out.addAll(entry.getValue().items);
            }
        }
        return out;
    }

    /**
     * ¿Hay al menos una recompensa desbloqueada de cualquier tipo?
     */
    public static boolean hasAnyUnlocked(ServerPlayerEntity player, int level) {
        return !getUnlockedEveryLevel(player, level).isEmpty()
                || !getUnlockedExactLevel(player, level).isEmpty()
                || !getUnlockedPackages(player, level).isEmpty();
    }

    /**
     * Reclama todas las recompensas (EveryLevel + Exact + Packages) de una sola vez.
     */
    public static void claimAll(ServerPlayerEntity player, int level) {
        RewardConfig cfg = RewardConfig.get();
        String playerName = player.getName().getString();
        MinecraftServer server = player.getCommandSenderWorld().getServer();

        // EveryLevel
        if (cfg.EveryLevel) {
            executeRewards(playerName, server, cfg.Rewards.EveryLevel);
        }
        // Exact
        RewardConfig.LevelRewards exact = cfg.Rewards.Exact.get(String.valueOf(level));
        if (exact != null) {
            executeRewards(playerName, server, exact);
        }
        // Packages
        if (cfg.Packages) {
            for (Map.Entry<String, RewardConfig.LevelRewards> entry : cfg.Rewards.Packages.entrySet()) {
                int interval;
                try {
                    interval = Integer.parseInt(entry.getKey());
                } catch (NumberFormatException e) {
                    continue;
                }
                if (interval > 0 && level % interval == 0) {
                    executeRewards(playerName, server, entry.getValue());
                }
            }
        }
    }
}
