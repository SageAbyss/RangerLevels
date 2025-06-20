package rl.sage.rangerlevels.items.altar;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.items.Tier;
import rl.sage.rangerlevels.items.modificadores.*;
import rl.sage.rangerlevels.items.sacrificios.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AltarAlmasRegistry {

    /** Llama a todos los métodos de registro */
    public static void registerRecipes() {
        registerNatureModifierL();
        registerNatureModifierU();
        registerNatureModifierLUni();
        registerNatureModifierUUni();
        registerSizeModifierL();
        registerSizeModifierU();
        registerSizeModifierLUni();
        registerSizeModifierUUni();
        registerShinyL();
        registerShinyU();
        registerShinyLUni();
        registerShinyUUni();
        registerIVsL();
        registerIVsU();
        registerConcentradoL();
        registerConcentradoU();
        registerNucleoL();
        registerNucleoU();
        registerIVsLUniversal();
        registerIVsUUniversal();
        // … añade aquí más invocaciones ligadas al mismo altar
    }

    private static void registerNatureModifierL() {
        Map<String,Integer> ingredients = new HashMap<>();
        ingredients.put(EsenciaLegendaria.ID, 3);
        ingredients.put("pixelmon:mint", 1);
        ingredients.put(NucleoDeSacrificio.ID, 1);

        Supplier<ItemStack> result = () ->
                CustomItemRegistry.create(ModificadorNaturaleza.ID, 1);

        AltarRecipe recipe = new AltarRecipe(
                new ResourceLocation("rangerlevels", "invocacion_modificador_naturaleza_l"),
                ingredients,
                result,
                Tier.LEGENDARIO.applyGradient("✦ Activaste el Altar de Almas y materializó el Modificador de Naturaleza ✦"),
                new StringTextComponent("§cNo cumples los requisitos para invocar el Modificador de Naturaleza.")
        );
        InvocationCraftHelper.registerRecipe(AltarAlmas.ID, recipe);
    }
    private static void registerNatureModifierU() {
        Map<String,Integer> ingredients = new HashMap<>();
        ingredients.put(EsenciaUltraente.ID, 3);
        ingredients.put("pixelmon:mint", 1);
        ingredients.put(NucleoDeSacrificio.ID, 1);

        Supplier<ItemStack> result = () ->
                CustomItemRegistry.create(ModificadorNaturaleza.ID, 1);

        AltarRecipe recipe = new AltarRecipe(
                new ResourceLocation("rangerlevels", "invocacion_modificador_naturaleza_u"),
                ingredients,
                result,
                Tier.LEGENDARIO.applyGradient("✦ Activaste el Altar de Almas y materializó el Modificador de Naturaleza ✦"),
                new StringTextComponent("§cNo cumples los requisitos para invocar el Modificador de Naturaleza.")
        );
        InvocationCraftHelper.registerRecipe(AltarAlmas.ID, recipe);
    }
    private static void registerNatureModifierLUni() {
        Map<String,Integer> ingredients = new HashMap<>();
        ingredients.put(EsenciaLegendaria.ID, 6);
        ingredients.put("pixelmon:mint", 1);
        ingredients.put(NucleoDeSacrificio.ID, 1);

        Supplier<ItemStack> result = () ->
                CustomItemRegistry.create(ModificadorNaturalezaUniversal.ID, 1);

        AltarRecipe recipe = new AltarRecipe(
                new ResourceLocation("rangerlevels", "invocacion_modificador_naturaleza_universal_l"),
                ingredients,
                result,
                Tier.LEGENDARIO.applyGradient("✦ Activaste el Altar de Almas y materializó el Modificador de Naturaleza Universal ✦"),
                new StringTextComponent("§cNo cumples los requisitos para invocar el Modificador de Naturaleza Universal.")
        );
        InvocationCraftHelper.registerRecipe(AltarAlmas.ID, recipe);
    }
    private static void registerNatureModifierUUni() {
        Map<String,Integer> ingredients = new HashMap<>();
        ingredients.put(EsenciaUltraente.ID, 6);
        ingredients.put("pixelmon:mint", 1);
        ingredients.put(NucleoDeSacrificio.ID, 1);

        Supplier<ItemStack> result = () ->
                CustomItemRegistry.create(ModificadorNaturalezaUniversal.ID, 1);

        AltarRecipe recipe = new AltarRecipe(
                new ResourceLocation("rangerlevels", "invocacion_modificador_naturaleza_universal_u"),
                ingredients,
                result,
                Tier.LEGENDARIO.applyGradient("✦ Activaste el Altar de Almas y materializó el Modificador de Naturaleza Universal ✦"),
                new StringTextComponent("§cNo cumples los requisitos para invocar el Modificador de Naturaleza Universal.")
        );
        InvocationCraftHelper.registerRecipe(AltarAlmas.ID, recipe);
    }
    private static void registerSizeModifierL() {
        Map<String,Integer> ingredients = new HashMap<>();
        ingredients.put(EsenciaLegendaria.ID, 3);
        ingredients.put("minecraft:bone_meal", 64);
        ingredients.put(NucleoDeSacrificio.ID, 1);

        Supplier<ItemStack> result = () ->
                CustomItemRegistry.create(ModificadorTamano.ID, 1);

        AltarRecipe recipe = new AltarRecipe(
                new ResourceLocation("rangerlevels", "invocacion_modificador_tamano_l"),
                ingredients,
                result,
                Tier.LEGENDARIO.applyGradient("✦ Activaste el Altar de Almas y materializó el Modificador de Tamaño ✦"),
                new StringTextComponent("§cNo cumples los requisitos para invocar el Modificador de Tamaño.")
        );
        InvocationCraftHelper.registerRecipe(AltarAlmas.ID, recipe);
    }
    private static void registerSizeModifierU() {
        Map<String,Integer> ingredients = new HashMap<>();
        ingredients.put(EsenciaUltraente.ID, 3);
        ingredients.put("minecraft:bone_meal", 64);
        ingredients.put(NucleoDeSacrificio.ID, 1);

        Supplier<ItemStack> result = () ->
                CustomItemRegistry.create(ModificadorTamano.ID, 1);

        AltarRecipe recipe = new AltarRecipe(
                new ResourceLocation("rangerlevels", "invocacion_modificador_tamano_u"),
                ingredients,
                result,
                Tier.LEGENDARIO.applyGradient("✦ Activaste el Altar de Almas y materializó el Modificador de Tamaño ✦"),
                new StringTextComponent("§cNo cumples los requisitos para invocar el Modificador de Tamaño.")
        );
        InvocationCraftHelper.registerRecipe(AltarAlmas.ID, recipe);
    }
    private static void registerSizeModifierLUni() {
        Map<String,Integer> ingredients = new HashMap<>();
        ingredients.put(EsenciaLegendaria.ID, 6);
        ingredients.put("minecraft:bone_meal", 64);
        ingredients.put(NucleoDeSacrificio.ID, 1);

        Supplier<ItemStack> result = () ->
                CustomItemRegistry.create(ModificadorTamanoUniversal.ID, 1);

        AltarRecipe recipe = new AltarRecipe(
                new ResourceLocation("rangerlevels", "invocacion_modificador_tamano_universal_l"),
                ingredients,
                result,
                Tier.LEGENDARIO.applyGradient("✦ Activaste el Altar de Almas y materializó el Modificador de Tamaño Universal ✦"),
                new StringTextComponent("§cNo cumples los requisitos para invocar el Modificador de Tamaño Universal.")
        );
        InvocationCraftHelper.registerRecipe(AltarAlmas.ID, recipe);
    }
    private static void registerSizeModifierUUni() {
        Map<String,Integer> ingredients = new HashMap<>();
        ingredients.put(EsenciaUltraente.ID, 6);
        ingredients.put("minecraft:bone_meal", 64);
        ingredients.put(NucleoDeSacrificio.ID, 1);

        Supplier<ItemStack> result = () ->
                CustomItemRegistry.create(ModificadorTamanoUniversal.ID, 1);

        AltarRecipe recipe = new AltarRecipe(
                new ResourceLocation("rangerlevels", "invocacion_modificador_tamano_universal_u"),
                ingredients,
                result,
                Tier.LEGENDARIO.applyGradient("✦ Activaste el Altar de Almas y materializó el Modificador de Tamaño Universal ✦"),
                new StringTextComponent("§cNo cumples los requisitos para invocar el Modificador de Tamaño Universal.")
        );
        InvocationCraftHelper.registerRecipe(AltarAlmas.ID, recipe);
    }
    private static void registerShinyL() {
        Map<String,Integer> ingredients = new HashMap<>();
        ingredients.put(EsenciaLegendaria.ID, 3);
        ingredients.put("minecraft:yellow_dye", 16);
        ingredients.put(NucleoDeSacrificio.ID, 1);

        Supplier<ItemStack> result = () ->
                CustomItemRegistry.create(ModificadorShiny.ID, 1);

        AltarRecipe recipe = new AltarRecipe(
                new ResourceLocation("rangerlevels", "invocacion_modificador_shiny_l"),
                ingredients,
                result,
                Tier.LEGENDARIO.applyGradient("✦ Activaste el Altar de Almas y materializó el Modificador Shiny ✦"),
                new StringTextComponent("§cNo cumples los requisitos para invocar el Modificador Shiny.")
        );
        InvocationCraftHelper.registerRecipe(AltarAlmas.ID, recipe);
    }
    private static void registerShinyLUni() {
        Map<String,Integer> ingredients = new HashMap<>();
        ingredients.put(EsenciaLegendaria.ID, 6);
        ingredients.put("minecraft:yellow_dye", 16);
        ingredients.put(NucleoDeSacrificio.ID, 1);

        Supplier<ItemStack> result = () ->
                CustomItemRegistry.create(ModificadorShinyUniversal.ID, 1);

        AltarRecipe recipe = new AltarRecipe(
                new ResourceLocation("rangerlevels", "invocacion_modificador_shiny_universal_l"),
                ingredients,
                result,
                Tier.LEGENDARIO.applyGradient("✦ Activaste el Altar de Almas y materializó el Modificador Shiny Universal ✦"),
                new StringTextComponent("§cNo cumples los requisitos para invocar el Modificador Shiny Universal.")
        );
        InvocationCraftHelper.registerRecipe(AltarAlmas.ID, recipe);
    }
    private static void registerShinyUUni() {
        Map<String,Integer> ingredients = new HashMap<>();
        ingredients.put(EsenciaUltraente.ID, 6);
        ingredients.put("minecraft:yellow_dye", 16);
        ingredients.put(NucleoDeSacrificio.ID, 1);

        Supplier<ItemStack> result = () ->
                CustomItemRegistry.create(ModificadorShinyUniversal.ID, 1);

        AltarRecipe recipe = new AltarRecipe(
                new ResourceLocation("rangerlevels", "invocacion_modificador_shiny_universal_u"),
                ingredients,
                result,
                Tier.LEGENDARIO.applyGradient("✦ Activaste el Altar de Almas y materializó el Modificador Shiny Universal ✦"),
                new StringTextComponent("§cNo cumples los requisitos para invocar el Modificador Shiny Universal.")
        );
        InvocationCraftHelper.registerRecipe(AltarAlmas.ID, recipe);
    }
    private static void registerShinyU() {
        Map<String,Integer> ingredients = new HashMap<>();
        ingredients.put(EsenciaUltraente.ID, 3);
        ingredients.put("minecraft:yellow_dye", 16);
        ingredients.put(NucleoDeSacrificio.ID, 1);

        Supplier<ItemStack> result = () ->
                CustomItemRegistry.create(ModificadorShiny.ID, 1);

        AltarRecipe recipe = new AltarRecipe(
                new ResourceLocation("rangerlevels", "invocacion_modificador_shiny_u"),
                ingredients,
                result,
                Tier.LEGENDARIO.applyGradient("✦ Activaste el Altar de Almas y materializó el Modificador Shiny ✦"),
                new StringTextComponent("§cNo cumples los requisitos para invocar el Modificador Shiny.")
        );
        InvocationCraftHelper.registerRecipe(AltarAlmas.ID, recipe);
    }
    private static void registerIVsL() {
        Map<String,Integer> ingredients = new HashMap<>();
        ingredients.put(EsenciaLegendaria.ID, 4);
        ingredients.put("minecraft:nether_star", 1);
        ingredients.put(NucleoDeSacrificio.ID, 1);

        Supplier<ItemStack> result = () ->
                CustomItemRegistry.create(ModificadorIVs.ID, 1);

        AltarRecipe recipe = new AltarRecipe(
                new ResourceLocation("rangerlevels", "invocacion_modificador_ivs_l"),
                ingredients,
                result,
                Tier.LEGENDARIO.applyGradient("✦ Activaste el Altar de Almas y materializó el Modificador de IVS ✦"),
                new StringTextComponent("§cNo cumples los requisitos para invocar el Modificador de IVS.")
        );
        InvocationCraftHelper.registerRecipe(AltarAlmas.ID, recipe);
    }
    private static void registerIVsLUniversal() {
        Map<String,Integer> ingredients = new HashMap<>();
        ingredients.put(EsenciaLegendaria.ID, 8);
        ingredients.put("minecraft:nether_star", 1);
        ingredients.put(NucleoDeSacrificio.ID, 1);

        Supplier<ItemStack> result = () ->
                CustomItemRegistry.create(ModificadorIVsUniversal.ID, 1);

        AltarRecipe recipe = new AltarRecipe(
                new ResourceLocation("rangerlevels", "invocacion_modificador_ivs_universal_l"),
                ingredients,
                result,
                Tier.LEGENDARIO.applyGradient("✦ Activaste el Altar de Almas y materializó el Modificador de IVS Universal ✦"),
                new StringTextComponent("§cNo cumples los requisitos para invocar el Modificador de IVS Universal.")
        );
        InvocationCraftHelper.registerRecipe(AltarAlmas.ID, recipe);
    }
    private static void registerIVsUUniversal() {
        Map<String,Integer> ingredients = new HashMap<>();
        ingredients.put(EsenciaUltraente.ID, 8);
        ingredients.put("minecraft:nether_star", 1);
        ingredients.put(NucleoDeSacrificio.ID, 1);

        Supplier<ItemStack> result = () ->
                CustomItemRegistry.create(ModificadorIVsUniversal.ID, 1);

        AltarRecipe recipe = new AltarRecipe(
                new ResourceLocation("rangerlevels", "invocacion_modificador_ivs_universal_u"),
                ingredients,
                result,
                Tier.LEGENDARIO.applyGradient("✦ Activaste el Altar de Almas y materializó el Modificador de IVS Universal ✦"),
                new StringTextComponent("§cNo cumples los requisitos para invocar el Modificador de IVS Universal.")
        );
        InvocationCraftHelper.registerRecipe(AltarAlmas.ID, recipe);
    }
    private static void registerIVsU() {
        Map<String,Integer> ingredients = new HashMap<>();
        ingredients.put(EsenciaUltraente.ID, 4);
        ingredients.put("minecraft:nether_star", 1);
        ingredients.put(NucleoDeSacrificio.ID, 1);

        Supplier<ItemStack> result = () ->
                CustomItemRegistry.create(ModificadorIVs.ID, 1);

        AltarRecipe recipe = new AltarRecipe(
                new ResourceLocation("rangerlevels", "invocacion_modificador_ivs_u"),
                ingredients,
                result,
                Tier.LEGENDARIO.applyGradient("✦ Activaste el Altar de Almas y materializó el Modificador IVS ✦"),
                new StringTextComponent("§cNo cumples los requisitos para invocar el Modificador de IVS.")
        );
        InvocationCraftHelper.registerRecipe(AltarAlmas.ID, recipe);
    }
    private static void registerConcentradoL() {
        Map<String,Integer> ingredients = new HashMap<>();
        ingredients.put(EsenciaLegendaria.ID, 5);
        ingredients.put("minecraft:nether_star", 1);
        ingredients.put(NucleoDeSacrificio.ID, 2);

        Supplier<ItemStack> result = () ->
                CustomItemRegistry.create(ConcentradoDeAlmas.ID, 1);

        AltarRecipe recipe = new AltarRecipe(
                new ResourceLocation("rangerlevels", "invocacion_concentrado_almas_l"),
                ingredients,
                result,
                Tier.LEGENDARIO.applyGradient("✦ Activaste el Altar de Almas y materializó el Concentrado de Almas ✦"),
                new StringTextComponent("§cNo cumples los requisitos para invocar el Concentrado de Almas.")
        );
        InvocationCraftHelper.registerRecipe(AltarAlmas.ID, recipe);
    }
    private static void registerConcentradoU() {
        Map<String,Integer> ingredients = new HashMap<>();
        ingredients.put(EsenciaUltraente.ID, 5);
        ingredients.put("minecraft:nether_star", 1);
        ingredients.put(NucleoDeSacrificio.ID, 2);

        Supplier<ItemStack> result = () ->
                CustomItemRegistry.create(ConcentradoDeAlmas.ID, 1);

        AltarRecipe recipe = new AltarRecipe(
                new ResourceLocation("rangerlevels", "invocacion_concentrado_almas_u"),
                ingredients,
                result,
                Tier.LEGENDARIO.applyGradient("✦ Activaste el Altar de Almas y materializó el Concentrado de Almas ✦"),
                new StringTextComponent("§cNo cumples los requisitos para invocar el Concentrado de Almas.")
        );
        InvocationCraftHelper.registerRecipe(AltarAlmas.ID, recipe);
    }
    private static void registerNucleoL() {
        Map<String,Integer> ingredients = new HashMap<>();
        ingredients.put(EsenciaLegendaria.ID, 1);
        ingredients.put("pixelmon:boulder", 1);

        Supplier<ItemStack> result = () ->
                CustomItemRegistry.create(NucleoDeSacrificio.ID, 1);

        AltarRecipe recipe = new AltarRecipe(
                new ResourceLocation("rangerlevels", "invocacion_nucleo_l"),
                ingredients,
                result,
                Tier.LEGENDARIO.applyGradient("✦ Activaste el Altar de Almas y materializó el Núcleo de Sacrificio ✦"),
                new StringTextComponent("§cNo cumples los requisitos para invocar el Núcleo de Sacrificio.")
        );
        InvocationCraftHelper.registerRecipe(AltarAlmas.ID, recipe);
    }
    private static void registerNucleoU() {
        Map<String,Integer> ingredients = new HashMap<>();
        ingredients.put(EsenciaUltraente.ID, 1);
        ingredients.put("pixelmon:boulder", 1);

        Supplier<ItemStack> result = () ->
                CustomItemRegistry.create(NucleoDeSacrificio.ID, 1);

        AltarRecipe recipe = new AltarRecipe(
                new ResourceLocation("rangerlevels", "invocacion_nucleo_u"),
                ingredients,
                result,
                Tier.LEGENDARIO.applyGradient("✦ Activaste el Altar de Almas y materializó el Núcleo de Sacrificio ✦"),
                new StringTextComponent("§cNo cumples los requisitos para invocar el Núcleo de Sacrificio.")
        );
        InvocationCraftHelper.registerRecipe(AltarAlmas.ID, recipe);
    }
}
