package rl.sage.rangerlevels.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Helper para obtener mensajes aleatorios en las diferentes etapas de la invocación.
 * Usa plantillas con placeholders como %player%, %x%, %y%, %z%, %key% para personalizar.
 */
public class InvocationMessageHelper {

    // Mensajes globales de inicio de invocación (se formatea con coordenadas o nombre si quieres)
    private static final List<String> START_TEMPLATES = Arrays.asList(
            "§d✦ Una energía ancestral se activa en %d,%d,%d... El Altar Supremo despierta. ✦",
            "§5❖ Las estrellas convergen en %d,%d,%d: comienza la invocación del Altar Supremo. ❖",
            "§b☄ Un susurro cósmico retumba en %d,%d,%d: el Altar Supremo inicia su ritual. ☄",
            "§6✵ El cielo se parte en %d,%d,%d mientras el Altar Supremo cobra vida. ✵"
    );

    // Mensajes dinámicos de consumo de ingrediente (se formatea con nombre de jugador y clave del item)
    private static final List<String> CONSUME_TEMPLATES = Arrays.asList(
            "§3→ %s introduce §e%key%§3 en el círculo. Preparando Invocación...",
            "§3→ %s introduce §e%key%§3 en el círculo, Preparando Invocación..."
    );

    // Mensajes genéricos de fallo (si falla al final)
    private static final List<String> FAILURE_TEMPLATES = Arrays.asList(
            "§4✖ El ritual colapsa en llamas caóticas y todo se consume. ✖",
            "§c☠ La invocación estalla en un cataclismo y el Altar sucumbe. ☠",
            "§4⚠ El poder se desbordó y la invocación fracasa en una explosión apocalíptica. ⚠",
            "§c❌ Las fuerzas se vuelven contra el invocador: la invocación termina en ruina. ❌"
    );

    // Mensajes genéricos de éxito (se formatea con nombre de jugador)
    private static final List<String> SUCCESS_TEMPLATES = Arrays.asList(
            "§6✦ La invocación del Altar Supremo ha triunfado: ¡felicidades, %s, elegido por el cosmos! ✦",
            "§e✧ El Altar Supremo responde al llamado y otorga su don a %s. ¡Gloria! ✧",
            "§a☀ Éxito absoluto: %s ha sido ungido por el poder estelar. ☀",
            "§b❖ Las fuerzas antiguas aplauden: %s completa el rito con maestría. ❖"
    );

    // Mensajes para anunciar inicio local al jugador (títulos o subtítulos), ejemplo:
    private static final List<String> TITLE_TEMPLATES = Arrays.asList(
            "§6Preparando invocación...",
            "§aLas fuerzas despiertan...",
            "§bEl ritual comienza...",
            "§dEl Altar se activa..."
    );

    /** Obtiene mensaje global de inicio formateado con coordenadas. */
    public static IFormattableTextComponent getRandomStartMessage(int x, int y, int z) {
        String template = START_TEMPLATES.get(ThreadLocalRandom.current().nextInt(START_TEMPLATES.size()));
        String msg = String.format(template, x, y, z);
        return new StringTextComponent(msg);
    }

    /** Obtiene mensaje dinámico de consumo, formateando con nombre de jugador y clave. */
    public static IFormattableTextComponent getRandomConsumeMessage(String playerName, String key) {
        ItemStack stack = ItemStack.EMPTY;
        if (CustomItemRegistry.contains(key)) {
            stack = CustomItemRegistry.create(key, 1);
        }
        if (stack.isEmpty()) {
            ResourceLocation loc = key.contains(":")
                    ? new ResourceLocation(key)
                    : new ResourceLocation("rangerlevels", key);  // opcional, si guardas mods propios sin namespace
            Item vanilla = ForgeRegistries.ITEMS.getValue(loc);
            if (vanilla != null) {
                stack = new ItemStack(vanilla);
            }
        }
        String itemName = !stack.isEmpty()
                ? stack.getHoverName().getString()
                : key;
        String template = CONSUME_TEMPLATES.get(
                ThreadLocalRandom.current().nextInt(CONSUME_TEMPLATES.size())
        );
        String msg = template
                .replace("%s", playerName)
                .replace("%key%", itemName);
        return new StringTextComponent(msg);
    }


    /** Mensaje de éxito global, formateando con nombre de jugador. */
    public static IFormattableTextComponent getRandomSuccessMessage(String playerName) {
        String template = SUCCESS_TEMPLATES.get(ThreadLocalRandom.current().nextInt(SUCCESS_TEMPLATES.size()));
        String msg = String.format(template, playerName);
        return new StringTextComponent(msg);
    }

    /** Mensaje de fallo global. */
    public static IFormattableTextComponent getRandomFailureMessage() {
        String template = FAILURE_TEMPLATES.get(ThreadLocalRandom.current().nextInt(FAILURE_TEMPLATES.size()));
        return new StringTextComponent(template);
    }

    /** Mensaje para títulos/subtítulos durante countdown, formatea “phase” si lo deseas. */
    public static IFormattableTextComponent getRandomTitleMessage() {
        String template = TITLE_TEMPLATES.get(ThreadLocalRandom.current().nextInt(TITLE_TEMPLATES.size()));
        return new StringTextComponent(template);
    }

    // Si necesitas más secciones (p.ej. mensajes de aviso de falta de ingrediente), 
    // puedes añadir aquí listas y métodos similares.
}
