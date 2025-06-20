// File: rl/sage/rangerlevels/items/sacrificios/ModificadorShinyUniversalHandler.java
package rl.sage.rangerlevels.items.modificadores;

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
import rl.sage.rangerlevels.gui.modificadores.ShinyMenu;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

@Mod.EventBusSubscriber(modid = "rangerlevels")
public class ModificadorShinyUniversalHandler {

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem ev) {
        if (!(ev.getPlayer() instanceof ServerPlayerEntity)) return;
        ItemStack held = ev.getPlayer().getItemInHand(ev.getHand());
        if (held.isEmpty() ||
                !ModificadorShinyUniversal.ID.equals(RangerItemDefinition.getIdFromStack(held)))
            return;

        // Si no has elegido aún Shiny/No Shiny, abre el menú
        if (ModificadorShinyUniversal.getShiny(held) == null) {
            ev.setCanceled(true);
            ShinyMenu.open((ServerPlayerEntity) ev.getPlayer());
        }
    }

    @SubscribeEvent
    public static void onEntityRightClick(PlayerInteractEvent.EntityInteractSpecific ev) {
        PlayerEntity player = ev.getPlayer();
        if (!(player instanceof ServerPlayerEntity)) return;

        ItemStack held = player.getItemInHand(ev.getHand());
        if (held.isEmpty() ||
                !ModificadorShinyUniversal.ID.equals(RangerItemDefinition.getIdFromStack(held)))
            return;

        Boolean selected = ModificadorShinyUniversal.getShiny(held);
        if (selected == null) return;

        Entity target = ev.getTarget();
        if (!(target instanceof PixelmonEntity)) return;
        PixelmonEntity pix = (PixelmonEntity) target;

        // 1) Debe ser tu Pokémon
        if (!player.getUUID().equals(pix.getPokemon().getOriginalTrainerUUID())) {
            player.sendMessage(new StringTextComponent("§cEste Pokémon no es tuyo."), player.getUUID());
            return;
        }
        // 2) Sólo legendarios o Ultraentes
        if (!(pix.getPokemon().isLegendary() || pix.getPokemon().getSpecies().isUltraBeast())) {
            player.sendMessage(new StringTextComponent("§cSólo Legendarios o Ultraentes."), player.getUUID());
            return;
        }

        // 3) Aplicar o quitar Shiny
        pix.getPokemon().setShiny(selected);
        pix.setPokemon(pix.getPokemon());
        held.shrink(1);

        player.sendMessage(new StringTextComponent(
                "§aHas aplicado Shiny: §e" + (selected ? "Sí" : "No") + "§a a tu Pokémon."
        ), player.getUUID());
        PlayerSoundUtils.playSoundToPlayer(
                (ServerPlayerEntity) player,
                SoundEvents.BEACON_DEACTIVATE,
                SoundCategory.MASTER,
                2.0f,
                1.0f
        );
        ev.setCanceled(true);
    }
}
