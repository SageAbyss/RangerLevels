package rl.sage.rangerlevels.items;

import net.minecraft.util.text.TextFormatting;

public enum Tier {
    COMUN(TextFormatting.WHITE,     "ᴄᴏᴍᴍᴏɴ"),
    RARO(TextFormatting.BLUE,       "ʀᴀʀᴇ"),
    EPICO(TextFormatting.AQUA,      "ᴇᴘɪᴄ"),
    LEGENDARIO(TextFormatting.GOLD, "ʟᴇɢᴇɴᴅ"),
    ESTELAR(TextFormatting.DARK_AQUA, "ᴇꜱᴛᴇʟᴀʀ"),
    MITICO(TextFormatting.LIGHT_PURPLE,  "ᴍʏᴛʜɪᴄ");

    private final TextFormatting color;
    private final String displayName;

    Tier(TextFormatting color, String displayName) {
        this.color = color;
        this.displayName = displayName;
    }

    /**
     * Retorna el nombre amigable con acento (ej. "Épico").
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Retorna el color asociado a este tier (TextFormatting).
     */
    public TextFormatting getColor() {
        return color;
    }
}
