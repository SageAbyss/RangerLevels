package rl.sage.rangerlevels.items.modificadores;


import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import com.pixelmonmod.pixelmon.enums.EnumGrowth;
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
import rl.sage.rangerlevels.gui.modificadores.TamanoMenu;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

@Mod.EventBusSubscriber(modid = "rangerlevels")
public class ModificadorTamanoHandler {

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem ev) {
        if (!(ev.getPlayer() instanceof ServerPlayerEntity)) return;
        ItemStack held = ev.getPlayer().getItemInHand(ev.getHand());
        if (held.isEmpty() ||
                !ModificadorTamano.ID.equals(RangerItemDefinition.getIdFromStack(held)))
            return;

        // Si no tienes tamaño seleccionado, abrimos el menú
        if (ModificadorTamano.getSize(held) == null) {
            ev.setCanceled(true);
            TamanoMenu.open((ServerPlayerEntity) ev.getPlayer());
        }
    }

    @SubscribeEvent
    public static void onEntityRightClick(PlayerInteractEvent.EntityInteractSpecific ev) {
        PlayerEntity player = ev.getPlayer();
        if (!(player instanceof ServerPlayerEntity)) return;

        ItemStack held = player.getItemInHand(ev.getHand());
        if (held.isEmpty() ||
                !ModificadorTamano.ID.equals(RangerItemDefinition.getIdFromStack(held)))
            return;

        String selectedSize = ModificadorTamano.getSize(held);
        if (selectedSize == null) return;

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
        String modifierSpecies = ModificadorTamano.getSpecies(held);
        if (modifierSpecies == null || !species.equals(modifierSpecies)) {
            player.sendMessage(new StringTextComponent(
                    "§cEsta herramienta es para §e" + modifierSpecies +
                            "§c, no para §e" + species + "§c."
            ), player.getUUID());
            return;
        }

        // 4) Obtener el enum EnumGrowth y aplicarlo
        try {
            EnumGrowth growth = EnumGrowth.valueOf(selectedSize);
            pix.getPokemon().setGrowth(growth);
            pix.setPokemon(pix.getPokemon());
            held.shrink(1);
            player.sendMessage(new StringTextComponent(
                    "§aHas aplicado el Tamaño §e" + growth.getLocalizedName() +
                            "§a a tu " + species + "."
            ), player.getUUID());
            PlayerSoundUtils.playSoundToPlayer((ServerPlayerEntity) player, SoundEvents.BEACON_DEACTIVATE, SoundCategory.MASTER, 2.0f, 1.0f);
            ev.setCanceled(true);
        } catch (IllegalArgumentException e) {
            player.sendMessage(new StringTextComponent("§cTamaño inválido en el ítem."), player.getUUID());
        }
    }
}
