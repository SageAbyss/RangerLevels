package rl.sage.rangerlevels.util;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import rl.sage.rangerlevels.config.ExpConfig;

/**
 * Funciones auxiliares para control de mundos.
 */
public class WorldUtils {

    private WorldUtils() { /* no instancies */ }

    /**
     * Obtiene el nombre de la dimensión actual del jugador (por ejemplo: overworld, the_nether, the_end, ultra_space, drowned)
     */
    public static String getDimensionName(ServerPlayerEntity player) {
        RegistryKey<World> dimKey = player.level.dimension();
        ResourceLocation dimensionId = dimKey.location(); // Por ejemplo: "minecraft:overworld" o "pixelmon:ultra_space"
        return dimensionId.getPath(); // Solo "overworld", "ultra_space", etc.
    }

    /**
     * Comprueba si este jugador puede recibir EXP en su dimensión actual,
     * según ExpConfig.worlds (lista de nombres de dimensión).
     */
    public static boolean isWorldAllowed(ServerPlayerEntity player) {
        String dimensionName = getDimensionName(player);
        return ExpConfig.isWorldAllowed(dimensionName);
    }
}
