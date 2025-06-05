package rl.sage.rangerlevels.items.cetro;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.Tier;

import java.util.Arrays;
import java.util.List;

/**
 * Cetro Divino - Tier Común:
 *  - Restaura el 50% de HP de jugador + equipo Pokémon
 *  - Restaura el 50% de PP de cada Pokémon del equipo
 *  - Tiene 5 usos (se almacenan en NBT del ItemStack)
 */
public class CetroDivinoComun extends RangerItemDefinition {
    public static final String ID = "cetro_divino_comun";
    private static final int USOS_INICIALES = 5;
    private static final double RESTAURA_HP_PORC = 0.50;
    private static final double RESTAURA_PP_PORC = 0.50;
    // NBT key para usos restantes en el ItemStack
    private static final String NBT_USOS_KEY = "CetroDivinoUsos";

    public CetroDivinoComun() {
        super(
                ID,
                Items.BLAZE_ROD,                 // Reutilizamos un palo como base (puedes cambiarlo)
                Tier.COMUN,
                null,                        // Color sólido ya no hace falta; usamos degradado en createStack()
                "✦ Cetro Divino ✦",
                null                         // Lore se añadirá en createStack()
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(amount);

        // 1) Asignar hover-name con degradado del tier COMÚN
        stack.setHoverName(Tier.COMUN.applyGradient(getDisplayName()));

        // 2) Añadir NBT con los usos iniciales (si no lo tiene ya)
        CompoundNBT tag = stack.getOrCreateTag();
        if (!tag.contains(NBT_USOS_KEY)) {
            tag.putInt(NBT_USOS_KEY, USOS_INICIALES);
        }

        // 3) Generar lore dinámico (número de usos restantes)
        int usosRestantes = tag.getInt(NBT_USOS_KEY);
        List<IFormattableTextComponent> lore = Arrays.asList(
                new StringTextComponent("§7✧ Restaura el 50% de tu HP y del equipo Pokémon"),
                new StringTextComponent("§7✧ Restaura 50% de PP de cada Pokémon"),
                new StringTextComponent("§7✧ Usos restantes: §e" + usosRestantes),
                new StringTextComponent("§7▶ Tier: ").append(Tier.COMUN.getColor())
        );

        CompoundNBT displayTag = tag.contains("display") ? tag.getCompound("display") : new CompoundNBT();
        ListNBT loreList = new ListNBT();
        for (IFormattableTextComponent line : lore) {
            String json = IFormattableTextComponent.Serializer.toJson(line);
            loreList.add(StringNBT.valueOf(json));
        }
        displayTag.put("Lore", loreList);
        tag.put("display", displayTag);

        // 4) Guardar tag y regresar el stack
        stack.setTag(tag);
        return stack;
    }

    /** Obtiene cuántos usos le quedan al ItemStack; */
    public static int getUsosRestantes(ItemStack stack) {
        if (stack == null || !stack.hasTag()) return 0;
        CompoundNBT tag = stack.getTag();
        if (!tag.contains(NBT_USOS_KEY)) return 0;
        return tag.getInt(NBT_USOS_KEY);
    }

    /** Resta un uso (si quedan) y actualiza NBT. */
    public static void decrementarUso(ItemStack stack) {
        if (stack == null || !stack.hasTag()) return;
        CompoundNBT tag = stack.getTag();
        if (!tag.contains(NBT_USOS_KEY)) return;

        int usos = tag.getInt(NBT_USOS_KEY);
        if (usos <= 0) return;

        usos--;
        tag.putInt(NBT_USOS_KEY, usos);

        // Actualizar lore para que muestre usos restantes
        CompoundNBT displayTag = tag.getCompound("display");
        ListNBT loreList = new ListNBT();
        // Reconstruimos el lore con el nuevo número de usos
        List<IFormattableTextComponent> nuevaLore = Arrays.asList(
                new StringTextComponent("§7✧ Restaura el 50% de tu HP y del equipo Pokémon"),
                new StringTextComponent("§7✧ Restaura 50% de PP de cada Pokémon"),
                new StringTextComponent("§7✧ Usos restantes: §e" + usos),
                new StringTextComponent("§7▶ Tier: ").append(Tier.COMUN.getColor())
        );
        for (IFormattableTextComponent line : nuevaLore) {
            String json = IFormattableTextComponent.Serializer.toJson(line);
            loreList.add(StringNBT.valueOf(json));
        }
        displayTag.put("Lore", loreList);
        tag.put("display", displayTag);

        stack.setTag(tag);
    }

    public static double getHpRestorePorc() {
        return RESTAURA_HP_PORC;
    }

    public static double getPpRestorePorc() {
        return RESTAURA_PP_PORC;
    }
}
