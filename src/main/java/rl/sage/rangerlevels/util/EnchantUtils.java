package rl.sage.rangerlevels.util;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import java.util.Map;

/**
 * Utilidad estática para agregar encantamientos a un ItemStack,
 * creando o modificando la tag "Enchantments" en el NBT.
 *
 * Incluye dos métodos:
 * 1) addEnchantment(ItemStack stack, Enchantment ench, int level)
 * 2) addEnchantment(ItemStack stack, String enchantmentId, int level)
 *
 * Estos métodos NO tocan ningún HideFlags. Si quieres ocultar
 * el encantamiento, usa tu otra clase util para aplicar HideFlags
 * después de aquí.
 */
public class EnchantUtils {

    private EnchantUtils() { /* clase estática, no instancias */ }

    /**
     * Agrega (o actualiza) un encantamiento a un ItemStack.
     * Si ya existe ese encantamiento en el stack, sobrescribe el nivel
     * por el valor que indiques.
     *
     * @param stack        El ItemStack al que quieres poner el encantamiento
     * @param enchantment  La instancia de Enchantment (p. ej. Enchantments.SHARPNESS)
     * @param level        Nivel deseado (>= 1). Si pasas 0 o negativo, no hace nada.
     */
    public static void addEnchantment(ItemStack stack, Enchantment enchantment, int level) {
        if (stack == null || enchantment == null || level < 1) return;

        // 1) Obtener la tag NBT actual (o crearla si no existe)
        CompoundNBT tag = stack.getOrCreateTag();

        // 2) Obtener (o crear) la lista "Enchantments" dentro del tag
        ListNBT enchList = tag.contains("Enchantments")
                ? tag.getList("Enchantments", 10) // tipo 10 = CompoundNBT
                : new ListNBT();

        // 3) Generar el CompoundNBT para este encantamiento
        //    - id: string del ResourceLocation (p.ej. "minecraft:unbreaking")
        //    - lvl: short con el nivel
        ResourceLocation regName = enchantment.getRegistryName();
        if (regName == null) return; // rarísimo, pero por seguridad
        String idString = regName.toString();

        // 4) Buscar si ya existe un entry con el mismo id
        boolean reemplazado = false;
        for (int i = 0; i < enchList.size(); i++) {
            CompoundNBT existing = enchList.getCompound(i);
            if (existing.contains("id", 8) && idString.equals(existing.getString("id"))) {
                // Reemplazamos el nivel:
                existing.putShort("lvl", (short) level);
                reemplazado = true;
                break;
            }
        }

        // 5) Si no existía, añadimos un nuevo CompoundNBT al final
        if (!reemplazado) {
            CompoundNBT newEntry = new CompoundNBT();
            newEntry.putString("id", idString);
            newEntry.putShort("lvl", (short) level);
            enchList.add(newEntry);
        }

        // 6) Guardar la lista modificada en el tag y volver a aplicarla al stack
        tag.put("Enchantments", enchList);
        stack.setTag(tag);
    }

    /**
     * Agrega (o actualiza) un encantamiento dado su ID en texto
     * (p. ej. "minecraft:sharpness" o "unbreaking" si te fijas en el registry).
     * Internamente busca el Enchantment en el registro; si no existe, no hace nada.
     *
     * @param stack          El ItemStack al que quieres poner el encantamiento
     * @param enchantmentId  El string del ResourceLocation (p. ej. "minecraft:efficiency")
     * @param level          Nivel deseado (>= 1). Si pasas 0 o negativo, no hace nada.
     */
    public static void addEnchantment(ItemStack stack, String enchantmentId, int level) {
        if (stack == null || enchantmentId == null || enchantmentId.isEmpty() || level < 1) return;

        ResourceLocation rl = ResourceLocation.tryParse(enchantmentId);
        if (rl == null) return;

        Enchantment ench = Registry.ENCHANTMENT.getOptional(rl).orElse(null);
        if (ench == null) return;

        addEnchantment(stack, ench, level);
    }

    /**
     * Retorna un mapa de todos los encantamientos que ya tiene el ItemStack
     * (solo útiles si quieres inspeccionar qué niveles hay antes de agregar).
     */
    public static Map<Enchantment, Integer> getEnchantments(ItemStack stack) {
        return EnchantmentHelper.getEnchantments(stack);
    }
}
