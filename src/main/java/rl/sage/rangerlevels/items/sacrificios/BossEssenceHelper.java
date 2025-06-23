package rl.sage.rangerlevels.items.sacrificios;

import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import com.pixelmonmod.pixelmon.api.pokemon.boss.BossTier;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.server.ServerWorld;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

import java.util.Random;

import static rl.sage.rangerlevels.items.sacrificios.SacrificioHandler.spawnParticles;

public class BossEssenceHelper {
    private static final Random RNG = new Random();

    /**
     * Comprueba si el PixelmonEntity es boss de tier épico en adelante, o si es Mega.
     */
    public static boolean isBoss(PixelmonEntity pix) {
        if (!pix.isBossPokemon()) {
            return false;
        }
        BossTier tier = pix.getBossTier();
        if (tier == null) {
            return false;
        }
        switch (tier.getID()) {
            case "rare":
            case "epic":
            case "legendary":
            case "ultimate":
            case "haunted":
            case "drowned":
                return true;
            default:
                return false;
        }
    }

    /**
     * Intenta dar la EsenciaBoss con 10% de probabilidad si el Pixelmon derrotado es boss o Mega válido.
     * Llamar este método desde tu event handler cuando detectes la derrota de un PixelmonEntity.
     */
    public static void tryGiveBossEssenceOnSacrifice(ServerPlayerEntity player, PixelmonEntity pix, ItemStack catalizador) {
        if (!isBoss(pix)) return;

        // Extraer datos
        String speciesName = pix.getPokemon().getSpecies().getLocalizedName();
        String personality = extractPersonality(pix);
        if (personality.isEmpty()) personality = pix.getUUID().toString();

        // Partículas y desaparición
        spawnParticles((ServerWorld) player.level, pix);
        pix.remove();

        // Crear y dar esencia
        ItemStack esencia = EsenciaBoss.createForSpeciesAndId(speciesName, personality);
        if (esencia.isEmpty()) {
            player.sendMessage(new StringTextComponent("§cError al generar EsenciaBoss."), player.getUUID());
            // Si es versión limitada, decrementar en lugar de shrink directo:
            if (CatalizadorAlmasLimitado.ID.equals(RangerItemDefinition.getIdFromStack(catalizador))) {
                SacrificioHandler.manejarDecrementoLimitado(player, catalizador);
            } else {
                catalizador.shrink(1);
            }
            return;
        }
        giveOrDrop(player, esencia);

        player.sendMessage(new StringTextComponent(
                "§aHas sacrificado a " + speciesName + " y obtenido §d"
                        + esencia.getHoverName().getString() + "§a."
        ), player.getUUID());
        PlayerSoundUtils.playSoundToPlayer(player, SoundEvents.TRIDENT_HIT, SoundCategory.PLAYERS, 1f, 1f);

        // Después del sacrificio exitoso, si es catalizador limitado, decrementar:
        if (CatalizadorAlmasLimitado.ID.equals(RangerItemDefinition.getIdFromStack(catalizador))) {
            SacrificioHandler.manejarDecrementoLimitado(player, catalizador);
        }
    }


    public static void tryGiveBossEssenceOnDefeat(ServerPlayerEntity player, PixelmonEntity pix) {
        if (!isBoss(pix)) return;
        if (RNG.nextDouble() >= 0.25) return;

        String speciesName = pix.getPokemon().getSpecies().getLocalizedName();
        String personality = extractPersonality(pix);
        if (personality.isEmpty()) personality = pix.getUUID().toString();

        ItemStack esencia = EsenciaBoss.createForSpeciesAndId(speciesName, personality);
        if (esencia.isEmpty()) {
            player.sendMessage(new StringTextComponent("§cError al generar EsenciaBoss."), player.getUUID());
            return;
        }
        giveOrDrop(player, esencia);

        player.sendMessage(new StringTextComponent(
                "§7Has obtenido §d" + esencia.getHoverName().getString() + "§7 por derrotar un boss."
        ), player.getUUID());
        PlayerSoundUtils.playSoundToPlayer(player, SoundEvents.TRIDENT_HIT, SoundCategory.PLAYERS, 1f, 1f);
    }

    /**
     * Intenta añadir el stack al inventario; si está lleno, lo dropea en el mundo.
     */
    private static void giveOrDrop(ServerPlayerEntity player, ItemStack stack) {
        if (!player.inventory.add(stack)) {
            player.drop(stack, false);
        }
    }

    /**
     * Extrae el Personality/EncryptionConstant de PixelmonEntity; si no existe, devuelve null.
     */
    private static String extractPersonality(PixelmonEntity pix) {
        try {
            CompoundNBT data = pix.getPokemon().getPersistentData();
            if (data.contains("EncryptionConstant")) {
                return Long.toString(data.getLong("EncryptionConstant"));
            }
            if (data.contains("Personality")) {
                return Long.toString(data.getLong("Personality"));
            }
        } catch (Exception e) {
            RangerLevels.LOGGER.warn("No pude extraer personality del Pokemon: {}", e.getMessage());
        }
        return pix.getUUID().toString();
    }
}
