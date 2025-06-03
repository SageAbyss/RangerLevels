package rl.sage.rangerlevels.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.storage.FolderName;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.capability.LevelProvider;
import rl.sage.rangerlevels.capability.PassCapabilityProvider;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Comando /rlv resetAll
 *
 * Recorre todos los archivos de <mundo>/playerdata/*.dat y, en cada uno,
 * reinicia tanto el NBT de la capability de nivel/exp como el NBT de la capability de pase.
 */
public class CommandResetAll {

    /**
     * Ejecuta /rlv resetAll
     */
    public static int resetAll(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        // 1) Obtener el servidor actual
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            ctx.getSource().sendFailure(
                    new StringTextComponent("§cNo se pudo acceder al servidor.")
            );
            return 0;
        }

        // 2) Llamar al método que hace el trabajo de reiniciar ambas capabilities
        int afectados = resetAllPlayerCapabilities(server);

        // 3) Mensaje de confirmación al que ejecutó el comando
        ctx.getSource().sendSuccess(
                new StringTextComponent("§aSe ha reseteado Nivel, EXP y PASE a Free Pass para "
                        + afectados + " jugador(es)."),
                false
        );
        return 1;
    }

    /**
     * Recorre cada .dat en la carpeta playerdata y borra/reescribe
     * el tag NBT de la capability de nivel y la de pase (dentro de ForgeCaps).
     *
     * - Para jugadores online: actualiza ambas capabilities en memoria y marca
     *   needSaveOnlineData = true.
     * - Para jugadores offline: modifica directamente el .dat vía NBT.
     * - Al final, si needSaveOnlineData es true, llama a saveAllPlayerData().
     *
     * @param server la instancia de MinecraftServer
     * @return cuántos archivos/jugadores fueron modificados
     */
    private static int resetAllPlayerCapabilities(MinecraftServer server) {
        int contadorReseteados = 0;

        // 1) Obtener la ruta a <mundo>/playerdata
        Path playerDataFolder = server.getWorldPath(FolderName.PLAYER_DATA_DIR);

        // 2) Claves NBT: "ForgeCaps" y los nombres EXACTOS de tus capabilities
        final String FORGE_CAPS_KEY     = "ForgeCaps";
        final String LEVEL_CAP_KEY      = "rangerlevels:level";
        final String PASS_CAP_KEY       = "rangerlevels:pass";

        PlayerList playerList = server.getPlayerList();
        boolean needSaveOnlineData = false;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(playerDataFolder, "*.dat")) {
            for (Path path : stream) {
                // 3) Extraer el UUID de "<uuid>.dat"
                String fileName = path.getFileName().toString();
                if (!fileName.endsWith(".dat")) {
                    continue;
                }

                String uuidStr = fileName.substring(0, fileName.length() - 4);
                UUID uuid;
                try {
                    uuid = UUID.fromString(uuidStr);
                } catch (IllegalArgumentException e) {
                    RangerLevels.LOGGER.warn("Nombre de archivo no es un UUID válido: " + fileName);
                    continue;
                }

                // 4) Verificar si ese jugador está conectado (online)
                ServerPlayerEntity onlinePlayer = playerList.getPlayer(uuid);

                if (onlinePlayer != null) {
                    // ──────────── CASO A: JUGADOR ONLINE ────────────

                    // 4.a) Resetear LEVEL_CAP en memoria
                    onlinePlayer.getCapability(LevelProvider.LEVEL_CAP).ifPresent(cap -> {
                        cap.setLevel(1);
                        cap.setExp(0);
                        // Si hay otros campos (por ejemplo playerMultiplier), dejar en valor por defecto.
                    });

                    // 4.b) Resetear PASS_CAP en memoria
                    onlinePlayer.getCapability(PassCapabilityProvider.PASS_CAP).ifPresent(cap -> {
                        cap.setTier(0);       // Free Pass
                        cap.setExpiresAt(0L); // Sin expiración
                    });

                    // 4.c) Mensaje y sonido al jugador
                    onlinePlayer.sendMessage(
                            new StringTextComponent(
                                    "§a☑ §dTus estadísticas han sido reseteadas"
                            ),
                            onlinePlayer.getUUID()
                    );
                    PlayerSoundUtils.playSoundToPlayer(
                            onlinePlayer,
                            SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE,
                            SoundCategory.MASTER,
                            1.0f,
                            0.5f
                    );

                    // 4.d) Marcar que debemos guardar a todos online más tarde
                    needSaveOnlineData = true;
                    contadorReseteados++;
                    continue; // Pasamos al siguiente .dat
                }

                // ──────────── CASO B: JUGADOR OFFLINE ────────────
                // Modificamos directamente el archivo .dat vía NBT
                try {
                    CompoundNBT rootNBT = CompressedStreamTools.readCompressed(Files.newInputStream(path));

                    if (rootNBT.contains(FORGE_CAPS_KEY)) {
                        CompoundNBT forgeCaps = rootNBT.getCompound(FORGE_CAPS_KEY);
                        boolean modificado = false;

                        // 4.e) Reset LEVEL_CAP en el NBT
                        if (forgeCaps.contains(LEVEL_CAP_KEY)) {
                            CompoundNBT capLevelTag = forgeCaps.getCompound(LEVEL_CAP_KEY);
                            capLevelTag.putInt("level", 1);
                            capLevelTag.putDouble("exp", 0.0);
                            // Si hay otros campos (playerMultiplier, etc.), puedes dejarlos con su valor por defecto o ajustarlos aquí.

                            forgeCaps.put(LEVEL_CAP_KEY, capLevelTag);
                            modificado = true;
                        }

                        // 4.f) Reset PASS_CAP en el NBT
                        if (forgeCaps.contains(PASS_CAP_KEY)) {
                            CompoundNBT capPassTag = forgeCaps.getCompound(PASS_CAP_KEY);
                            capPassTag.putInt("tier", 0);
                            capPassTag.putLong("expiresAt", 0L);
                            // Si tu IPassCapability tiene más campos, resetea sólo los necesarios.

                            forgeCaps.put(PASS_CAP_KEY, capPassTag);
                            modificado = true;
                        }

                        if (modificado) {
                            rootNBT.put(FORGE_CAPS_KEY, forgeCaps);
                            CompressedStreamTools.writeCompressed(rootNBT, Files.newOutputStream(path));
                            contadorReseteados++;
                        }
                    }
                } catch (IOException ex) {
                    RangerLevels.LOGGER.warn(
                            "No se pudo leer/escribir el playerdata: " + path.getFileName(),
                            ex
                    );
                }
            }
        } catch (IOException e) {
            RangerLevels.LOGGER.error("Error accediendo a la carpeta playerdata", e);
        }

        // 5) Si actualizamos al menos un jugador online, guardamos todos los datos online
        if (needSaveOnlineData) {
            try {
                playerList.saveAll();
            } catch (Exception e) {
                RangerLevels.LOGGER.error("Error guardando datos de jugadores online", e);
            }
        }

        return contadorReseteados;
    }
}
