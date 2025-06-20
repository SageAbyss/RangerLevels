package rl.sage.rangerlevels.items.totems.fragmentos;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.stats.IVStore;
import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import com.pixelmonmod.pixelmon.api.pokemon.Element;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import com.pixelmonmod.pixelmon.enums.EnumGrowth;
import com.pixelmonmod.pixelmon.init.registry.SoundRegistration;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Mod.EventBusSubscriber(modid = "rangerlevels")
public class TotemLamentoDiosesHandler {

    private static final Set<Element> TIPOS_AFECTADOS = EnumSet.of(
            Element.FIRE, Element.DRAGON, Element.FIGHTING,
            Element.PSYCHIC, Element.GHOST, Element.DARK
    );
    private static final Random RNG = new Random();

    private static final List<String> ACTIVATION_TEMPLATES = Arrays.asList(
            // 1
            "§5☁ Un lamento olvidado rasgó los cielos al activar el §8§ki§5Tótem del Lamento de los Dioses§8§ki§5 ☁\n" +
                    "§5→ Una sombra ancestral cayó sobre §c%s§5,\n" +
                    "§7→ Alterando su esencia con un poder antiguo y oscuro.",

            // 2
            "§8⚔ El eco de antiguas batallas resonó al invocar el §8§ki§4Tótem del Lamento de los Dioses§8§ki§8 ⚔\n" +
                    "§8→ §c%s§8 fue marcado por fuerzas que trascienden el tiempo,\n" +
                    "§7→ Su alma brilla ahora con un destino irreversible.",

            // 3
            "§4✔ Las arenas del olvido se alzaron con furia al usar el §8§ki§cTótem del Lamento de los Dioses§8§ki§4 ✔\n" +
                    "§4→ §c%s§4 fue tocado por una maldición sagrada,\n" +
                    "§7→ Cambiando para siempre su forma y legado.",

            // 4
            "§d⟁ Un portal entre mundos se abrió brevemente con el §8§ki§dTótem del Lamento de los Dioses§8§ki§d ⟁\n" +
                    "§d→ §c%s§d emergió alterado por energías prohibidas,\n" +
                    "§7→ Su poder se fundió con algo desconocido."
    );


    public static boolean hasTotem(ServerPlayerEntity player) {
        for (ItemStack stack : player.inventory.items) {
            if (!stack.isEmpty() &&
                    TotemLamentoDioses.ID.equals(RangerItemDefinition.getIdFromStack(stack))) {
                return true;
            }
        }
        ItemStack off = player.inventory.offhand.get(0);
        return !off.isEmpty() &&
                TotemLamentoDioses.ID.equals(RangerItemDefinition.getIdFromStack(off));
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent ev) {
        LivingEntity ent = ev.getEntityLiving();
        if (!(ent instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) ent;
        if (hasTotem(player) && (ev.getSource().isFire() || ev.getSource() == DamageSource.LAVA)) {
            ev.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END || !(ev.player instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) ev.player;
        if (hasTotem(player)) {
            player.addEffect(new EffectInstance(Effects.DAMAGE_BOOST, 220, 1, false, false));
        } else {
            player.removeEffect(Effects.DAMAGE_BOOST);
        }
    }

    /**
     * Aplica bonus de 30% de experiencia al derrotar un Pokémon, y replica la lógica
     * de Raíz Primordial en capturas con +30% exp y IVs/Shiny/etc. usando los TIPOS_AFECTADOS de este tótem.
     * No mueve ninguna de tus lógicas existentes: es un método extra para invocar desde tu event handler de batalla.
     */
    public static int applyExpBonusIfApplicable(ServerPlayerEntity player,
                                                PixelmonEntity defeated,
                                                int baseExp,
                                                boolean isCapture) {
        if (!hasTotem(player) || defeated == null) return baseExp;
        Pokemon p = defeated.getPokemon();
        if (p == null) return baseExp;

        // Excluir legendarios, ultrabeasts y Ditto en capturas
        boolean esLegendario = p.isLegendary();
        boolean esUltra = p.getSpecies().isUltraBeast();
        boolean esDitto = p.getSpecies().getDefaultForm().getLocalizedName().equalsIgnoreCase("ditto");
        if (isCapture && (esLegendario || esUltra || esDitto)) {
            return baseExp;
        }

        // Verificar tipo
        boolean tipoOk = p.getForm().getTypes().stream().anyMatch(TIPOS_AFECTADOS::contains);
        if (tipoOk) {
            // Aplica 30% extra de exp
            baseExp = (int) Math.round(baseExp * 1.30);
        }

        // En caso de captura y tipo válido, replica lógica de Raíz Primordial para IVs, tamaño, mensaje y sonido
        if (isCapture && tipoOk) {
            // Probabilidad alta de aplicar IVs y mensaje
            if (RNG.nextDouble() < 0.30) {
                IVStore ivs = p.getIVs();
                if (ivs != null) {
                    for (BattleStatsType stat : BattleStatsType.values()) {
                        int old = ivs.getStat(stat);
                        int inc = (int) Math.ceil(old * 0.12);
                        ivs.setStat(stat, Math.min(old + inc, 31));
                    }
                    ivs.markDirty();
                    p.setGrowth(RNG.nextBoolean() ? EnumGrowth.Microscopic : EnumGrowth.Ginormous);

                    // Mensaje aleatorio usando tus plantillas de Lamento de los Dioses
                    String nombre = p.getLocalizedName();
                    String template = ACTIVATION_TEMPLATES.get(RNG.nextInt(ACTIVATION_TEMPLATES.size()));
                    IFormattableTextComponent msg = new StringTextComponent(String.format(template, nombre));
                    player.sendMessage(msg, player.getUUID());

                    // Sonido
                    PlayerSoundUtils.playPixelmonSoundToPlayer(
                            player,
                            SoundRegistration.MYSTERY_BOX_OPEN,
                            SoundCategory.MASTER,
                            1.0f, 1.0f
                    );
                }
            }
            // Posible shiny con tu probabilidad (mismo 5% del método IV) ENREALIDAD ES 1
            if (RNG.nextDouble() < 0.01) {
                p.setShiny(true);
                player.sendMessage(
                        new StringTextComponent("§5→ ¡Dioses desconocidos convirtieron a " + p.getLocalizedName() + " en §e✧Shiny✧"),
                        player.getUUID()
                );
            }
        }

        return baseExp;
    }
}
