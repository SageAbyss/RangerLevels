package rl.sage.rangerlevels.events.items;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rl.sage.rangerlevels.items.Tier;

/**
 * Cuando un ItemEntity (objeto tirado) entra al mundo, si es uno de nuestros ítems
 * (detectado por el tag "RangerID"), se le aplica glow y se asigna a un Scoreboard Team
 * cuyo color depende de su Tier.
 */
@Mod.EventBusSubscriber(modid = "rangerlevels")
public class ItemGlowHandler {

    private static final String TEAM_PREFIX = "ranger_glow_";

    @SubscribeEvent
    public static void onItemEntitySpawn(EntityJoinWorldEvent event) {
        // 1) Solo en servidor y solo ItemEntity
        if (event.getWorld().isClientSide()) return;
        if (!(event.getEntity() instanceof ItemEntity)) return;
        ItemEntity itemEntity = (ItemEntity) event.getEntity();

        // 2) Verificar tag “RangerID” para filtrar solo nuestros ítems
        ItemStack stack = itemEntity.getItem();
        if (stack == null || !stack.hasTag()) return;
        CompoundNBT tag = stack.getTag();
        if (!tag.contains("RangerID")) return;

        // 3) Leer el Tier desde el tag “RangerTier”
        if (!tag.contains("RangerTier")) {
            return; // si no tiene tier, no hacemos glow
        }
        Tier tier;
        try {
            tier = Tier.valueOf(tag.getString("RangerTier"));
        } catch (IllegalArgumentException e) {
            return; // valor inválido
        }

        // 4) Calcular el TextFormatting asociado a ese Tier
        TextFormatting color = getColorForTier(tier);
        if (color == null) {
            return;
        }

        // 5) Obtener (o crear) el equipo de Scoreboard para ese color
        String teamName = TEAM_PREFIX + color.getName(); // ej. "ranger_glow_gold"
        Scoreboard scoreboard = itemEntity.level.getScoreboard();
        ScorePlayerTeam team = scoreboard.getPlayerTeam(teamName);
        if (team == null) {
            team = scoreboard.addPlayerTeam(teamName);
            team.setColor(color);
            // Opcional: configurar visibilidad de tags, colisión, etc.
        }

        // 6) Agregar el ItemEntity al equipo usando scoreboard.addPlayerToTeam(...)
        String entry = itemEntity.getScoreboardName(); // normalmente la UUID en String
        // Comprobamos que no esté ya en ese equipo:
        ScorePlayerTeam existing = scoreboard.getPlayersTeam(entry);
        if (existing != team) {
            scoreboard.addPlayerToTeam(entry, team);
        }

        // 7) Activar el glow
        itemEntity.setGlowing(true);
    }

    /**
     * Mapea cada Tier a un TextFormatting compatible con Scoreboard teams.
     */
    private static TextFormatting getColorForTier(Tier tier) {
        switch (tier) {
            case COMUN:      return TextFormatting.GRAY;
            case RARO:       return TextFormatting.DARK_AQUA;
            case EPICO:      return TextFormatting.LIGHT_PURPLE;
            case LEGENDARIO: return TextFormatting.GOLD;
            case ESTELAR:    return TextFormatting.AQUA;
            case MITICO:     return TextFormatting.GREEN;
            default:         return null;
        }
    }
}
