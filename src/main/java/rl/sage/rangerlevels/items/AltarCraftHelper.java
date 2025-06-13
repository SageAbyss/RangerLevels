package rl.sage.rangerlevels.items;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class AltarCraftHelper {

    private static final Map<ResourceLocation, AltarRecipe> RECIPES = new java.util.HashMap<>();

    public static void registerRecipe(AltarRecipe recipe) {
        RECIPES.put(recipe.getId(), recipe);
    }

    public static AltarRecipe getRecipe(ResourceLocation id) {
        return RECIPES.get(id);
    }

    /**
     * Método general para activar un altar identificado por recipeId.
     * Si cumple receta: consume ingredientes + altar, da resultado y envía mensaje épico.
     * Si no cumple: no consume ingredientes ni altar, envía mensaje detallando faltantes.
     *
     * @param player     jugador
     * @param altarStack ItemStack del altar en la mano
     * @param recipeId   ResourceLocation de la receta (p.ej. "rangerlevels:totem_raiz_primordial")
     * @param pos        posición donde “coloca” el altar (puede usarse para sonido/partículas)
     */
    public static void handleActivation(ServerPlayerEntity player, ItemStack altarStack,
                                        ResourceLocation recipeId, BlockPos pos) {
        AltarRecipe recipe = RECIPES.get(recipeId);
        if (recipe == null) return;
        if (!(player.level instanceof ServerWorld)) return;
        ServerWorld world = (ServerWorld) player.level;

        // Solo en servidor:
        if (recipe.matches(player)) {
            // consume ingredientes
            recipe.consumeIngredients(player);
            // consume el altar mismo
            altarStack.shrink(1);

            // dar resultado
            ItemStack result = recipe.getResultSupplier().get();
            if (!player.inventory.add(result)) {
                player.drop(result, false);
            }

            // Mensaje de éxito
            IFormattableTextComponent msg = recipe.getSuccessMessage();
            player.sendMessage(msg, player.getUUID());

            // Sonido / efectos
            world.playSound(null, pos, SoundEvents.PORTAL_TRIGGER,
                    SoundCategory.BLOCKS, 1.0f, 1.2f);
            // Aquí podrías también spawn partículas o similar.
        } else {
            // calcula faltantes
            Map<String, Integer> faltantes = recipe.getMissingIngredients(player);
            // Construir mensaje dinámico
            StringTextComponent comp = new StringTextComponent("§c❌ Ingredientes faltantes para invocar:");
            for (Map.Entry<String, Integer> entry : faltantes.entrySet()) {
                String key = entry.getKey();
                int qty = entry.getValue();
                String nombre;
                // Intentar nombre legible:
                // 1) Si es RangerItemDefinition registrado:
                if (CustomItemRegistry.contains(key)) {
                    ItemStack tmp = CustomItemRegistry.create(key, 1);
                    nombre = tmp.getHoverName().getString();
                } else {
                    // 2) intentar obtener Item por ResourceLocation
                    Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(key));
                    if (item != null) {
                        ItemStack tmp = new ItemStack(item);
                        nombre = tmp.getHoverName().getString();
                    } else {
                        nombre = key; // fallback al ID
                    }
                }
                comp.append("\n").append(new StringTextComponent("→ " + qty + "× " + nombre));
            }
            player.sendMessage(comp, player.getUUID());
            // No consumimos altarStack, se queda en mano
            // Opcional: reproducir sonido de error
            world.playSound(null, pos, SoundEvents.VILLAGER_NO,
                    SoundCategory.PLAYERS, 1.0f, 1.0f);
        }
    }
    public static Collection<AltarRecipe> getAllRecipes() {
        return Collections.unmodifiableCollection(RECIPES.values());
    }
}
