package rl.sage.rangerlevels.capability;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Se encarga de copiar los valores de ILevel al respawnear (PlayerEvent.Clone),
 * sin duplicar la adjunción de capabilities ni registros.
 */
@Mod.EventBusSubscriber(modid = "rangerlevels", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerCloneHandler {

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        // Sólo nos interesa cuando es por muerte
        if (!event.isWasDeath()) {
            return;
        }

        PlayerEntity original = event.getOriginal();
        PlayerEntity clone    = event.getPlayer();

        // Copiamos sólo la capacidad de nivel
        original.getCapability(LevelProvider.LEVEL_CAP).ifPresent(oldCap ->
                clone.getCapability(LevelProvider.LEVEL_CAP).ifPresent(newCap -> {
                    newCap.setLevel(oldCap.getLevel());
                    newCap.setExp(oldCap.getExp());
                    // Si guardas también multiplier:
                    newCap.setPlayerMultiplier(oldCap.getPlayerMultiplier());
                })
        );
    }
}
