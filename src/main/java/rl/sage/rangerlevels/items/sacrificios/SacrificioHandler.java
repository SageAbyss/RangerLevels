// File: rl/sage/rangerlevels/items/sacrificios/SacrificioHandler.java
package rl.sage.rangerlevels.items.sacrificios;

import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

import static com.pixelmonmod.pixelmon.api.storage.StorageProxy.getParty;

@Mod.EventBusSubscriber(modid = "rangerlevels")
public class SacrificioHandler {

    @SubscribeEvent
    public static void onEntityRightClick(PlayerInteractEvent.EntityInteractSpecific ev) {
        PlayerEntity player = ev.getPlayer();
        if (!(player instanceof ServerPlayerEntity)) return;

        ItemStack held = player.getItemInHand(ev.getHand());
        if (held.isEmpty() ||
                !CatalizadorAlmas.ID.equals(RangerItemDefinition.getIdFromStack(held)))
            return;

        Entity target = ev.getTarget();
        if (!(target instanceof PixelmonEntity)) return;
        PixelmonEntity pix = (PixelmonEntity) target;

        // 1) Propietario
        if (!player.getUUID().equals(pix.getPokemon().getOriginalTrainerUUID())) {
            player.sendMessage(new StringTextComponent("§cEste Pokémon no es tuyo."), player.getUUID());
            return;
        }
        // 2) Sólo legendarios o ultraentes
        if (!(pix.getPokemon().isLegendary() || pix.getPokemon().getSpecies().isUltraBeast())) {
            player.sendMessage(new StringTextComponent("§cSolo puedes sacrificar un Legendario o Ultraente."), player.getUUID());
            return;
        }

        // 3) Extraer el ID ÚNICO: primero intentamos NBT, si falla usamos el UUID de la entidad
        String personality = extractPersonality(pix);
        if (personality == null) {
            personality = pix.getUUID().toString();
        }

        // 4) Remover de la party y del mundo
        ServerPlayerEntity sp = (ServerPlayerEntity) player;
        PlayerPartyStorage storage = getParty(sp);
        int slot = storage.getSlot(pix.getPokemon());
        if (slot != -1) storage.set(slot, null);
        pix.remove();

        // 5) Crear la esencia ligada a esa personalidad
        ItemStack esencia = pix.getPokemon().isLegendary()
                ? EsenciaLegendaria.createForSpeciesAndId(pix.getPokemon().getSpecies().getLocalizedName(), personality)
                : EsenciaUltraente.createForSpeciesAndId(pix.getPokemon().getSpecies().getLocalizedName(), personality);

        if (esencia.isEmpty()) {
            player.sendMessage(new StringTextComponent("§cError al extraer la Esencia del Pokémon."), player.getUUID());
            return;
        }
        if (!player.inventory.add(esencia)) player.drop(esencia, false);

        // 6) Consumimos el catalizador y avisamos
        held.shrink(1);
        player.sendMessage(new StringTextComponent(
                "§aHas sacrificado a " + pix.getPokemon().getLocalizedName() +
                        " y obtenido §b" + esencia.getHoverName().getString() + "§a."
        ), player.getUUID());
        PlayerSoundUtils.playSoundToPlayer(sp, SoundEvents.BEACON_DEACTIVATE, SoundCategory.MASTER, 2f, 1f);
        PlayerSoundUtils.playSoundToPlayer(sp, SoundEvents.WITHER_DEATH,    SoundCategory.MASTER, .5f, .8f);

        ev.setCanceled(true);
    }

    /**
     * Intenta leer el Personality/EncryptionConstant de la entidad Pixelmon;
     * si no está (p.ej. claves distintas en tu build), devuelve null.
     */
    private static String extractPersonality(PixelmonEntity pix) {
        try {
            CompoundNBT fullNBT = pix.serializeNBT();
            CompoundNBT data;
            if (fullNBT.contains("PokemonData")) {
                data = fullNBT.getCompound("PokemonData");
            } else if (fullNBT.contains("pixelmon")) {
                data = fullNBT.getCompound("pixelmon");
            } else {
                data = fullNBT;
            }
            if (data.contains("EncryptionConstant")) {
                return Long.toString(data.getLong("EncryptionConstant"));
            }
            if (data.contains("Personality")) {
                return Long.toString(data.getLong("Personality"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
