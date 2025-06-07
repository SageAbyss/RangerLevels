package rl.sage.rangerlevels.items.cetro;

import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.Tier;
import rl.sage.rangerlevels.util.NBTUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Cetro Divino - Tier Mítico:
 *  - Cura completamente HP y PP del jugador + equipo Pokémon
 *  - Uso ilimitado, pero con cooldown de 30 minutos (se guarda en NBT persistente del jugador)
 */
public class CetroDivinoMitico extends RangerItemDefinition {
    public static final String ID = "cetro_divino_mitico";
    private static final String NBT_COOLDOWN_KEY = "CetroDivinoMiticoCooldown";
    private static final long COOLDOWN_MS = 30 * 60_000L; // 30 minutos

    public CetroDivinoMitico() {
        super(
                ID,
                PixelmonItems.fairy_wand,
                Tier.MITICO,
                null,
                "✦ Cetro Divino Mítico ✦",
                null
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(amount);
        stack.setHoverName(Tier.MITICO.applyGradient(getDisplayName()));

        // Generar lore estático (no hay usos, pero se menciona el cooldown)
        List<IFormattableTextComponent> lore = Arrays.asList(
                new StringTextComponent("§7✧ Cura completamente tu HP y a tu equipo Pokémon"),
                new StringTextComponent("§7✧ Restaura todos los PP de cada movimiento"),
                new StringTextComponent("§7✧ Uso ilimitado, cooldown: §b30 min"),
                new StringTextComponent("§7▶ Tier: ").append(Tier.MITICO.getColor())
        );
        CompoundNBT tag = stack.getOrCreateTag();
        CompoundNBT displayTag = tag.contains("display") ? tag.getCompound("display") : new CompoundNBT();
        ListNBT loreList = new ListNBT();
        for (IFormattableTextComponent line : lore) {
            String json = IFormattableTextComponent.Serializer.toJson(line);
            loreList.add(StringNBT.valueOf(json));
        }
        displayTag.put("Lore", loreList);
        tag.put("display", displayTag);

        NBTUtils.applyAllHideFlags(tag);

        stack.setTag(tag);
        return stack;
    }

    /** Devuelve el cooldown activo (timestamp en milis), o 0 si no está en cooldown. */
    public static long getCooldownTimestamp(ServerPlayerEntity player) {
        CompoundNBT persist = player.getPersistentData();
        if (!persist.contains(NBT_COOLDOWN_KEY)) return 0L;
        return persist.getLong(NBT_COOLDOWN_KEY);
    }

    /** Guarda el nuevo timestamp de cooldown en el jugador (ahora + 30min). */
    public static void setCooldown(ServerPlayerEntity player) {
        long ahora = System.currentTimeMillis();
        long nextAvailable = ahora + COOLDOWN_MS;
        CompoundNBT persist = player.getPersistentData();
        persist.putLong(NBT_COOLDOWN_KEY, nextAvailable);
    }

    /** Devuelve cuánto tiempo falta (en ms) para que termine el cooldown, o 0 si ya pasó. */
    public static long getRemainingCooldownMs(ServerPlayerEntity player) {
        long ts = getCooldownTimestamp(player);
        long now = System.currentTimeMillis();
        long restante = ts - now;
        return restante > 0 ? restante : 0L;
    }

    public static long getCooldownMs() {
        return COOLDOWN_MS;
    }
}
