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
public class TotemAbismoGlacialHandler {

    private static final Set<Element> TIPOS_AFECTADOS = EnumSet.of(
            Element.ICE, Element.WATER, Element.FLYING,
            Element.POISON, Element.FAIRY, Element.STEEL
    );
    private static final Random RNG = new Random();

    private static final List<String> ACTIVATION_TEMPLATES = Arrays.asList(
            // 1
            "§b❄ Un aliento helado surgió de las profundidades al activar el §8§ki§bTótem del Abismo Glacial§8§ki§b ❄\n" +
                    "§b→ Una escarcha ancestral envolvió a §c%s§b,\n" +
                    "§7→ Transformando su esencia con el frío eterno.",
            // 2
            "§3🌊 El abismo glacial se abrió al invocar el §8§ki§3Tótem del Abismo Glacial§8§ki§3 🌊\n" +
                    "§3→ §c%s§3 fue envuelto en aguas heladas prohibidas,\n" +
                    "§7→ Forjando su destino en hielo perpetuo.",
            // 3
            "§9❄ Ráfagas gélidas ascendieron al usar el §8§ki§9Tótem del Abismo Glacial§8§ki§9 ❄\n" +
                    "§9→ §c%s§9 sintió el peso del frío abisal,\n" +
                    "§7→ Sellando su poder en la herrumbre de lo ártico.",
            // 4
            "§c⛧ Un vórtice de hielo surgió al activar el §8§ki§cTótem del Abismo Glacial§8§ki§c ⛧\n" +
                    "§c→ §c%s§c emergió endurecido por la escarcha abisal,\n" +
                    "§7→ Redefiniendo su forma bajo la helada eternidad."
    );

    public static boolean hasTotem(ServerPlayerEntity player) {
        for (ItemStack stack : player.inventory.items) {
            if (!stack.isEmpty() &&
                    TotemAbismoGlacial.ID.equals(RangerItemDefinition.getIdFromStack(stack))) {
                return true;
            }
        }
        ItemStack off = player.inventory.offhand.get(0);
        return !off.isEmpty() &&
                TotemAbismoGlacial.ID.equals(RangerItemDefinition.getIdFromStack(off));
    }

    /** Inmunidad a ahogarse y a daño de caída */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent ev) {
        LivingEntity ent = ev.getEntityLiving();
        if (!(ent instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) ent;
        if (!hasTotem(player)) return;

        DamageSource src = ev.getSource();
        // Inmunidad a ahogarse
        if (src == DamageSource.DROWN) {
            ev.setCanceled(true);
            // Restaurar aire inmediatamente
            player.setAirSupply(player.getMaxAirSupply());
            return;
        }
        // Inmunidad a daño de caída
        if (src == DamageSource.FALL) {
            ev.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END || !(ev.player instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) ev.player;
        if (hasTotem(player)) {
            // Restaurar aire para la inmunidad continua
            player.setAirSupply(player.getMaxAirSupply());
        }
    }

    /**
     * Aplica +30% de exp si el Pokémon derrotado/capturado tiene tipo afectado.
     * Además, en capturas, replica lógica de IVs (+12%) y tamaño y mensajes sfx con probabilidad.
     * 0.1% de probabilidad de Shiny.
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
            // +30% exp
            baseExp = (int) Math.round(baseExp * 1.30);
        }

        // En capturas de tipo válido, aplicar IVs, tamaño, mensaje y sonido
        if (isCapture && tipoOk) {
            // Probabilidad de ivs
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

                    // Mensaje aleatorio adaptado al Abismo Glacial
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
            // 0.1% de probabilidad de Shiny
            if (RNG.nextDouble() < 0.01) {
                p.setShiny(true);
                player.sendMessage(
                        new StringTextComponent("§b→ ¡Tu " + p.getLocalizedName() + " emergió como §e✧Shiny✧ del abismo!"),
                        player.getUUID()
                );
            }
        }

        return baseExp;
    }
}
