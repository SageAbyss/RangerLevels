// File: rl/sage/rangerlevels/items/sacrificios/SacrificioHandler.java
package rl.sage.rangerlevels.items.sacrificios;

import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

import java.util.Objects;

import static com.pixelmonmod.pixelmon.api.storage.StorageProxy.getParty;

@Mod.EventBusSubscriber(modid = "rangerlevels")
public class SacrificioHandler {

    @SubscribeEvent
    public static void onEntityRightClick(PlayerInteractEvent.EntityInteractSpecific ev) {
        PlayerEntity player = ev.getPlayer();
        if (!(player instanceof ServerPlayerEntity)) return;

        ItemStack held = player.getItemInHand(ev.getHand());
        if (held.isEmpty()) return;

        String id = RangerItemDefinition.getIdFromStack(held);
        boolean isInfinite = CatalizadorAlmas.ID.equals(id);
        boolean isLimited  = CatalizadorAlmasLimitado.ID.equals(id);
        if (!isInfinite && !isLimited) {
            return;
        }

        Entity target = ev.getTarget();
        if (!(target instanceof PixelmonEntity)) return;
        PixelmonEntity pix = (PixelmonEntity) target;
        Pokemon pokemon = pix.getPokemon();

        boolean isLegendario = pokemon.isLegendary();
        boolean isUltra      = pokemon.getSpecies().isUltraBeast();
        boolean isBossOrMega = BossEssenceHelper.isBoss(pix);

        if (isLegendario || isUltra) {
            // Legendario o Ultraente: solo propio
            if (!player.getUUID().equals(pokemon.getOriginalTrainerUUID())) {
                player.sendMessage(new StringTextComponent("§cEste Pokémon no es tuyo."), player.getUUID());
                return;
            }
            sacrificarYDarEsencia(player, pix, held, isLegendario, isUltra);

        } else if (isBossOrMega) {
            // Jefe o Mega: delegamos
            BossEssenceHelper.tryGiveBossEssenceOnSacrifice((ServerPlayerEntity) player, pix, held);
            // Si era catalizador limitado, decrementamos después
            if (isLimited) {
                manejarDecrementoLimitado((ServerPlayerEntity) player, held);
            }
        } else {
            player.sendMessage(new StringTextComponent(
                    "§cSolo puedes sacrificar Legendarios, Ultraentes de tu equipo o Jefes/Megas de tier Épico en adelante."
            ), player.getUUID());
        }

        ev.setCanceled(true);
    }

    /** Maneja sacrificio y entrega de EsenciaLegendaria o EsenciaUltraente */
    private static void sacrificarYDarEsencia(PlayerEntity player, PixelmonEntity pix, ItemStack held,
                                              boolean isLegendario, boolean isUltra) {
        ServerPlayerEntity sp = (ServerPlayerEntity) player;
        Pokemon pokemon = pix.getPokemon();

        // Extraer personality
        String personality = extractPersonality(pix);
        if (personality == null || personality.isEmpty()) {
            personality = pix.getUUID().toString();
        }

        // Quitar de party
        PlayerPartyStorage storage = getParty(sp);
        int slot = storage.getSlot(pokemon);
        if (slot != -1) storage.set(slot, null);

        // Efecto gráfico
        if (player.level instanceof ServerWorld) {
            spawnParticles((ServerWorld) player.level, pix);
        }
        pix.remove();

        // Crear esencia adecuada
        ItemStack esencia = isLegendario
                ? EsenciaLegendaria.createForSpeciesAndId(pokemon.getSpecies().getLocalizedName(), personality)
                : EsenciaUltraente.createForSpeciesAndId(pokemon.getSpecies().getLocalizedName(), personality);

        if (esencia.isEmpty()) {
            player.sendMessage(new StringTextComponent("§cError al extraer la Esencia del Pokémon."), player.getUUID());
            return;
        }
        giveOrDrop(sp, esencia);

        player.sendMessage(new StringTextComponent(
                "§aHas sacrificado a " + pokemon.getLocalizedName() +
                        " y obtenido §b" + esencia.getHoverName().getString() + "§a."
        ), player.getUUID());
        PlayerSoundUtils.playSoundToPlayer(sp, SoundEvents.BEACON_DEACTIVATE, SoundCategory.MASTER, 2f, 1f);
        PlayerSoundUtils.playSoundToPlayer(sp, SoundEvents.WITHER_DEATH,     SoundCategory.MASTER, .5f, .8f);
        PlayerSoundUtils.playSoundToPlayer(sp, SoundEvents.END_PORTAL_SPAWN,  SoundCategory.MASTER, .8f, 1.2f);

        // Si era catalizador limitado, decrementamos usos
        String id = RangerItemDefinition.getIdFromStack(held);
        if (CatalizadorAlmasLimitado.ID.equals(id)) {
            manejarDecrementoLimitado((ServerPlayerEntity) player, held);
        }
    }

    /** Lógica de decremento para CatalizadorAlmasLimitado */
    static void manejarDecrementoLimitado(ServerPlayerEntity player, ItemStack held) {
        int usosRestantes = CatalizadorAlmasLimitado.getUsosRestantes(held);
        // Decrementamos un uso
        CatalizadorAlmasLimitado.decrementarUso(held);
        int nuevosUsos = usosRestantes - 1;
        if (nuevosUsos > 0) {
            player.sendMessage(new StringTextComponent(
                    "§6Catalizador de Almas (§cLimitado§6): §6quedan §e" + nuevosUsos + " §6usos."
            ), player.getUUID());
        } else {
            player.sendMessage(new StringTextComponent("§cSe ha agotado el Catalizador de Almas (Limitado)."), player.getUUID());
            PlayerSoundUtils.playSoundToPlayer(player, SoundEvents.ITEM_BREAK, SoundCategory.PLAYERS, 1f, 1f);
            // Eliminar el item en servidor
            Objects.requireNonNull(player.getServer()).execute(() -> held.shrink(1));
        }
    }

    static void spawnParticles(ServerWorld world, PixelmonEntity pix) {
        double x = pix.getX(), y = pix.getY() + pix.getBbHeight()/2, z = pix.getZ();
        world.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 1, 0,0,0, 0.0);
        world.sendParticles(ParticleTypes.LARGE_SMOKE,      x, y, z, 30,1.0,1.0,1.0,0.02);
        world.sendParticles(ParticleTypes.SMOKE,            x, y, z, 50,1.5,1.5,1.5,0.01);
        world.sendParticles(ParticleTypes.FLAME,            x, y, z, 20,0.8,0.8,0.8,0.02);
        world.sendParticles(ParticleTypes.DAMAGE_INDICATOR, x, y, z, 25,1.0,1.0,1.0,0.0);
        world.sendParticles(ParticleTypes.PORTAL,           x, y, z, 40,1.0,1.0,1.0,0.05);
        world.sendParticles(ParticleTypes.EXPLOSION,        x, y, z, 1, 0,0,0,   0.0);
    }

    /** Extrae Personality/EncryptionConstant de PixelmonEntity */
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

    /** Utility: dar al inventario o dropear si está lleno */
    private static void giveOrDrop(ServerPlayerEntity player, ItemStack stack) {
        if (!player.inventory.add(stack)) {
            player.drop(stack, false);
        }
    }
}
