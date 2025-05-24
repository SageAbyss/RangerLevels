package rl.sage.rangerlevels.rewards;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;
import rl.sage.rangerlevels.capability.IPassCapability;
import rl.sage.rangerlevels.capability.PassCapabilityProvider;
import rl.sage.rangerlevels.config.RewardConfig;

import java.util.Arrays;
import java.util.List;

public class RewardManager {
    // Orden de rutas de pase (se usa en routeAllowed)
    private static final List<String> ROUTES = Arrays.asList("Free", "Super", "Ultra", "Master");

    /**
     * Al subir de nivel, marca todas las recompensas correspondientes
     * (EveryLevel, Exact y Packages) como PENDING en el capability.
     */
    public static void handleLevelUp(ServerPlayerEntity player, int level) {
        PendingRewardsService.unlockRewards(player, level);
    }

    /**
     * Método opcional para reclamar todas las recompensas vía código.
     */
    public static void claimAll(ServerPlayerEntity player, int level) {
        handleLevelUp(player, level);
    }

    /**
     * Verifica si el jugador tiene al menos la ruta de pase solicitada.
     */
    static boolean routeAllowed(ServerPlayerEntity player, String route) {
        // Nivel de pase del jugador (0 = Free)
        final int[] playerTier = {0};
        LazyOptional<IPassCapability> capOpt =
                player.getCapability(PassCapabilityProvider.PASS_CAP);
        capOpt.ifPresent(cap -> playerTier[0] = cap.getTier());
        int routeTier = ROUTES.indexOf(route);
        return routeTier >= 0 && playerTier[0] >= routeTier;
    }

    /**
     * Ejecuta ítems y comandos de una ruta, luego reproduce el sonido que toque.
     */
    public static void executeRouteRewards(ServerPlayerEntity player,
                                           MinecraftServer server,
                                           RewardConfig.RouteRewards rr,
                                           String rewardType) {
        String playerName = player.getName().getString();

        // 1) Dar ítems
        if (rr.items != null) {
            for (String def : rr.items) {
                String[] parts = def.split(" ");
                String itemId = parts[0];
                String qty    = parts.length > 1 ? parts[1] : "1";
                server.getCommands().performCommand(
                        server.createCommandSourceStack(),
                        "give " + playerName + " " + itemId + " " + qty
                );
            }
        }

        // 2) Ejecutar comandos
        if (rr.commands != null) {
            for (String tmpl : rr.commands) {
                String cmd = tmpl.replace("{PLAYER}", playerName);
                server.getCommands().performCommand(
                        server.createCommandSourceStack(),
                        cmd
                );
            }
        }

        // 3) Reproducir sonido según tipo de recompensa
        String soundName;
        switch (rewardType) {
            case "Exact":
                soundName = RewardConfig.get().Sounds.Exact;
                break;
            case "Packages":
                soundName = RewardConfig.get().Sounds.Packages;
                break;
            default:
                soundName = RewardConfig.get().Sounds.EveryLevel;
                break;
        }
        SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(soundName));
        if (sound != null) {
            player.playSound(sound, 1.0F, 1.0F);
        }
    }
}
