package rl.sage.rangerlevels.util;

import net.minecraft.nbt.CompoundNBT;

public class NBTUtils {

    /**
     * Aplica los bits de HideFlags especificados sobre el tag NBT dado.
     * - Si ya existían HideFlags, los mezcla con OR.
     * - flags debería ser la máscara (por ejemplo, 1 para ocultar encantos, 32 para ocultar color de poción, etc.).
     */
    public static void applyHideFlags(CompoundNBT tag, int flags) {
        int hide = tag.contains("HideFlags") ? tag.getInt("HideFlags") : 0;
        hide |= flags;
        tag.putInt("HideFlags", hide);
    }

    /**
     * Aplica **todos** los flags posibles de ocultamiento y elimina información extra como el tag de Pixelmon.
     * Esto incluye:
     *   - Ocultar encantamientos, atributos, "unbreakable", CanDestroy, CanPlaceOn, efectos de pociones, tintes, mejoras.
     *   - Eliminar tag "pixelmon" para evitar tooltips especiales de Pixelmon.
     */
    public static void applyAllHideFlags(CompoundNBT tag) {
        // Eliminar tooltip de Pixelmon
        if (tag.contains("pixelmon")) {
            tag.remove("pixelmon");
        }

        // Aplicar todos los HideFlags (vanilla)
        int allFlags = 1   // encantamientos
                | 2   // atributos
                | 4   // unbreakable
                | 8   // CanDestroy
                | 16  // CanPlaceOn
                | 32  // efectos de poción
                | 64  // colorantes
                | 128; // mejoras (upgrades)

        applyHideFlags(tag, allFlags);
    }
}
