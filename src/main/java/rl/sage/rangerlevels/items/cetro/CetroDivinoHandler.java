package rl.sage.rangerlevels.items.cetro;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.api.storage.StoragePosition;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

/**
 * Listener de uso para Cetro Divino (Común, Épico, Mítico), adaptado a Pixelmon 9.1.12.
 * - Identifica el ítem (ID “RangerID”).
 * - Común/Épico: cura 50% de HP (jugador + party) y restaura 50% de PP.
 * - Mítico: si no está en cooldown, cura completamente (HP + PP) usando heal(); si está en cooldown, informa tiempo.
 *
 * Se usa StorageProxy.getParty(player) para obtener el PlayerPartyStorage.
 * Al modificar un Pokémon, se usa party.notifyListeners(pos, pkm) con StoragePosition obtenido desde party.getPosition(pkm).
 */
@Mod.EventBusSubscriber(modid = RangerLevels.MODID)
public class CetroDivinoHandler {

    @SubscribeEvent
    public static void onRightClickCetro(PlayerInteractEvent.RightClickItem event) {
        if (event.getWorld().isClientSide()) return;
        if (!(event.getPlayer() instanceof ServerPlayerEntity)) return;

        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        ItemStack held = player.getItemInHand(event.getHand());
        if (held == null || held.isEmpty()) return;

        // 1) Obtener ID NBT “RangerID”
        String id = RangerItemDefinition.getIdFromStack(held);
        if (id == null) return;

        // 2) Cancelamos el uso vanilla y marcamos SUCCESS para anular comportamiento por defecto
        event.setCanceled(true);
        event.setCancellationResult(ActionResultType.SUCCESS);

        switch (id) {
            case CetroDivinoComun.ID:
                usarCetroComun(player, held);
                break;
            case CetroDivinoEpico.ID:
                usarCetroEpico(player, held);
                break;
            case CetroDivinoMitico.ID:
                usarCetroMitico(player, held);
                break;
            default:
                // No es un Cetro Divino reconocido
                break;
        }
    }

    // —— GETTERS PARA MENSAJES ——————————————————————————————————————————————————————————

    /** Retorna el mensaje de “uso exitoso” para el Cetro Común */
    public static String getMensajeUsoComun(double porcHp, int usosRestantes) {
        String titulo = TextFormatting.DARK_GREEN + "✦ Cetro Divino (Común) Usado ✦";
        String linea1 = TextFormatting.GREEN + "❖ HP restaurado: §b" + (int) (porcHp * 100) + "%";
        String linea2 = TextFormatting.GREEN + "❖ PP del equipo: §b" + (int) (porcHp * 100) + "%";
        String linea3 = TextFormatting.YELLOW + "❖ Usos restantes: §e" + usosRestantes;
        return titulo + "\n" + linea1 + "\n" + linea2 + "\n" + linea3;
    }

    /** Retorna el mensaje de “se acabaron los usos” para el Cetro Común */
    public static String getMensajeSinUsosComun() {
        return TextFormatting.RED + "✦ Ya no quedan usos de tu Cetro Divino (Común).";
    }

    /** Retorna el mensaje de “uso exitoso” para el Cetro Épico */
    public static String getMensajeUsoEpico(double porcHp, int usosRestantes) {
        String titulo = TextFormatting.DARK_PURPLE + "✦ Cetro Divino (Épico) Usado ✦";
        String linea1 = TextFormatting.GREEN + "❖ HP restaurado: §b" + (int) (porcHp * 100) + "%";
        String linea2 = TextFormatting.GREEN + "❖ PP del equipo: §b" + (int) (porcHp * 100) + "%";
        String linea3 = TextFormatting.YELLOW + "❖ Usos restantes: §e" + usosRestantes;
        return titulo + "\n" + linea1 + "\n" + linea2 + "\n" + linea3;
    }

    /** Retorna el mensaje de “se acabaron los usos” para el Cetro Épico */
    public static String getMensajeSinUsosEpico() {
        return TextFormatting.RED + "✦ Ya no quedan usos de tu Cetro Divino (Épico).";
    }

    // —— MÉTODOS PRINCIPALES ——————————————————————————————————————————————————————————————

    /**
     * Cetro Divino (Común):
     * - 50% HP curado (jugador + cada Pokémon de la party)
     * - Restaurar 50% de PP de cada movimiento
     * - 5 usos totales (gestionados en CetroDivinoComun)
     */
    private static void usarCetroComun(ServerPlayerEntity player, ItemStack stack) {
        int usosRestantes = CetroDivinoComun.getUsosRestantes(stack);
        if (usosRestantes <= 0) {
            // Si ya estaba a 0, mostramos mensaje y eliminamos
            player.sendMessage(new StringTextComponent(getMensajeSinUsosComun()), player.getUUID());
            PlayerSoundUtils.playSoundToPlayer(
                    player, SoundEvents.ITEM_BREAK, SoundCategory.PLAYERS, 0.7f, 1.0f
            );
            player.getServer().execute(() -> stack.shrink(1));
            return;
        }

        // 1) Decrementar uso:
        CetroDivinoComun.decrementarUso(stack);

        // 2) Ahora que ya restamos el uso, recalculamos cuántos quedan:
        usosRestantes = CetroDivinoComun.getUsosRestantes(stack);

        // 3) Aplicamos el efecto de curación
        double porcHp = CetroDivinoComun.getHpRestorePorc(); // 0.50
        // 3.1) Curar 50% de HP al jugador
        float vidaAct = player.getHealth();
        float vidaMax = player.getMaxHealth();
        float curar = (float) (vidaMax * porcHp);
        player.setHealth(Math.min(vidaAct + curar, vidaMax));

        // 3.2) Curar a cada Pokémon de la party
        PlayerPartyStorage party = StorageProxy.getParty(player);
        if (party != null) {
            double porcPp = CetroDivinoComun.getPpRestorePorc(); // 0.50
            for (Pokemon pkm : party.getTeam()) {
                if (pkm == null || pkm.isEgg()) continue;

                // Curamos 50% de la vida actual
                int hpActual = pkm.getHealth();
                int hpMax   = pkm.getStats().getHP();
                int restauraHp = (int) Math.floor(hpMax * porcHp);
                pkm.setHealth(Math.min(hpActual + restauraHp, hpMax));

                // Curamos 50% de PP de cada movimiento
                for (Attack atk : pkm.getMoveset()) {
                    int ppActual = atk.pp;
                    int ppMax    = atk.getMaxPP();
                    int restauraPp = (int) Math.floor(ppMax * porcPp);
                    atk.pp = Math.min(ppActual + restauraPp, ppMax);
                }

                // Notificar el cambio
                StoragePosition pos = party.getPosition(pkm);
                party.notifyListeners(pos, pkm);
            }
        }

        // 4) Sonido de curación
        PlayerSoundUtils.playSoundToPlayer(
                player, SoundEvents.GENERIC_BURN, SoundCategory.PLAYERS, 1.0f, 0.5f
        );

        // 5) Si llegó a 0 usos tras este clic, enviar ambos mensajes y eliminar
        if (usosRestantes <= 0) {
            player.sendMessage(new StringTextComponent(getMensajeSinUsosComun()), player.getUUID());
            player.sendMessage(new StringTextComponent(getMensajeUsoComun(porcHp, 0)), player.getUUID());
            PlayerSoundUtils.playSoundToPlayer(
                    player, SoundEvents.ITEM_BREAK, SoundCategory.PLAYERS, 0.7f, 1.0f
            );
            player.getServer().execute(() -> stack.shrink(1));
            return;
        }

        // 6) Si aún quedan usos, mensaje de uso normal
        String mensaje = getMensajeUsoComun(porcHp, usosRestantes);
        player.sendMessage(new StringTextComponent(mensaje), player.getUUID());
    }


    /**
     * Cetro Divino (Épico):
     * - 50% HP curado (jugador + party)
     * - Restaurar 50% de PP de cada movimiento
     * - 30 usos totales (gestionados en CetroDivinoEpico)
     */
    private static void usarCetroEpico(ServerPlayerEntity player, ItemStack stack) {
        int usosRestantes = CetroDivinoEpico.getUsosRestantes(stack);
        if (usosRestantes <= 0) {
            // Si ya estaba a 0, mostramos mensaje y eliminamos
            player.sendMessage(new StringTextComponent(getMensajeSinUsosEpico()), player.getUUID());
            PlayerSoundUtils.playSoundToPlayer(
                    player, SoundEvents.ITEM_BREAK, SoundCategory.PLAYERS, 0.7f, 1.0f
            );
            player.getServer().execute(() -> stack.shrink(1));
            return;
        }

        // 1) Decrementar uso:
        CetroDivinoEpico.decrementarUso(stack);

        // 2) Ahora que ya restamos el uso, recalculamos cuántos quedan:
        usosRestantes = CetroDivinoEpico.getUsosRestantes(stack);

        // 3) Aplicamos el efecto de curación
        double porcHp = CetroDivinoEpico.getHpRestorePorc(); // 0.50
        // 3.1) Curar 50% de HP al jugador
        float vidaAct = player.getHealth();
        float vidaMax = player.getMaxHealth();
        float cura = (float) (vidaMax * porcHp);
        player.setHealth(Math.min(vidaAct + cura, vidaMax));

        // 3.2) Curar a cada Pokémon de la party
        PlayerPartyStorage party = StorageProxy.getParty(player);
        if (party != null) {
            double porcPp = CetroDivinoEpico.getPpRestorePorc(); // 0.50
            for (Pokemon pkm : party.getTeam()) {
                if (pkm == null || pkm.isEgg()) continue;

                // Curación parcial de HP (50%)
                int hpActual = pkm.getHealth();
                int hpMax = pkm.getStats().getHP();
                int curacion = (int) Math.floor(hpMax * porcHp);
                pkm.setHealth(Math.min(hpActual + curacion, hpMax));

                // Restaurar 50% de PP de cada movimiento
                for (Attack atk : pkm.getMoveset()) {
                    int ppCur = atk.pp;
                    int ppMax = atk.getMaxPP();
                    int restorePp = (int) Math.floor(ppMax * porcPp);
                    atk.pp = Math.min(ppCur + restorePp, ppMax);
                }

                // Notificar el cambio
                StoragePosition pos = party.getPosition(pkm);
                party.notifyListeners(pos, pkm);
            }
        }

        // 4) Sonido de curación
        PlayerSoundUtils.playSoundToPlayer(
                player, SoundEvents.GENERIC_BURN, SoundCategory.PLAYERS, 1.0f, 0.5f
        );

        // 5) Si llegó a 0 usos tras este clic, enviar ambos mensajes y eliminar
        if (usosRestantes <= 0) {
            player.sendMessage(new StringTextComponent(getMensajeSinUsosEpico()), player.getUUID());
            player.sendMessage(new StringTextComponent(getMensajeUsoEpico(porcHp, 0)), player.getUUID());
            PlayerSoundUtils.playSoundToPlayer(
                    player, SoundEvents.ITEM_BREAK, SoundCategory.PLAYERS, 0.7f, 1.0f
            );
            player.getServer().execute(() -> stack.shrink(1));
            return;
        }

        // 6) Si aún quedan usos, mensaje de uso normal
        String mensaje = getMensajeUsoEpico(porcHp, usosRestantes);
        player.sendMessage(new StringTextComponent(mensaje), player.getUUID());
    }


    /**
     * Cetro Divino (Mítico):
     * - Si no está en cooldown: cura completa (jugador + party) con heal() y aplica cooldown
     * - Si está en cooldown: informa tiempo restante
     * - Siempre consume el ítem al usarlo
     */
    private static void usarCetroMitico(ServerPlayerEntity player, ItemStack stack) {
        long remainingMs = CetroDivinoMitico.getRemainingCooldownMs(player);
        if (remainingMs > 0) {
            long mins = (remainingMs + 59_999L) / 60_000L; // redondeo
            String msg = TextFormatting.RED
                    + "✦ Cetro Divino (Mítico) en cooldown.\n§c✧ Tiempo restante: §e"
                    + mins + " min";
            player.sendMessage(new StringTextComponent(msg), player.getUUID());
            PlayerSoundUtils.playSoundToPlayer(
                    player, SoundEvents.UI_BUTTON_CLICK, SoundCategory.PLAYERS, 0.7f, 1.0f
            );
            return;
        }

        // 1) Curar completamente al jugador
        player.setHealth(player.getMaxHealth());

        // 2) Curar completamente a cada Pokémon de la party
        PlayerPartyStorage party = StorageProxy.getParty(player);
        if (party != null) {
            for (Pokemon pkm : party.getTeam()) {
                if (pkm == null || pkm.isEgg()) continue;

                pkm.heal(); // cura HP + PP completos
                StoragePosition pos = party.getPosition(pkm);
                party.notifyListeners(pos, pkm);
            }
        }

        // 3) Sonido de curación épica
        PlayerSoundUtils.playSoundToPlayer(
                player, SoundEvents.TOTEM_USE, SoundCategory.PLAYERS, 1.0f, 1.0f
        );

        // 4) Mensaje decorativo
        String titulo = TextFormatting.GOLD
                + "✦ Cetro Divino (Mítico) Usado ✦";
        String linea1 = TextFormatting.GREEN
                + "❖ HP restaurado: §b100%";
        String linea2 = TextFormatting.GREEN
                + "❖ PP del equipo: §b100%";
        String linea3 = TextFormatting.AQUA
                + "❖ Nuevo cooldown: §b30 min";
        String mensaje = titulo + "\n" + linea1 + "\n" + linea2 + "\n" + linea3;
        player.sendMessage(new StringTextComponent(mensaje), player.getUUID());

        // 5) Aplicar cooldown (ahora + 30 min)
        CetroDivinoMitico.setCooldown(player);
    }
}
