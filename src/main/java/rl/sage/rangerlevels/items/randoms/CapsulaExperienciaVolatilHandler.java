package rl.sage.rangerlevels.items.randoms;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.Explosion;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.config.ItemsConfig;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.capability.LevelProvider;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

import java.util.Random;

/**
 * Handler para el uso de Cápsula de Experiencia Volátil.
 */
@Mod.EventBusSubscriber(modid = RangerLevels.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapsulaExperienciaVolatilHandler {
    private static final Random RNG = new Random();

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem ev) {
        if (!(ev.getPlayer() instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) ev.getPlayer();
        ItemStack stack = ev.getItemStack();
        String id = RangerItemDefinition.getIdFromStack(stack);
        if (!CapsulaExperienciaVolatil.ID.equals(id) || stack.isEmpty()) return;

        // Consumo de la cápsula
        stack.shrink(1);

        // Probabilidad de fallo
        double failChance = ItemsConfig.get().volatilCapsule.failChance;
        if (RNG.nextDouble() < failChance) {
            // Explosión de poder 10 en la posición del jugador
            player.level.explode(
                    player,                             // fuente de la explosión (puede ser null)
                    player.getX(), player.getY(), player.getZ(),
                    10.0F,                               // poder de la explosión
                    Explosion.Mode.DESTROY              // destruye bloques
            );
            // Dejar al jugador con medio corazón (1 punto de vida)
            player.setHealth(1.0F);

            player.displayClientMessage(
                    new StringTextComponent("§c¡La cápsula explotó y te dejó al borde de la muerte!"),
                    true
            );
            PlayerSoundUtils.playSoundToPlayer(
                    player,
                    SoundEvents.BEACON_DEACTIVATE,
                    SoundCategory.PLAYERS,
                    1.0f, 1.0f
            );
            PlayerSoundUtils.playSoundToPlayer(
                    player,
                    SoundEvents.WITHER_SPAWN,
                    SoundCategory.PLAYERS,
                    .5f, 1.0f
            );
        } else {
            // Rango configurable de EXP
            int min = ItemsConfig.get().volatilCapsule.expMin;
            int max = ItemsConfig.get().volatilCapsule.expMax;
            int amount = min + RNG.nextInt(max - min + 1);
            PlayerSoundUtils.playSoundToPlayer(player, SoundEvents.ELDER_GUARDIAN_CURSE, SoundCategory.PLAYERS, 1f, 1.5f);
            // Otorgar EXP
            LevelProvider.giveExpAndNotify(player, amount);
        }

        ev.setCanceled(true);
        ev.setCancellationResult(ActionResultType.SUCCESS);
    }
}
