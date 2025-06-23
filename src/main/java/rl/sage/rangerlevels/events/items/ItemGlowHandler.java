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
 * Al spawnear un ItemEntity de RangerLevels, lo ponemos glowing
 * y lo metemos en un equipo de Scoreboard cuyo nombre nunca excede los 16 chars.
 */
@Mod.EventBusSubscriber(modid = "rangerlevels")
public class ItemGlowHandler {
    // Prefijo muy corto (3 chars), luego 'comun', 'raro', etc.
    private static final String TEAM_PREFIX = "rg_";

    @SubscribeEvent
    public static void onItemEntitySpawn(EntityJoinWorldEvent event) {
        if (event.getWorld().isClientSide()) return;
        if (!(event.getEntity() instanceof ItemEntity)) return;

        ItemEntity itemEntity = (ItemEntity) event.getEntity();
        ItemStack stack = itemEntity.getItem();
        if (!stack.hasTag()) return;
        CompoundNBT tag = stack.getTag();
        if (!tag.contains("RangerID") || !tag.contains("RangerTier")) return;

        // 3) Tier stored in NBT
        Tier tier;
        try {
            tier = Tier.valueOf(tag.getString("RangerTier"));
        } catch (IllegalArgumentException e) {
            return;
        }

        // 4) map to TextFormatting
        TextFormatting color = getColorForTier(tier);
        if (color == null) return;

        // 5) build a team name that's <=16 chars:
        //    prefix (3) + '_' + tier.name().toLowerCase() (max 10) = <=14
        String teamName = TEAM_PREFIX + tier.name().toLowerCase();

        Scoreboard scoreboard = itemEntity.level.getScoreboard();
        ScorePlayerTeam team = scoreboard.getPlayerTeam(teamName);
        if (team == null) {
            team = scoreboard.addPlayerTeam(teamName);
            team.setColor(color);
            // aquí podrías configurar visibilidad, colisiones, etc.
        }

        // 6) asignar la entidad al equipo
        String entry = itemEntity.getScoreboardName();
        ScorePlayerTeam existing = scoreboard.getPlayersTeam(entry);
        if (existing != team) {
            scoreboard.addPlayerToTeam(entry, team);
        }

        // 7) activar glow
        itemEntity.setGlowing(true);
    }

    private static TextFormatting getColorForTier(Tier tier) {
        switch (tier) {
            case COMUN:      return TextFormatting.GRAY;
            case RARO:       return TextFormatting.DARK_AQUA;
            case EPICO:      return TextFormatting.LIGHT_PURPLE;
            case LEGENDARIO: return TextFormatting.GOLD;
            case ESTELAR:    return TextFormatting.AQUA;
            case MITICO:     return TextFormatting.GREEN;
            case SINGULAR:     return TextFormatting.DARK_RED;
            default:         return null;
        }
    }
}
