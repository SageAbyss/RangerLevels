package rl.sage.rangerlevels.items;

import net.minecraft.item.ItemStack;

import java.util.*;

/**
 * Registry estático que mantiene un mapa id → RangerItemDefinition.
 * Permite:
 *  - registrar nuevas definiciones (register)
 *  - verificar si existe una definición (contains)
 *  - crear el ItemStack correspondiente (create)
 *  - obtener todas las IDs registradas (getAllIds)
 */
public class CustomItemRegistry {
    private static final Map<String, RangerItemDefinition> DEFINITIONS = new HashMap<>();

    public static void register(RangerItemDefinition def) {
        DEFINITIONS.put(def.getId(), def);
    }

    public static boolean contains(String ID) {
        return DEFINITIONS.containsKey(ID);
    }

    public static ItemStack create(String ID, int amount) {
        RangerItemDefinition def = DEFINITIONS.get(ID);
        return def == null ? ItemStack.EMPTY : def.createStack(amount);
    }

    /**
     * Devuelve un Set inmodificable con todas las IDs registradas.
     * Si necesitas una List, basta con hacer new ArrayList<>(getAllIds()).
     */
    public static Set<String> getAllIds() {
        return Collections.unmodifiableSet(DEFINITIONS.keySet());
    }

    /**
     * (Opcional) Devuelve una List con todas las IDs registradas,
     * en caso de que prefieras trabajar con List en lugar de Set.
     */
    public static RangerItemDefinition getDefinition(String id) {
        return DEFINITIONS.get(id);
    }
}
