package rl.sage.rangerlevels.capability;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.util.LazyOptional;

/**
 * Utilidad para obtener la capability de pase de un jugador (solo servidor).
 */
public class PassCapabilities {
    /**
     * Obtiene la capability de pase de un jugador (servidor).
     *
     * @param player instancia de ServerPlayerEntity
     * @return IPassCapability adjunta al jugador
     * @throws IllegalStateException si por alguna razón no está presente.
     */
    public static IPassCapability get(ServerPlayerEntity player) {
        LazyOptional<IPassCapability> opt = player.getCapability(PassCapabilityProvider.PASS_CAP);
        return opt.orElseThrow(() ->
                new IllegalStateException("PassCapability no adjunta al jugador: " + player.getName().getString())
        );
    }
}
