package rl.sage.rangerlevels.util;

import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextFormatterUtil {
    private static final Pattern TOKEN = Pattern.compile(
            "(?<LEGACY>[&§][0-9A-Fa-fk-orK-OR])"
                    + "|(?<HEX>&#[0-9A-Fa-f]{6})"
                    + "|(?<XHEX>&x(?:&[0-9A-Fa-f]){6})"
    );

    public static IFormattableTextComponent parse(String input) {
        IFormattableTextComponent result = new StringTextComponent("");
        Matcher m = TOKEN.matcher(input);
        int lastPos = 0;
        Style currentStyle = Style.EMPTY;

        while (m.find()) {
            // texto entre tokens
            if (m.start() > lastPos) {
                String chunk = input.substring(lastPos, m.start());
                result.append(new StringTextComponent(chunk).setStyle(currentStyle));
            }

            // HEX “&#RRGGBB”
            if (m.group("HEX") != null) {
                List<String> hexes = new ArrayList<>();
                hexes.add(m.group("HEX").substring(2));
                int scan = m.end();
                Matcher look = TOKEN.matcher(input);
                look.region(scan, input.length());
                while (look.lookingAt() && look.group("HEX") != null) {
                    hexes.add(look.group("HEX").substring(2));
                    scan = look.end();
                    look.region(scan, input.length());
                }
                if (hexes.size() >= 2) {
                    int next = input.length();
                    if (look.find()) next = look.start();
                    String txt = input.substring(scan, next);
                    result.append(gradientOf(txt, hexes));
                    m.region(next, input.length());
                    lastPos = next;
                } else {
                    currentStyle = styleFromHex(hexes.get(0));
                    lastPos = m.end();
                }
                continue;
            }

            // XHEX “&x&R&R&G&G&B&B”
            if (m.group("XHEX") != null) {
                String seq = m.group("XHEX");
                StringBuilder hex = new StringBuilder(6);
                for (int i = 3; i < seq.length(); i += 2) hex.append(seq.charAt(i));
                currentStyle = styleFromHex(hex.toString());
                lastPos = m.end();
                continue;
            }

            // LEGACY “&6” / “§6”, “&l” / “§l”, etc.
            if (m.group("LEGACY") != null) {
                char codeChar = m.group("LEGACY").charAt(1);
                currentStyle = applyLegacy(currentStyle, codeChar);
                lastPos = m.end();
            }
        }

        // resto de texto
        if (lastPos < input.length()) {
            String tail = input.substring(lastPos);
            result.append(new StringTextComponent(tail).setStyle(currentStyle));
        }
        return result;
    }

    // ——— Manejo manual de legacy codes ———
    private static Style applyLegacy(Style base, char codeChar) {
        char c = Character.toLowerCase(codeChar);
        switch (c) {
            // Colores
            case '0': return Style.EMPTY.withColor(Color.fromRgb(0x000000)); // Black
            case '1': return Style.EMPTY.withColor(Color.fromRgb(0x0000AA)); // Dark Blue
            case '2': return Style.EMPTY.withColor(Color.fromRgb(0x00AA00)); // Dark Green
            case '3': return Style.EMPTY.withColor(Color.fromRgb(0x00AAAA)); // Dark Aqua
            case '4': return Style.EMPTY.withColor(Color.fromRgb(0xAA0000)); // Dark Red
            case '5': return Style.EMPTY.withColor(Color.fromRgb(0xAA00AA)); // Dark Purple
            case '6': return Style.EMPTY.withColor(Color.fromRgb(0xFFAA00)); // Gold
            case '7': return Style.EMPTY.withColor(Color.fromRgb(0xAAAAAA)); // Gray
            case '8': return Style.EMPTY.withColor(Color.fromRgb(0x555555)); // Dark Gray
            case '9': return Style.EMPTY.withColor(Color.fromRgb(0x5555FF)); // Blue
            case 'a': return Style.EMPTY.withColor(Color.fromRgb(0x55FF55)); // Green
            case 'b': return Style.EMPTY.withColor(Color.fromRgb(0x55FFFF)); // Aqua
            case 'c': return Style.EMPTY.withColor(Color.fromRgb(0xFF5555)); // Red
            case 'd': return Style.EMPTY.withColor(Color.fromRgb(0xFF55FF)); // Light Purple
            case 'e': return Style.EMPTY.withColor(Color.fromRgb(0xFFFF55)); // Yellow
            case 'f': return Style.EMPTY.withColor(Color.fromRgb(0xFFFFFF)); // White

            // Formateos
            case 'k': return base.setObfuscated(true);
            case 'l': return base.withBold(true);
            case 'm': return base.setStrikethrough(true);
            case 'n': return base.withUnderlined(true);
            case 'o': return base.withItalic(true);

            case 'r': // reset
                return Style.EMPTY;

            default:
                return base;
        }
    }

    // ——— Gradiente ———

    private static IFormattableTextComponent gradientOf(String text, List<String> hexStops) {
        int n = hexStops.size();
        int[][] rgbs = new int[n][3];
        for (int i = 0; i < n; i++) rgbs[i] = hexToRgb(hexStops.get(i));

        IFormattableTextComponent comp = new StringTextComponent("");
        int len = text.length();
        int segs = n - 1;
        for (int i = 0; i < len; i++) {
            double t = (double) i / Math.max(1, len - 1);
            double s = t * segs;
            int seg = Math.min((int) s, segs - 1);
            double lt = s - seg;
            int[] col = interpolate(rgbs[seg], rgbs[seg + 1], lt);
            int ci = (col[0] << 16) | (col[1] << 8) | col[2];
            comp.append(new StringTextComponent(String.valueOf(text.charAt(i)))
                    .setStyle(Style.EMPTY.withColor(Color.fromRgb(ci))));
        }
        return comp;
    }

    // ——— Utilidades de color ———

    private static int[] hexToRgb(String h) {
        int v = Integer.parseInt(h, 16);
        return new int[]{(v >> 16) & 0xFF, (v >> 8) & 0xFF, v & 0xFF};
    }

    private static int[] interpolate(int[] a, int[] b, double t) {
        return new int[]{
                (int) (a[0] + (b[0] - a[0]) * t),
                (int) (a[1] + (b[1] - a[1]) * t),
                (int) (a[2] + (b[2] - a[2]) * t)
        };
    }

    private static Style styleFromHex(String hex) {
        int v = Integer.parseInt(hex, 16);
        return Style.EMPTY.withColor(Color.fromRgb(v));
    }
}
