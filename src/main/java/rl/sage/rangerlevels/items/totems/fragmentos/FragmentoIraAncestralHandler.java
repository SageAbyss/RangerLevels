package rl.sage.rangerlevels.items.totems.fragmentos;

import com.pixelmonmod.pixelmon.api.pokemon.Element;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.items.RangerItemDefinition;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = "rangerlevels")
public class FragmentoIraAncestralHandler {

    /** Tipos que activan el bonus de EXP */
    private static final Set<Element> TIPOS_AFECTADOS = EnumSet.of(
            Element.FIRE, Element.DRAGON, Element.FIGHTING,
            Element.PSYCHIC, Element.GHOST, Element.DARK
    );

    public static boolean hasFragmento(ServerPlayerEntity player) {
        for (ItemStack stack : player.inventory.items) {
            if (!stack.isEmpty() &&
                    FragmentoIraAncestral.ID.equals(RangerItemDefinition.getIdFromStack(stack))) {
                return true;
            }
        }
        ItemStack off = player.inventory.offhand.get(0);
        return !off.isEmpty() &&
                FragmentoIraAncestral.ID.equals(RangerItemDefinition.getIdFromStack(off));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END || !(ev.player instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) ev.player;
        boolean bloqueado = TotemRaizPrimordialHandler.hasTotem(player);

        if (hasFragmento(player) && !bloqueado) {
            player.addEffect(new EffectInstance(Effects.DAMAGE_BOOST,   220, 0, false, false));
            player.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 220, 0, false, false));
        } else {
            player.removeEffect(Effects.DAMAGE_BOOST);
            player.removeEffect(Effects.MOVEMENT_SPEED);
        }
    }

    public static int applyBonusIfApplicable(ServerPlayerEntity player,
                                             PixelmonEntity defeated,
                                             int baseExp) {
        if (!hasFragmento(player) || defeated == null) {
            return baseExp;
        }
        List<Element> tipos = defeated.getPokemon().getForm().getTypes();
        if (tipos != null) {
            for (Element tipo : tipos) {
                if (TIPOS_AFECTADOS.contains(tipo)) {
                    return (int) Math.round(baseExp * 1.15);
                }
            }
        }
        return baseExp;
    }
}
