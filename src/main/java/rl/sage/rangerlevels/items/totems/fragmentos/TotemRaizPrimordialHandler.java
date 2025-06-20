package rl.sage.rangerlevels.items.totems.fragmentos;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import com.pixelmonmod.pixelmon.api.pokemon.Element;
import com.pixelmonmod.pixelmon.api.pokemon.stats.IVStore;
import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import com.pixelmonmod.pixelmon.enums.EnumGrowth;
import com.pixelmonmod.pixelmon.init.registry.SoundRegistration;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
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
public class TotemRaizPrimordialHandler {

    private static final Set<Element> TIPOS_AFECTADOS = EnumSet.of(
            Element.BUG, Element.GROUND, Element.ROCK,
            Element.NORMAL, Element.ELECTRIC, Element.GRASS
    );

    private static final Random RNG = new Random();

    // Plantillas de mensaje con placeholder %s para el nombre
    private static final List<String> ACTIVATION_TEMPLATES = Arrays.asList(
            // 1
            "§6⚡ ¡Un latido ancestral retumbó al activar el §c§ki§dTótem de Raíz Primordial§c§ki§6! ⚡\n" +
                    "§6→ Una energía salvaje envolvió a §c%s§6,\n" +
                    "§7→ Sus músculos se tensaron y sus dimensiones se distorsionaron",

            // 2
            "§2❀ La savia mística del bosque surgió con furia al despertar el §3§ki§1Tótem de Raíz Primordial§3§ki§2 ❀\n" +
                    "§2→ Una oleada de poder recorrió a §c%s§2,\n" +
                    "§7→ Fortificándolo más allá de lo imaginable y modificando su tamaño.",

            // 3
            "§5✷ Al resonar el eco del Árbol Primigenio, el §c§ki§dTótem de Raíz Primordial§c§ki§5 cobró vida ✷\n" +
                    "§5→ Sus raíces ancestrales elevaron a §c%s§5,\n" +
                    "§7→ Imbuyéndolo de un poder ancestral incontrastable.",

            // 4
            "§a☄ Un fulgor esmeralda emanó del §3§ki§2Tótem de Raíz Primordial§3§ki§a ☄ \n" +
                    "§a→ Los sentidos de §c%s§a se agudizaron,\n" +
                    "§7→ Su resistencia y fortuna alcanzaron cotas legendarias."

    );

    /** Cancela el daño por caída si el jugador lleva el tótem */
    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent ev) {
        LivingEntity entity = ev.getEntityLiving();
        if (!(entity instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) entity;

        if (hasTotem(player)) {
            ev.setCanceled(true);
            ev.setDistance(0F);
        }
    }
    public static boolean hasTotem(ServerPlayerEntity player) {
        for (ItemStack stack : player.inventory.items) {
            if (!stack.isEmpty()) {
                String id = RangerItemDefinition.getIdFromStack(stack);
                if (id != null && id.endsWith(TotemRaizPrimordial.ID)) {
                    return true;
                }
            }
        }
        ItemStack off = player.inventory.offhand.get(0);
        if (!off.isEmpty()) {
            String id = RangerItemDefinition.getIdFromStack(off);
            if (id != null && id.endsWith(TotemRaizPrimordial.ID)) {
                return true;
            }
        }
        return false;
    }

    public static int applyExpBonusIfApplicable(ServerPlayerEntity player,
                                                PixelmonEntity defeated,
                                                int baseExp,
                                                boolean isCapture) {
        if (!hasTotem(player) || defeated == null) return baseExp;
        Pokemon p = defeated.getPokemon();
        if (p == null) return baseExp;

        boolean esLegendario = p.isLegendary();
        boolean esUltra = p.getSpecies().isUltraBeast();
        boolean esDitto = p.getSpecies().getDefaultForm().getLocalizedName().equalsIgnoreCase("ditto");
        if (isCapture && (esLegendario || esUltra || esDitto)) {
            return baseExp;
        }
        boolean tipoOk = p.getForm().getTypes().stream().anyMatch(TIPOS_AFECTADOS::contains);
        if (tipoOk) {
            baseExp = (int) Math.round(baseExp * 1.30);
        }

        if (isCapture && tipoOk) {
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

                    // Mensaje aleatorio
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
            // Posible shiny con tu probabilidad
            if (RNG.nextDouble() < 0.01) {
                p.setShiny(true);
                player.sendMessage(
                        new StringTextComponent("§6→ ¡Tu " + p.getLocalizedName() + " surgió de la tierra como §e✧Shiny✧"),
                        player.getUUID()
                );
            }
        }

        return baseExp;
    }
}
