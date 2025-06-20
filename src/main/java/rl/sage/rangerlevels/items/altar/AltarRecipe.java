package rl.sage.rangerlevels.items.altar;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraftforge.registries.ForgeRegistries;
import rl.sage.rangerlevels.items.RangerItemDefinition;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AltarRecipe {
    private final ResourceLocation id;
    private final Map<String, Integer> ingredients; // RangerID o ResourceLocation string -> cantidad
    private final Supplier<ItemStack> resultSupplier;
    private final IFormattableTextComponent successMessage;
    private final IFormattableTextComponent failureMessage;

    public AltarRecipe(ResourceLocation id,
                       Map<String, Integer> ingredients,
                       Supplier<ItemStack> resultSupplier,
                       IFormattableTextComponent successMessage,
                       IFormattableTextComponent failureMessage) {
        this.id = id;
        // Creamos copia para no modificar el original
        this.ingredients = new HashMap<>(ingredients);
        this.resultSupplier = resultSupplier;
        this.successMessage = successMessage;
        this.failureMessage = failureMessage;
    }

    public ResourceLocation getId() { return id; }
    public Supplier<ItemStack> getResultSupplier() { return resultSupplier; }
    public IFormattableTextComponent getSuccessMessage() { return successMessage; }
    public IFormattableTextComponent getFailureMessage() { return failureMessage; }

    /** Comprueba si el jugador tiene todos los ingredientes en inventario/offhand */
    public boolean matches(ServerPlayerEntity player) {
        Map<String, Integer> remaining = new HashMap<>(ingredients);
        contarEnInventario(player, remaining);
        return remaining.values().stream().allMatch(c -> c <= 0);
    }

    /** Devuelve un mapa con la cantidad faltante (>0) de cada ingrediente. */
    public Map<String, Integer> getMissingIngredients(ServerPlayerEntity player) {
        Map<String, Integer> remaining = new HashMap<>(ingredients);
        contarEnInventario(player, remaining);
        // Filtrar solo los que queden > 0
        Map<String, Integer> faltantes = new HashMap<>();
        for (Map.Entry<String, Integer> entry : remaining.entrySet()) {
            int qty = entry.getValue();
            if (qty > 0) {
                faltantes.put(entry.getKey(), qty);
            }
        }
        return faltantes;
    }

    private void contarEnInventario(ServerPlayerEntity player, Map<String, Integer> remaining) {
        // Iterar inventario principal
        for (ItemStack stack : player.inventory.items) {
            restarStack(stack, remaining);
        }
        // Iterar offhand
        for (ItemStack stack : player.inventory.offhand) {
            restarStack(stack, remaining);
        }
    }

    private void restarStack(ItemStack stack, Map<String, Integer> remaining) {
        if (stack == null || stack.isEmpty()) return;
        String key = obtenerKeyDeStack(stack);
        if (key == null) return;
        if (remaining.containsKey(key)) {
            int rem = remaining.get(key);
            rem -= stack.getCount();
            remaining.put(key, Math.max(rem, 0));
        }
    }

    /** Extrae “clave” del stack: si es un ítem Ranger personalizado, devuelve RangerID;
     *  si no, busca si es un vanilla/pixelmon por ResourceLocation.
     */
    private String obtenerKeyDeStack(ItemStack stack) {
        // Primero, si el stack tiene tag “RangerID”, asumir item personalizado
        if (stack.hasTag() && stack.getTag().contains(RangerItemDefinition.NBT_ID_KEY)) {
            String rid = stack.getTag().getString(RangerItemDefinition.NBT_ID_KEY);
            return rid;
        }
        // Si no, obtener el ResourceLocation del item en el registro
        Item item = stack.getItem();
        ResourceLocation rl = ForgeRegistries.ITEMS.getKey(item);
        if (rl != null) {
            return rl.toString(); // p.ej. "pixelmon:gold_bottle_cap" o "minecraft:iron_ingot"
        }
        return null;
    }

    /** Quita los ingredientes usados del inventario */
    public void consumeIngredients(ServerPlayerEntity player) {
        for (Map.Entry<String, Integer> entry : ingredients.entrySet()) {
            String key = entry.getKey();
            int qtyToConsume = entry.getValue();
            // Primero inventario principal
            qtyToConsume = consumirDeLista(player.inventory.items, key, qtyToConsume);
            // Luego offhand si hace falta
            if (qtyToConsume > 0) {
                consumirDeLista(player.inventory.offhand, key, qtyToConsume);
            }
        }
    }
    public void consumeSingleIngredient(ServerPlayerEntity player, String key, int amount) {
        // Similar a consumeIngredients pero para una sola clave y cantidad
        int remaining = amount;
        // Inventario principal
        remaining = consumirDeLista(player.inventory.items, key, remaining);
        // Offhand si queda
        if (remaining > 0) {
            consumirDeLista(player.inventory.offhand, key, remaining);
        }
    }

    private int consumirDeLista(java.util.List<ItemStack> lista, String key, int remaining) {
        for (int i = 0; i < lista.size() && remaining > 0; i++) {
            ItemStack stack = lista.get(i);
            if (stack == null || stack.isEmpty()) continue;
            String stackKey = obtenerKeyDeStack(stack);
            if (key.equals(stackKey)) {
                int take = Math.min(remaining, stack.getCount());
                stack.shrink(take);
                remaining -= take;
            }
        }
        return remaining;
    }
    public boolean hasIngredient(ServerPlayerEntity player, String key) {
        // Recorre inventario principal
        for (ItemStack stack : player.inventory.items) {
            if (stack != null && !stack.isEmpty() && key.equals(obtenerKeyDeStack(stack))) {
                return true;
            }
        }
        // Offhand
        for (ItemStack stack : player.inventory.offhand) {
            if (stack != null && !stack.isEmpty() && key.equals(obtenerKeyDeStack(stack))) {
                return true;
            }
        }
        return false;
    }


    public Map<String, Integer> getIngredients() {
        return ingredients;
    }
}
