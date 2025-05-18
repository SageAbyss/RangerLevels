package rl.sage.rangerlevels.capability;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.RangerLevels;

/**
 * Copia tanto el nivel (LevelCapability) como el tier de pase (PassCapability)
 * al respawnear tras la muerte.
 */
@Mod.EventBusSubscriber(modid = RangerLevels.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerCloneHandler {

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        // Solo queremos copiar cuando el jugador muere
        if (!event.isWasDeath()) {
            return;
        }

        PlayerEntity original = event.getOriginal();
        PlayerEntity clone    = event.getPlayer();

        // --- Copia de LevelCapability ---
        original.getCapability(LevelProvider.LEVEL_CAP).ifPresent(oldLevelCap ->
                clone.getCapability(LevelProvider.LEVEL_CAP).ifPresent(newLevelCap -> {
                    newLevelCap.setLevel(oldLevelCap.getLevel());
                    newLevelCap.setExp(oldLevelCap.getExp());
                    newLevelCap.setPlayerMultiplier(oldLevelCap.getPlayerMultiplier());
                })
        );

        // --- Copia de PassCapability ---
        original.getCapability(PassCapabilityProvider.PASS_CAP).ifPresent(oldPassCap ->
                clone.getCapability(PassCapabilityProvider.PASS_CAP).ifPresent(newPassCap -> {
                    newPassCap.setTier(oldPassCap.getTier());
                })
        );
    }
}
