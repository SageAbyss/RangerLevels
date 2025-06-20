package rl.sage.rangerlevels.items.altar;

import java.util.*;

public class InvocationCraftHelper {

    private static final Map<String, List<AltarRecipe>> RECIPES_BY_ALTAR = new HashMap<>();

    public static void registerRecipe(String altarId, AltarRecipe recipe) {
        RECIPES_BY_ALTAR
                .computeIfAbsent(altarId, k -> new ArrayList<>())
                .add(recipe);
    }

    public static List<AltarRecipe> getRecipesForAltar(String altarId) {
        return RECIPES_BY_ALTAR.getOrDefault(altarId, Collections.emptyList());
    }
}
