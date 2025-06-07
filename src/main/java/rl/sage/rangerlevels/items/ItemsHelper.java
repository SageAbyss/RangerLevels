// File: rl/sage/rangerlevels/items/ItemsHelper.java
package rl.sage.rangerlevels.items;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Helper para obtener dinámicamente los IDs de los ítems propios
 * (excluyendo las cajas misteriosas).
 */
public class ItemsHelper {

    /**
     * Devuelve la lista de IDs de todos los RangerItems registrados
     * excepto aquellos que sean MysteryBoxes.
     */
    public static List<String> getRewardItemIds() {
        Set<String> all = CustomItemRegistry.getAllIds();
        return all.stream()
                // Excluir IDs de cajas
                .filter(id -> !id.startsWith("caja_misteriosa_"))
                .collect(Collectors.toList());
    }

}
