package rl.sage.rangerlevels.util;

import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;

public class GradientText {

    /**
     * Genera un componente de texto con un gradiente entre múltiples colores hexadecimales.
     * @param text El texto a colorear.
     * @param hexColors Colores en formato "#RRGGBB" o "RRGGBB".
     * @return Un IFormattableTextComponent con cada carácter coloreado.
     */
    public static IFormattableTextComponent of(String text, String... hexColors) {
        if (hexColors.length < 2) {
            throw new IllegalArgumentException("Se requieren al menos 2 colores para un gradiente.");
        }

        int[][] rgbs = new int[hexColors.length][3];
        for (int i = 0; i < hexColors.length; i++) {
            rgbs[i] = hexToRgb(hexColors[i]);
        }

        IFormattableTextComponent result = new StringTextComponent("");
        int len = text.length();

        for (int i = 0; i < len; i++) {
            double t = (double) i / (len - 1);
            int segmentCount = hexColors.length - 1;
            double scaledT = t * segmentCount;
            int segment = Math.min((int) scaledT, segmentCount - 1);
            double localT = scaledT - segment;

            int[] rgb = interpolate(rgbs[segment], rgbs[segment + 1], localT);
            int colorInt = (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
            Color color = Color.fromRgb(colorInt);
            Style style = Style.EMPTY.withColor(color);

            result.append(new StringTextComponent(String.valueOf(text.charAt(i))).setStyle(style));
        }

        return result;
    }

    private static int[] hexToRgb(String hex) {
        int rgb = Integer.parseInt(hex.replace("#", ""), 16);
        return new int[] { (rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF };
    }

    private static int[] interpolate(int[] start, int[] end, double t) {
        int r = (int) (start[0] + (end[0] - start[0]) * t);
        int g = (int) (start[1] + (end[1] - start[1]) * t);
        int b = (int) (start[2] + (end[2] - start[2]) * t);
        return new int[] { r, g, b };
    }
}
