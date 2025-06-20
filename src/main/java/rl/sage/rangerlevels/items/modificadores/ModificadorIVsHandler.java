// File: rl/sage/rangerlevels/items/sacrificios/ModificadorIVsHandler.java
package rl.sage.rangerlevels.items.modificadores;

import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import com.pixelmonmod.pixelmon.api.pokemon.stats.IVStore;
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
import rl.sage.rangerlevels.gui.modificadores.IVsMenu;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

@Mod.EventBusSubscriber(modid = "rangerlevels")
public class ModificadorIVsHandler {

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem ev) {
        if (!(ev.getPlayer() instanceof ServerPlayerEntity)) return;
        ItemStack held = ev.getPlayer().getItemInHand(ev.getHand());
        if (held.isEmpty() ||
                !ModificadorIVs.ID.equals(RangerItemDefinition.getIdFromStack(held)))
            return;

        // Si falta acción o stat, abrimos el menú
        if (ModificadorIVs.getStat(held) == null) {
            ev.setCanceled(true);
            IVsMenu.open((ServerPlayerEntity) ev.getPlayer());
        }
    }

    @SubscribeEvent
    public static void onEntityRightClick(PlayerInteractEvent.EntityInteractSpecific ev) {
        PlayerEntity player = ev.getPlayer();
        if (!(player instanceof ServerPlayerEntity)) return;

        ItemStack held = player.getItemInHand(ev.getHand());
        if (held.isEmpty() ||
                !ModificadorIVs.ID.equals(RangerItemDefinition.getIdFromStack(held)))
            return;

        String action = ModificadorIVs.getAction(held);
        String stat   = ModificadorIVs.getStat(held);
        if (action == null || stat == null) return;

        Entity target = ev.getTarget();
        if (!(target instanceof PixelmonEntity)) return;
        PixelmonEntity pix = (PixelmonEntity) target;

        // 1) Sólo tu Pokémon
        if (!player.getUUID().equals(pix.getPokemon().getOriginalTrainerUUID())) {
            player.sendMessage(new StringTextComponent("§cEste Pokémon no es tuyo."), player.getUUID());
            return;
        }
        // 2) Sólo legendarios o Ultraentes
        if (!(pix.getPokemon().isLegendary() || pix.getPokemon().getSpecies().isUltraBeast())) {
            player.sendMessage(new StringTextComponent("§cSólo legendarios o Ultraentes."), player.getUUID());
            return;
        }
        // 3) Especie coincide
        String species         = pix.getPokemon().getSpecies().getLocalizedName();
        String modifierSpecies = ModificadorIVs.getSpecies(held);
        if (modifierSpecies == null || !species.equals(modifierSpecies)) {
            player.sendMessage(new StringTextComponent(
                    "§cEsta herramienta es para §e" + modifierSpecies +
                            "§c, no para §e" + species + "§c."
            ), player.getUUID());
            return;
        }

        // 4) Aplicar ajuste de IV
        int value = action.equals("up") ? 31 : 0;

        // Mapeo explícito de nombres
        BattleStatsType statType;
        switch (stat) {
            case "HP":
                statType = BattleStatsType.HP;
                break;
            case "ATTACK":
                statType = BattleStatsType.ATTACK;
                break;
            case "DEFENSE":
                statType = BattleStatsType.DEFENSE;
                break;
            case "SP_ATTACK":
                statType = BattleStatsType.SPECIAL_ATTACK;
                break;
            case "SP_DEFENSE":
                statType = BattleStatsType.SPECIAL_DEFENSE;
                break;
            case "SPEED":
                statType = BattleStatsType.SPEED;
                break;
            default:
                player.sendMessage(new StringTextComponent("§cStat inválido: " + stat), player.getUUID());
                return;
        }

        // Modificamos el IVStore
        IVStore ivs = pix.getPokemon().getIVs();
        ivs.setStat(statType, value);
        ivs.markDirty();

        // Reaplicamos los cambios
        pix.setPokemon(pix.getPokemon());

        // 5) Consumir el modificador y feedback
        held.shrink(1);
        player.sendMessage(new StringTextComponent(
                "§aIV de §e" + stat + "§a establecido a §e" + value + "§a."
        ), player.getUUID());
        PlayerSoundUtils.playSoundToPlayer((ServerPlayerEntity) player,
                SoundEvents.BEACON_DEACTIVATE, SoundCategory.MASTER, 2f, 1f);
        ev.setCanceled(true);
    }
}
