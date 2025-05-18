package rl.sage.rangerlevels.purge;

import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerPurgeNotifier {
    // Concurrencia porque puede usarse en hilos de tick / evento
    private static final Set<UUID> notified = ConcurrentHashMap.newKeySet();

    /** @return true si este jugador ya recibió el aviso */
    public static boolean hasNotified(ServerPlayerEntity player) {
        return notified.contains(player.getUUID());
    }

    /** Marca al jugador como ya notificado */
    public static void markNotified(ServerPlayerEntity player) {
        notified.add(player.getUUID());
    }
}
