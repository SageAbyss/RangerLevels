package rl.sage.rangerlevels.items;

import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.Style;
import rl.sage.rangerlevels.util.GradientText;

public enum Tier {
    COMUN     ("ᴄᴏᴍᴍᴏɴ",   new String[]{"#E2E2E2", "#9D9D9D"}),
    RARO      ("ʀᴀʀᴇ",     new String[]{"#ADD8E6", "#84C1E0"}),
    EPICO     ("ᴇᴘɪᴄ",     new String[]{"#BB8CFF", "#A346FF"}),
    LEGENDARIO("ʟᴇɢᴇɴᴅ",   new String[]{"#FFE87C", "#FFD700"}),
    ESTELAR   ("ᴇꜱᴛᴇʟᴀʀ", new String[]{"#6EE3DC", "#996FEA"}),
    MITICO    ("ᴍʏᴛʜɪᴄ",   new String[]{"#11998E", "#38EF7D"}),
    SINGULAR    ("⨳⨳⨳⨳⨳",   new String[]{"#FF4500", "#8B0000"});


    private final String displayName;
    private final String[] gradientHex;

    Tier(String displayName, String[] gradientHex) {
        this.displayName = displayName;
        this.gradientHex  = gradientHex;
    }

    /** Retorna el nombre amigable con acento (ej. "ᴍʏᴛʜɪᴄ"). */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Devuelve el nombre del tier con degradado y sin cursiva.
     * Úsalo si realmente quieres mostrar el nombre interno del enum, pintado en degradado.
     */
    public IFormattableTextComponent getColor() {
        // GradientText.of(displayName, ...) produce un componente posiblemente con estilos por defecto.
        // Con withStyle(...) quitamos la cursiva:
        return GradientText
                .of(displayName, gradientHex[0], gradientHex[1])
                .withStyle(style -> style.withItalic(false));
    }

    /**
     * Pinta cualquier texto con el degradado de este tier y sin cursiva.
     * Ejemplo: Tier.LEGENDARIO.applyGradient("✦ Mi Ítem Legendario ✦")
     */
    public IFormattableTextComponent applyGradient(String text) {
        return GradientText
                .of(text, gradientHex[0], gradientHex[1])
                .withStyle(style -> style.withItalic(false));
    }
}
