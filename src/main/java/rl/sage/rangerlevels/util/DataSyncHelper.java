package rl.sage.rangerlevels.util;

import net.minecraft.entity.player.ServerPlayerEntity;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.capability.LevelProvider;
import rl.sage.rangerlevels.database.PlayerData;

import java.util.UUID;

/**
 * Clase auxiliar para sincronizar los datos de la capacidad ILevel de un jugador
 * con el archivo Data.json (y su backup).
 */
public final class DataSyncHelper {

    private DataSyncHelper() {
        // Constructor privado para evitar instanciación
    }

    /**
     * Lee la capacidad {@code ILevel} del jugador y vuelca sus valores (nivel, exp, multiplier)
     * en Data.json, además de generar un backup individual.
     *
     * @param player el jugador cuyo ILevel se quiere guardar en JSON
     */
    public static void syncCapabilityToJson(ServerPlayerEntity player) {
        player.getCapability(LevelProvider.LEVEL_CAP).ifPresent(cap -> {
            UUID uuid       = player.getUUID();
            String nickname = player.getName().getString();

            // 1) Construir un PlayerData con los valores actuales de la capacidad
            PlayerData data = new PlayerData(
                    uuid,
                    nickname,
                    cap.getLevel(),
                    cap.getExp(),
                    cap.getPlayerMultiplier(),
                    System.currentTimeMillis() / 1000L  // timestamp en segundos
            );

            // 2) Guardar en Data.json
            RangerLevels.INSTANCE.getDataManager().savePlayerData(data);

            // 3) Crear backup individual en config/rangerlevels/backups/
            RangerLevels.INSTANCE.getBackupManager().savePlayerData(data);
        });
    }
}
