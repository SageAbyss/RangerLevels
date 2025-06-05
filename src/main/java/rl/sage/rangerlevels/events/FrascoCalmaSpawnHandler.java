package rl.sage.rangerlevels.events;

import com.pixelmonmod.pixelmon.api.events.spawning.PixelmonSpawnerEvent;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.items.frasco.FrascoCalmaHandler;

import java.util.List;

/**
 * Handler para aplicar “reducción de huida” a los Pixelmon salvajes cuando spawnean,
 * si cerca hay un jugador con efecto de Frasco de Calma activo.
 * No se aplica a Pokémon legendarios (incluyendo Ultra Beasts).
 */
@Mod.EventBusSubscriber(modid = RangerLevels.MODID)
public class FrascoCalmaSpawnHandler {

    /** Tag NBT que marca a un Pixelmon ya procesado por Calma. */
    private static final String NBT_CALMA_PROCESADO = "RangerCalmaProcesado";

    @SubscribeEvent
    public static void onPixelmonSpawner(PixelmonSpawnerEvent.Post ev) {
        PixelmonEntity wild = ev.getEntity();

        // 1) Si es legendario o Ultra Beast, no aplicar reducción
        Pokemon pkmnData = wild.getPokemon();
        if (pkmnData.getSpecies().isLegendary() || pkmnData.getSpecies().isUltraBeast()) {
            return;
        }

        // 2) Accedemos al NBT persistente de la entidad para marcarla
        CompoundNBT persist = wild.getPersistentData();

        // 3) Si ya lo procesamos antes, salimos
        if (persist.getBoolean(NBT_CALMA_PROCESADO)) {
            return;
        }

        // 4) Buscar jugadores cercanos (radio 30 bloques) con Frasco activo
        double mejorReduccion = 0.0;
        AxisAlignedBB area = wild.getBoundingBox().inflate(30.0);
        List<ServerPlayerEntity> jugadoresCercanos = wild.level.getEntitiesOfClass(
                ServerPlayerEntity.class,
                area
        );
        for (ServerPlayerEntity p : jugadoresCercanos) {
            double red = FrascoCalmaHandler.getReduccionActivo(p);
            if (red > mejorReduccion) {
                mejorReduccion = red;
            }
        }

        if (mejorReduccion <= 0.0) {
            return;
        }

        // 5) Marcamos al Pixelmon para no procesarlo de nuevo
        persist.putBoolean(NBT_CALMA_PROCESADO, true);

        // 6) Aplicamos la reducción de huida vía reflexión
        try {
            java.lang.reflect.Field runField = pkmnData.getClass().getDeclaredField("runChance");
            runField.setAccessible(true);
            double runBase = (double) runField.get(pkmnData);
            double nuevoRun = runBase * (1.0 - mejorReduccion);
            runField.set(pkmnData, nuevoRun);
        } catch (Exception ex) {
            RangerLevels.LOGGER.warn("No se pudo aplicar reducción de huida a Pixelmon: " + ex.getMessage());
        }
    }
}
