// File: rl/sage/rangerlevels/items/sacrificios/ModificadorNaturalezaHandler.java
package rl.sage.rangerlevels.items.modificadores;

import com.pixelmonmod.pixelmon.api.pokemon.Nature;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.gui.modificadores.NaturalezaMenu;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

import java.util.Arrays;

@Mod.EventBusSubscriber(modid = "rangerlevels")
public class ModificadorNaturalezaHandler {

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem ev) {
        if (!(ev.getPlayer() instanceof ServerPlayerEntity)) return;
        ItemStack held = ev.getPlayer().getItemInHand(ev.getHand());
        if (held.isEmpty() ||
                !ModificadorNaturaleza.ID.equals(RangerItemDefinition.getIdFromStack(held)))
            return;

        // Si no tienes naturaleza seleccionada, abrimos el menú
        if (ModificadorNaturaleza.getNature(held) == null) {
            ev.setCanceled(true);
            NaturalezaMenu.open((ServerPlayerEntity) ev.getPlayer());
        }
    }

    @SubscribeEvent
    public static void onEntityRightClick(PlayerInteractEvent.EntityInteractSpecific ev) {
        PlayerEntity player = ev.getPlayer();
        if (!(player instanceof ServerPlayerEntity)) return;

        ItemStack held = player.getItemInHand(ev.getHand());
        if (held.isEmpty() ||
                !ModificadorNaturaleza.ID.equals(RangerItemDefinition.getIdFromStack(held)))
            return;

        String selectedNature = ModificadorNaturaleza.getNature(held);
        if (selectedNature == null) return;

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
            player.sendMessage(new StringTextComponent("§cSólo puedes modificar Legendarios o Ultraentes."), player.getUUID());
            return;
        }
        // 3) Especie
        String species         = pix.getPokemon().getSpecies().getLocalizedName();
        String modifierSpecies = ModificadorNaturaleza.getSpecies(held);
        if (modifierSpecies == null || !species.equals(modifierSpecies)) {
            player.sendMessage(new StringTextComponent(
                    "§cEsta herramienta es para §e" + modifierSpecies +
                            "§c, no para §e" + species + "§c."
            ), player.getUUID());
            return;
        }

        // --- OMITIDO: comprobación de instancia ---

        // 4) Obtener el enum Nature y aplicarlo
        Nature nat = Arrays.stream(Nature.values())
                .filter(n -> n.getLocalizedName().equals(selectedNature))
                .findFirst().orElse(null);
        if (nat == null) {
            player.sendMessage(new StringTextComponent("§cNaturaleza inválida en el ítem."), player.getUUID());
            return;
        }

        pix.getPokemon().setNature(nat);
        pix.setPokemon(pix.getPokemon());
        held.shrink(1);
        player.sendMessage(new StringTextComponent(
                "§aHas aplicado la Naturaleza §e" + nat.getLocalizedName() +
                        "§a a tu " + species + "."
        ), player.getUUID());
        PlayerSoundUtils.playSoundToPlayer((ServerPlayerEntity) player, SoundEvents.BEACON_DEACTIVATE, SoundCategory.MASTER, 2.0f, 1.0f);
        ev.setCanceled(true);
    }
}
