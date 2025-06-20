// File: rl/sage/rangerlevels/items/sacrificios/EspecieEssenceHelper.java
package rl.sage.rangerlevels.items.sacrificios;

import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

public class EspecieEssenceHelper {
    /**
     * Ahora siempre usa createForSpeciesAndId(...).
     */
    public static void giveEssence(ServerPlayerEntity player, PixelmonEntity pix) {
        // Obtener un identificador de especie: mejor usar registryName en vez de localizedName
        String speciesId = pix.getPokemon().getSpecies().getLocalizedName();
        // Extraer personality/EncryptionConstant con la lógica de SacrificioHandler:
        String personality = null;
        try {
            CompoundNBT full = pix.serializeNBT();
            CompoundNBT data;
            if (full.contains("PokemonData")) data = full.getCompound("PokemonData");
            else if (full.contains("pixelmon")) data = full.getCompound("pixelmon");
            else data = full;
            if (data.contains("EncryptionConstant")) {
                personality = Long.toString(data.getLong("EncryptionConstant"));
            } else if (data.contains("Personality")) {
                personality = Long.toString(data.getLong("Personality"));
            }
        } catch (Exception e) {
            RangerLevels.LOGGER.warn("Error extrayendo Personality: {}", e.getMessage());
        }
        if (personality == null || personality.isEmpty()) {
            // fallback al UUID de la entidad
            personality = pix.getUUID().toString();
        }

        ItemStack esencia = pix.getPokemon().isLegendary()
                ? EsenciaLegendaria.createForSpeciesAndId(speciesId, personality)
                : EsenciaUltraente.createForSpeciesAndId(speciesId, personality);

        if (esencia.isEmpty()) {
            player.sendMessage(new StringTextComponent("§cError al generar la Esencia."), player.getUUID());
            return;
        }
        if (!player.inventory.add(esencia)) player.drop(esencia, false);

        player.sendMessage(new StringTextComponent(
                "§7Has obtenido §b" + esencia.getHoverName().getString() + "§7."
        ), player.getUUID());
        PlayerSoundUtils.playSoundToPlayer(player, SoundEvents.BEACON_DEACTIVATE, SoundCategory.MASTER, 2.5f, 1f);
    }
}
