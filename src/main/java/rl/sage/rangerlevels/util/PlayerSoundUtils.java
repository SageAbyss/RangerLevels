package rl.sage.rangerlevels.util;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

/**
 * Utilidades para reproducir sonidos únicamente a un jugador específico en Forge 1.16.5.
 */
public class PlayerSoundUtils {

    /**
     * Reproduce un sonido solo para el jugador indicado.
     *
     * @param player   el ServerPlayerEntity que recibirá el sonido
     * @param sound    el SoundEvent a reproducir (p. ej. SoundEvents.BLOCK_NOTE_BLOCK_PLING)
     * @param category la categoría del sonido (p. ej. SoundCategory.BLOCKS)
     * @param volume   volumen (1.0f = normal)
     * @param pitch    pitch (1.0f = normal)
     */
    public static void playSoundToPlayer(ServerPlayerEntity player,
                                         SoundEvent sound,
                                         SoundCategory category,
                                         float volume,
                                         float pitch) {
        // Construimos el paquete SPlaySoundEffectPacket usando la posición actual del jugador
        SPlaySoundEffectPacket packet = new SPlaySoundEffectPacket(
                sound,
                category,
                (float) player.getX(),
                (float) player.getY(),
                (float) player.getZ(),
                volume,
                pitch
        );
        // Enviamos el paquete solo a ese jugador
        player.connection.send(packet);
    }
}
