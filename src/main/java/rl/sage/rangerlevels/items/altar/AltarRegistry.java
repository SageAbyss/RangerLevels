package rl.sage.rangerlevels.items.altar;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.items.Tier;
import rl.sage.rangerlevels.items.gemas.GemaExpLegendario;
import rl.sage.rangerlevels.items.randoms.LagrimaDiosaTiempo;
import rl.sage.rangerlevels.items.reliquias.SangreQuetzalEstelar;
import rl.sage.rangerlevels.items.reliquias.SangreQuetzalLegendario;
import rl.sage.rangerlevels.items.reliquias.SangreQuetzalMitico;
import rl.sage.rangerlevels.items.tickets.CarameloNivel;
import rl.sage.rangerlevels.items.tickets.TicketNivel;
import rl.sage.rangerlevels.items.totems.fragmentos.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AltarRegistry {
    public static void registerRecipes() {
        registerTotemRaizPrimordial();
        registerTotemLamentoDioses();
        registerTotemAbismoGlacial();
        registerLagrimaTiempo();
        registerSangreQuetzal();
        // ... aquí tantas recetas como necesites
    }

    public static void registerTotemRaizPrimordial() {
        Map<String,Integer> ingredients = new HashMap<>();
        ingredients.put(FragmentoCorazonGaia.ID, 6);
        ingredients.put(GenesisArcano.ID, 1);
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
    private static void registerTotemLamentoDioses() {
        Map<String,Integer> ingredients = new HashMap<>();
        ingredients.put(FragmentoIraAncestral.ID, 6);
        ingredients.put(GenesisArcano.ID, 1);
        ingredients.put("pixelmon:gold_bottle_cap", 2);

        Supplier<ItemStack> result = () ->
                CustomItemRegistry.create(TotemLamentoDioses.ID, 1);

        AltarRecipe recipe = new AltarRecipe(
                new ResourceLocation("rangerlevels","totem_lamento_dioses"),
                ingredients,
                result,
                Tier.ESTELAR.applyGradient("§5☁ El lamento de los Dioses resonó y apareció el Tótem del Lamento de los Dioses ☁"),
                new StringTextComponent("§c❌ No cumples los requisitos del ritual para el Tótem del Lamento de los Dioses.")
        );
        AltarCraftHelper.registerRecipe(recipe);
    }
    private static void registerTotemAbismoGlacial() {
        Map<String,Integer> ingredients = new HashMap<>();
        ingredients.put(FragmentoRealidadAlterna.ID, 6);
        ingredients.put(GenesisArcano.ID, 1);
        ingredients.put("pixelmon:gold_bottle_cap", 2);

        Supplier<ItemStack> result = () ->
                CustomItemRegistry.create(TotemAbismoGlacial.ID, 1);

        AltarRecipe recipe = new AltarRecipe(
                new ResourceLocation("rangerlevels","totem_abismo_glacial"),
                ingredients,
                result,
                Tier.ESTELAR.applyGradient("§5☁ El lamento de Aurora resonó y apareció el Tótem del Abismo Glacial ☁"),
                new StringTextComponent("§c❌ No cumples los requisitos del ritual para el Tótem del Abismo Glacial.")
        );
        AltarCraftHelper.registerRecipe(recipe);
    }
    //ARREGLAR v
    private static void registerLagrimaTiempo() {
        Map<String,Integer> ingredients = new HashMap<>();
        ingredients.put(TicketNivel.ID, 2);
        ingredients.put(CarameloNivel.ID, 2);
        ingredients.put(GemaExpLegendario.ID, 2);
        ingredients.put(GenesisArcano.ID, 1);

        Supplier<ItemStack> result = () ->
                CustomItemRegistry.create(LagrimaDiosaTiempo.ID, 1);

        AltarRecipe recipe = new AltarRecipe(
                new ResourceLocation("rangerlevels","lagrima_diosa_tiempo"),
                ingredients,
                result,
                Tier.ESTELAR.applyGradient("§5☁ El lamento de Aurora resonó y apareció el Tótem del Abismo Glacial ☁"),
                new StringTextComponent("§c❌ No cumples los requisitos del ritual para el Tótem del Abismo Glacial.")
        );
        AltarCraftHelper.registerRecipe(recipe);
    }
    private static void registerSangreQuetzal() {
        Map<String,Integer> ingredients = new HashMap<>();
        ingredients.put(SangreQuetzalLegendario.ID, 2);
        ingredients.put(SangreQuetzalEstelar.ID, 2);
        ingredients.put(GemaExpLegendario.ID, 2);
        ingredients.put(GenesisArcano.ID, 1);

        Supplier<ItemStack> result = () ->
                CustomItemRegistry.create(SangreQuetzalMitico.ID, 1);

        AltarRecipe recipe = new AltarRecipe(
                new ResourceLocation("rangerlevels","sangre_quetzal_mitico"),
                ingredients,
                result,
                Tier.ESTELAR.applyGradient("§5☁ El lamento de Aurora resonó y apareció el Tótem del Abismo Glacial ☁"),
                new StringTextComponent("§c❌ No cumples los requisitos del ritual para el Tótem del Abismo Glacial.")
        );
        AltarCraftHelper.registerRecipe(recipe);
    }
}
