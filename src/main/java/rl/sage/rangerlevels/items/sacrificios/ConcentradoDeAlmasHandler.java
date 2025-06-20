// File: rl/sage/rangerlevels/items/sacrificios/ConcentradoDeAlmasHandler.java
package rl.sage.rangerlevels.items.sacrificios;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.events.CaptureEvent.StartCapture;
import com.pixelmonmod.pixelmon.api.events.CaptureEvent.SuccessfulCapture;
import com.pixelmonmod.pixelmon.api.pokemon.catching.CaptureValues;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class ConcentradoDeAlmasHandler {
    // Probabilidad de que el concentrado active el "sello" de captura (45%)
    private static final double SEAL_CHANCE = 0.15;
    // Bonus de XP para legendarios/ultra
    public static final double XP_BONUS_PCT = 0.30;
    private static final Random RANDOM = new Random();
    // Guardamos los jugadores cuyo capture fue “sellado”
    private static final Set<UUID> ACTIVATED = new HashSet<>();

    /** Registrar este handler en tu setup */
    public static void register() {
        Pixelmon.EVENT_BUS.register(ConcentradoDeAlmasHandler.class);
    }

    /**
     * Antes de que Pixelmon calcule la captura,
     * tratamos de aplicar el sello al 100% con probabilidad SEAL_CHANCE
     * y añadimos al jugador a ACTIVATED si se activa.
     */
    @SubscribeEvent
    public static void onStartCapture(StartCapture ev) {
        if (!(ev.getPlayer() instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) ev.getPlayer();

        // ¿Tiene ConcentradoDeAlmas en inventario?
        boolean hasItem = player.inventory.items.stream()
                .anyMatch(stack ->
                        !stack.isEmpty() &&
                                ConcentradoDeAlmas.ID.equals(RangerItemDefinition.getIdFromStack(stack))
                );
        if (!hasItem) return;

        // Tirada aleatoria
        if (RANDOM.nextDouble() <= SEAL_CHANCE) {
            CaptureValues vals = ev.getCaptureValues();
            // Forzamos la captura
            vals.setCatchRate(255);
            vals.setBallBonus(100.0);
            vals.setCaught();
            // Marcamos al jugador para el título
            ACTIVATED.add(player.getUUID());
        }
    }

    /**
     * Tras confirmarse la captura exitosa,
     * sólo mostramos el título si el jugador estaba en ACTIVATED.
     */
    @SubscribeEvent
    public static void onCaptureSuccess(SuccessfulCapture ev) {
        if (!(ev.getPlayer() instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) ev.getPlayer();
        UUID id = player.getUUID();

        // Si no estaba activado, salimos
        if (!ACTIVATED.remove(id)) {
            return;
        }

        // Mostramos el título
        PixelmonEntity pkmn = ev.getPokemon();
        String name = pkmn.getPokemon().getSpecies().getLocalizedName();
        StringTextComponent title    = new StringTextComponent("§6✦ Concentrado Activado ✦");
        StringTextComponent subtitle = new StringTextComponent("§a¡Capturaste un " + name + " con éxito!");
        player.connection.send(new STitlePacket(STitlePacket.Type.TIMES,    title, 10, 60, 20));
        player.connection.send(new STitlePacket(STitlePacket.Type.TITLE,    title));
        player.connection.send(new STitlePacket(STitlePacket.Type.SUBTITLE, subtitle));
        PlayerSoundUtils.playSoundToPlayer(
                player, SoundEvents.BEACON_DEACTIVATE, SoundCategory.PLAYERS, 1.0f, 1.0f
        );
    }

    /**
     * Mantiene el bonus de experiencia para Pokémon legendarios/ultra.
     */
    public static double getXpMultiplier(ServerPlayerEntity player, boolean isLegendaryOrUltra) {
        if (!isLegendaryOrUltra) return 1.0;

        boolean hasItem = player.inventory.items.stream()
                .anyMatch(stack ->
                        !stack.isEmpty() &&
                                ConcentradoDeAlmas.ID.equals(RangerItemDefinition.getIdFromStack(stack))
                );
        return hasItem ? 1.0 + XP_BONUS_PCT : 1.0;
    }
}
