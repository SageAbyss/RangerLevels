package rl.sage.rangerlevels.util;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import com.pixelmonmod.pixelmon.init.registry.SoundRegistration;

/**
 * Utilidades para reproducir sonidos a jugadores en un mod server-side
 * para Forge 1.16.5 con Pixelmon Reforged.
 */
public class PlayerSoundUtils {

    /**
     * Reproduce un sonido de Minecraft o Pixelmon solo para el jugador indicado.
     */
    public static void playSoundToPlayer(ServerPlayerEntity player,
                                         SoundEvent sound,
                                         SoundCategory category,
                                         float volume,
                                         float pitch) {
        SPlaySoundEffectPacket packet = new SPlaySoundEffectPacket(
                sound,
                category,
                (float) player.getX(),
                (float) player.getY(),
                (float) player.getZ(),
                volume,
                pitch
        );
        player.connection.send(packet);
    }

    /**
     * Reproduce un sonido a partir de su ResourceLocation, útil para sonidos definidos dinámicamente.
     */
    public static void playSoundToPlayer(ServerPlayerEntity player,
                                         ResourceLocation soundRL,
                                         SoundCategory category,
                                         float volume,
                                         float pitch) {
        SoundEvent event = ForgeRegistries.SOUND_EVENTS.getValue(soundRL);
        if (event != null) {
            playSoundToPlayer(player, event, category, volume, pitch);
        } else {
            System.err.println("SoundEvent no encontrado: " + soundRL);
        }
    }

    /**
     * Conveniencia para reproducir sonidos registrados por Pixelmon,
     * usando directamente los RegistryObject<SoundEvent> de SoundRegistration.
     */
    public static void playPixelmonSoundToPlayer(ServerPlayerEntity player,
                                                 RegistryObject<SoundEvent> soundReg,
                                                 SoundCategory category,
                                                 float volume,
                                                 float pitch) {
        if (soundReg != null && soundReg.isPresent()) {
            SoundEvent event = soundReg.get();
            playSoundToPlayer(player, event, category, volume, pitch);
        } else {
            System.err.println("SoundEvent de Pixelmon no disponible: " + soundReg);
        }
    }

    /**
     * Reproduce un sonido de Minecraft para todos los jugadores en el servidor.
     */
    public static void playSoundToAllPlayers(MinecraftServer server,
                                             SoundEvent sound,
                                             SoundCategory category,
                                             float volume,
                                             float pitch) {
        for (ServerPlayerEntity player : server.getPlayerList().getPlayers()) {
            playSoundToPlayer(player, sound, category, volume, pitch);
        }
    }

    /**
     * Reproduce un sonido de Pixelmon para todos los jugadores en el servidor.
     */
    public static void playPixelmonSoundToAllPlayers(MinecraftServer server,
                                                     RegistryObject<SoundEvent> soundReg,
                                                     SoundCategory category,
                                                     float volume,
                                                     float pitch) {
        if (soundReg != null && soundReg.isPresent()) {
            SoundEvent sound = soundReg.get();
            playSoundToAllPlayers(server, sound, category, volume, pitch);
        } else {
            System.err.println("SoundEvent de Pixelmon no disponible: " + soundReg);
        }
    }
}
