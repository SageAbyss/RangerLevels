// File: rl/sage/rangerlevels/items/bonuses/SangreQuetzalHandler.java
package rl.sage.rangerlevels.items.reliquias;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import rl.sage.rangerlevels.config.ItemsConfig;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

@Mod.EventBusSubscriber(modid = "rangerlevels")
public class SangreQuetzalHandler {
    // Map<playerUUID, expirationTimeMillis>
    private static final Map<UUID, Long> ACTIVE = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onRightClick(RightClickItem ev) {
        if (!(ev.getPlayer() instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) ev.getPlayer();
        ItemStack stack = ev.getItemStack();
        String id = stack.hasTag() ? stack.getTag().getString(RangerItemDefinition.NBT_ID_KEY) : null;

        long now = System.currentTimeMillis();
        ItemsConfig.BloodConfig cfg = ItemsConfig.get().blood;
        double pct; int durMinutes;

        if (SangreQuetzalLegendario.ID.equals(id)) {
            pct = cfg.legendarioPercent;
            durMinutes = cfg.legendarioDurationMinutes;
        } else if (SangreQuetzalEstelar.ID.equals(id)) {
            pct = cfg.estelarPercent;
            durMinutes = cfg.estelarDurationMinutes;
        } else if (SangreQuetzalMitico.ID.equals(id)) {
            pct = cfg.miticoPercent;
            durMinutes = cfg.miticoDurationMinutes;
        } else {
            return;
        }

        ev.setCanceled(true);
        ev.setCancellationResult(ActionResultType.SUCCESS);

        // Activa el bonus
        ACTIVE.put(player.getUUID(), now + durMinutes * 60_000L);
        stack.shrink(1);

        player.sendMessage(
                new StringTextComponent(
                        String.format("§aBonus de EXP de §6+%.0f%% §aactivado por §6%d minutos.", pct, durMinutes)
                ),
                player.getUUID()
        );
        PlayerSoundUtils.playSoundToPlayer(
                player,
                SoundEvents.BEACON_DEACTIVATE,
                SoundCategory.MASTER,
                1.5f,
                1.0f
        );
        PlayerSoundUtils.playSoundToPlayer(
                player,
                SoundEvents.ENDER_DRAGON_GROWL,
                SoundCategory.MASTER,
                1.0f,
                0.8f
        );
    }

    /** Devuelve el bonus actual (e.g. 0.5 para +50%), o 0 si expiró/no activo. */
    public static double getBonus(ServerPlayerEntity player) {
        Long expTs = ACTIVE.get(player.getUUID());
        if (expTs == null || System.currentTimeMillis() > expTs) {
            ACTIVE.remove(player.getUUID());
            return 0.0;
        }
        ItemsConfig.BloodConfig cfg = ItemsConfig.get().blood;
        // Determina qué tier está activo por duración:
        long remaining = expTs - System.currentTimeMillis();
        int minsLeft = (int)Math.ceil(remaining / 60_000.0);
        if (minsLeft <= cfg.miticoDurationMinutes && cfg.miticoPercent > 0) {
            return cfg.miticoPercent / 100.0;
        } else if (minsLeft <= cfg.estelarDurationMinutes) {
            return cfg.estelarPercent / 100.0;
        } else {
            return cfg.legendarioPercent / 100.0;
        }
    }

    /** Aplica el bonus general sobre cualquier cantidad de EXP y redondea. */
    public static int applyBonus(ServerPlayerEntity player, int baseExp) {
        double bonus = getBonus(player);
        return (int)Math.round(baseExp * (1.0 + bonus));
    }
}
