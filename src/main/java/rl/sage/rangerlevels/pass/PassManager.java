package rl.sage.rangerlevels.pass;

import net.minecraft.entity.player.ServerPlayerEntity;
import rl.sage.rangerlevels.capability.IPassCapability;
import rl.sage.rangerlevels.capability.PassCapabilities;

import java.util.Arrays;
import java.util.List;

/**
 * Gestiona los diferentes tipos de pase **solo** en base al valor
 * almacenado en la capability (temporal). El pase FREE (tier=0) se
 * usa cuando no hay pase activo o expiró.
 */
public class PassManager {

    /** Orden de prioridad para mapear un número de tier a PassType */
    private static final List<PassType> PRIORITY = Arrays.asList(
            PassType.MASTER,
            PassType.ULTRA,
            PassType.SUPER,
            PassType.FREE
    );

    /**
     * Obtiene el pase **actual** del jugador, basándose únicamente
     * en la capability (número de tier + expiración). Si no hay
     * pase o ya expiró, retorna PassType.FREE.
     */
    public static PassType getCurrentPass(ServerPlayerEntity player) {
        IPassCapability cap = PassCapabilities.get(player);

        // Si la capability indica "activo" y no expiró, devolvemos el PassType correspondiente:
        if (cap.hasActivePass()) {
            int tier = cap.getTier();
            for (PassType type : PassType.values()) {
                if (type.getTier() == tier) {
                    return type;
                }
            }
            // Si por algún motivo el tier no coincide, limpiamos la capability y consideramos FREE:
            cap.setTier(0);
            cap.setExpiresAt(0L);
            return PassType.FREE;
        }

        // Si no hay pase activo o ya expiró, consideramos que está en FREE:
        return PassType.FREE;
    }

    /**
     * Verifica si el jugador tiene acceso a un pase de nivel “required” o superior,
     * comparando su pase actual (solo temporal).
     *
     * @param player   ServerPlayerEntity
     * @param required tipo de pase mínimo requerido
     * @return true si el pase actual (temporal) es >= required; false de lo contrario
     */
    public static boolean hasAccessTo(ServerPlayerEntity player, PassType required) {
        PassType actual = getCurrentPass(player);
        return actual.getTier() >= required.getTier();
    }
}
