package rl.sage.rangerlevels.database;

import java.util.UUID;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.capability.ILevel;

@Mod.EventBusSubscriber(modid = RangerLevels.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DBPlayerHandler {

    @SubscribeEvent
    public static void onLoadFromFile(PlayerEvent.LoadFromFile event) {
        if (!(event.getPlayer() instanceof ServerPlayerEntity)) {
            return;
        }
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        RangerLevels mod = RangerLevels.INSTANCE;

        UUID uuid = player.getUUID();
        String nickname = player.getName().getString();
        PlayerData pd = mod.getDataManager().loadPlayerData(uuid, nickname);

        // Comprobamos si la capacidad ILevel está disponible
        player.getCapability(ILevel.CAPABILITY).ifPresent(cap -> {
            cap.setLevel(pd.getLevel());
            cap.setExp(pd.getExp());
            cap.setPlayerMultiplier(pd.getMultiplier());
        });

        mod.getLogger().info("[DB] Cargado desde {}: {} (Lvl:{} XP:{})",
                mod.getDataManager().getSourceName(), nickname, pd.getLevel(), pd.getExp());
    }

    @SubscribeEvent
    public static void onSaveToFile(PlayerEvent.SaveToFile event) {
        if (!(event.getPlayer() instanceof ServerPlayerEntity)) {
            return;
        }
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        RangerLevels mod = RangerLevels.INSTANCE;

        // Comprobamos si la capacidad ILevel está disponible
        player.getCapability(ILevel.CAPABILITY).ifPresent(cap -> {
            PlayerData pd = new PlayerData(
                    player.getUUID(),
                    player.getName().getString(),
                    cap.getLevel(),
                    cap.getExp(),
                    cap.getPlayerMultiplier()
            );
            mod.getDataManager().savePlayerData(pd);
            mod.getBackupManager().savePlayerData(pd);
            mod.getLogger().info("[DB] Guardado en {}: {} (Lvl:{} XP:{})",
                    mod.getDataManager().getSourceName(), pd.getNickname(), pd.getLevel(), pd.getExp());
        });
    }
}
