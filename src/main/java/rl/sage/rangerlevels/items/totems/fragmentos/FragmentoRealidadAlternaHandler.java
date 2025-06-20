package rl.sage.rangerlevels.items.totems.fragmentos;

import com.pixelmonmod.pixelmon.api.pokemon.Element;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.items.RangerItemDefinition;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = "rangerlevels")
public class FragmentoRealidadAlternaHandler {

    /**
     * Tipos que activan el bonus de EXP
     */
    private static final Set<Element> TIPOS_AFECTADOS = EnumSet.of(
            Element.ICE, Element.WATER, Element.FLYING,
            Element.POISON, Element.FAIRY, Element.STEEL
    );

    public static boolean hasFragmento(ServerPlayerEntity player) {
        for (ItemStack stack : player.inventory.items) {
            if (!stack.isEmpty() &&
                    FragmentoRealidadAlterna.ID.equals(RangerItemDefinition.getIdFromStack(stack))) {
                return true;
            }
        }
        ItemStack off = player.inventory.offhand.get(0);
        return !off.isEmpty() &&
                FragmentoRealidadAlterna.ID.equals(RangerItemDefinition.getIdFromStack(off));
    }

    /**
     * Aplica Saturación I y Lentitud I mientras el jugador tenga el fragmento
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END || !(ev.player instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) ev.player;
        boolean bloqueado = TotemRaizPrimordialHandler.hasTotem(player);
        if (hasFragmento(player) && !bloqueado) {
            // Duración 220 ticks (~11s) se re-aplica continuamente
            player.addEffect(new EffectInstance(Effects.SATURATION, 220, 0, false, false));
            player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 220, 0, false, false));
        } else {
            player.removeEffect(Effects.SATURATION);
            player.removeEffect(Effects.MOVEMENT_SLOWDOWN);
        }
    }

    /**
     * Aplica +15% de EXP al derrotar o capturar un Pixelmon de tipos afectados.
     * Usar en tu PixelmonEventHandler:
     * base = FragmentoRealidadAlternaHandler.applyBonusIfApplicable(player, defeated, base);
     */
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
