package rl.sage.rangerlevels.items.totems.fragmentos;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import com.pixelmonmod.pixelmon.api.pokemon.Element;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.items.RangerItemDefinition;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = "rangerlevels")
public class FragmentoCorazonGaiaHandler {

    /** Conjunto de tipos que el fragmento afecta */
    private static final Set<Element> TIPOS_AFECTADOS = EnumSet.of(
            Element.BUG, Element.GROUND, Element.ROCK,
            Element.NORMAL, Element.ELECTRIC, Element.GRASS
    );

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END || !(ev.player instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) ev.player;

        if (hasFragmento(player) && !TotemRaizPrimordialHandler.hasTotem(player)) {
            player.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE, 220, 0, false, false));
            player.addEffect(new EffectInstance(Effects.LUCK,               220, 0, false, false));
        } else {
            if (player.hasEffect(Effects.DAMAGE_RESISTANCE))
                player.removeEffect(Effects.DAMAGE_RESISTANCE);
            if (player.hasEffect(Effects.LUCK))
                player.removeEffect(Effects.LUCK);
        }
    }

    public static boolean hasFragmento(ServerPlayerEntity player) {
        for (ItemStack stack : player.inventory.items) {
            if (!stack.isEmpty() &&
                    FragmentoCorazonGaia.ID.equals(RangerItemDefinition.getIdFromStack(stack))) {
                return true;
            }
        }
        ItemStack off = player.inventory.offhand.get(0);
        return !off.isEmpty() &&
                FragmentoCorazonGaia.ID.equals(RangerItemDefinition.getIdFromStack(off));
    }

    public static int applyBonusIfApplicable(ServerPlayerEntity player,
                                             PixelmonEntity defeated,
                                             int baseExp) {
        if (!hasFragmento(player) || defeated == null) {
            return baseExp;
        }
        Pokemon p = defeated.getPokemon();
        if (p == null) {
            return baseExp;
        }
        // Este es el único método que necesitas para obtener los tipos:
        List<Element> tipos = p.getForm().getTypes();
        if (tipos != null) {
            for (Element tipo : tipos) {
                if (TIPOS_AFECTADOS.contains(tipo)) {
                    int nuevo = (int) Math.round(baseExp * 1.15);
                    player.sendMessage(
                            new StringTextComponent("§a+20% EXP por Fragmento del Corazón de Gaia contra tipo afectado!"),
                            player.getUUID()
                    );
                    return nuevo;
                }
            }
        }
        return baseExp;
    }
}
