package rl.sage.rangerlevels.util;

import net.minecraft.util.text.Color;
import net.minecraft.util.text.Style;

/**
 * Utilidades para crear estilos de texto con colores hexadecimales.
 */
public class TextColorUtil {

    /**
     * Convierte un código hex de la forma "#RRGGBB" a un {@link Color}.
     * @param hexColor la cadena "#RRGGBB" o "RRGGBB"
     * @return el Color correspondiente
     * @throws IllegalArgumentException si el formato es inválido
     */
    public static Color fromHex(String hexColor) {
        if (hexColor == null) {
            throw new IllegalArgumentException("Color hex nulo");
        }
        String clean = hexColor.startsWith("#") ? hexColor.substring(1) : hexColor;
        if (!clean.matches("[0-9A-Fa-f]{6}")) {
            throw new IllegalArgumentException("Color hex inválido: " + hexColor);
        }
        int rgb = Integer.parseInt(clean, 16);
        return Color.fromRgb(rgb);
    }

    /**
     * Crea un {@link Style} con el color dado en hex.
     * @param hexColor "#RRGGBB" o "RRGGBB"
     */
    public static Style styleFromHex(String hexColor) {
        return Style.EMPTY.withColor(fromHex(hexColor));
    }
}
