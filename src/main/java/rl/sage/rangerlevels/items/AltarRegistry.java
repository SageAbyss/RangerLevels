package rl.sage.rangerlevels.items;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.items.frasco.FrascoCalmaEstelar;
import rl.sage.rangerlevels.items.totems.fragmentos.FragmentoCorazonGaia;
import rl.sage.rangerlevels.items.totems.fragmentos.TotemRaizPrimordial;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AltarRegistry {

    public static void registerRecipes() {
        Map<String,Integer> ingredients = new HashMap<>();
        ingredients.put(FragmentoCorazonGaia.ID, 6);
        ingredients.put(FrascoCalmaEstelar.ID, 1);
        ingredients.put("pixelmon:gold_bottle_cap", 2);

        Supplier<net.minecraft.item.ItemStack> result = () ->
                CustomItemRegistry.create(TotemRaizPrimordial.ID, 1);

        AltarRecipe recipe = new AltarRecipe(
                new ResourceLocation("rangerlevels","totem_raiz_primordial"),
                ingredients,
                result,
                Tier.ESTELAR.applyGradient("⚡ Un latido ancestral retumbó al invocar el Tótem de Raíz Primordial ⚡"),
                new StringTextComponent("§c❌ No tienes los ingredientes necesarios para el ritual.")
        );
        AltarCraftHelper.registerRecipe(recipe);
    }
}
