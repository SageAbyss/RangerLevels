package rl.sage.rangerlevels.items;

import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Registry estático que mantiene un mapa id → RangerItemDefinition.
 * Permite:
 *  - registrar nuevas definiciones (register)
 *  - verificar si existe una definición (contains)
 *  - crear el ItemStack correspondiente (create)
 */
public class CustomItemRegistry {
    private static final Map<String, RangerItemDefinition> DEFINITIONS = new HashMap<>();

    /**
     * Registra una definición nueva. La clave será def.getId().
     */
    public static void register(RangerItemDefinition def) {
        DEFINITIONS.put(def.getId(), def);
    }

    public static boolean contains(String ID) {
        return DEFINITIONS.containsKey(ID);
    }

    /**
     * Crea un ItemStack con la definición asociada a “id”.
     * Si no existe, devuelve ItemStack.EMPTY.
     */
    public static ItemStack create(String ID, int amount) {
        RangerItemDefinition def = DEFINITIONS.get(ID);
        if (def == null) {
            return ItemStack.EMPTY;
        }
        return def.createStack(amount);
    }

    public static Set<String> getAllIds() {
        return DEFINITIONS.keySet();
    }
}
