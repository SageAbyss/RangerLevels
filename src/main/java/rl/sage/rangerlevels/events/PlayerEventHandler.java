package rl.sage.rangerlevels.events;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.capability.ILevel;
import rl.sage.rangerlevels.capability.LevelProvider;
import rl.sage.rangerlevels.database.PlayerData;
import rl.sage.rangerlevels.util.DataSyncHelper;

import java.util.UUID;

/**
 * Se encarga de:
 *   - Al entrar un jugador: cargar/crear PlayerData, copiar valores a la capacidad ILevel,
 *     y luego sincronizar esa capacidad en Data.json (incluyendo backup).
 *   - Al salir un jugador: tomar valores de la capacidad ILevel y volcar en Data.json (incluyendo backup).
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerEventHandler {

    private static final RangerLevels mod = RangerLevels.INSTANCE;

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

        UUID uuid = player.getUUID();
        String nickname = player.getName().getString();

        // 1) Obtener o crear PlayerData en memoria
        PlayerData data = mod.getDataManager().getOrCreate(uuid, nickname);

        // ---> LOG DE DEBUG justo después de getOrCreate
       /* mod.getLogger().info("§8[DEBUG] LOGIN:§a “{}” → dataPreLogin: nivel={}, exp={}, multiplier={}",
                nickname, data.getLevel(), data.getExp(), data.getMultiplier());
                */

        // 2) Copiar valores de PlayerData a la capacidad ILevel
        player.getCapability(ILevel.CAPABILITY).ifPresent(cap -> {
            cap.setLevel(data.getLevel());
            cap.setExp(data.getExp());
            cap.setPlayerMultiplier(data.getMultiplier());
        });

        // 3) Actualizar timestamp y nickname en el POJO
        data.setTimestamp(System.currentTimeMillis() / 1000L);
        data.setNickname(nickname);
        mod.getDataManager().savePlayerData(data);

        // 4) Sincronizar la capacidad a JSON (y backup) para reflejar cualquier cambio
        DataSyncHelper.syncCapabilityToJson(player);

       // mod.getLogger().info("Jugador conectado: “{}” → datos cargados y sincronizados en JSON", nickname);
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

        UUID uuid = player.getUUID();
        String nickname = player.getName().getString();

        // 1) Sincronizar la capacidad a JSON (y backup) usando los valores en memoria
        DataSyncHelper.syncCapabilityToJson(player);

       // mod.getLogger().info("Jugador desconectado: “{}” → datos sincronizados en JSON", nickname);
    }
}
