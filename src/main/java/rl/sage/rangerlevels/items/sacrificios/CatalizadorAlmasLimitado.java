package rl.sage.rangerlevels.items.sacrificios;

import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.Tier;
import rl.sage.rangerlevels.util.EnchantUtils;
import rl.sage.rangerlevels.util.NBTUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Catalizador de Almas (Limitado) - Tier Mítico (Singular)
 * - Versión con X usos (p. ej. 30)
 * - Cada sacrificar decrementa usos en NBT, actualiza lore, y al llegar a 0 se consume.
 */
public class CatalizadorAlmasLimitado extends RangerItemDefinition {
    public static final String ID = "catalizador_almas_limitado";
    private static final int USOS_INICIALES = 20;
    private static final String NBT_USOS_KEY = "CatalizadorAlmasUsos";

    public CatalizadorAlmasLimitado() {
        super(
                ID,
                PixelmonItems.professors_mask,
                Tier.ESTELAR,
                TextFormatting.DARK_PURPLE,
                "✦ Catalizador de Almas (Limitado) ✦",
                null // el lore lo gestionamos en createStack y en decrementarUso
        );
        // Asegúrate de que en las propiedades del item se fije stack tamaño 1, si RangerItemDefinition lo permite.
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(1);
        EnchantUtils.addEnchantment(stack, net.minecraft.enchantment.Enchantments.UNBREAKING, 1);

        CompoundNBT tag = stack.getOrCreateTag();
        // Si no contiene la clave de usos, inicializamos
        if (!tag.contains(NBT_USOS_KEY)) {
            tag.putInt(NBT_USOS_KEY, USOS_INICIALES);
        }
        // Actualizar lore según usos actuales
        actualizarLore(stack, tag.getInt(NBT_USOS_KEY));

        // Oculta atributos extra
        NBTUtils.applyAllHideFlags(tag);
        stack.setTag(tag);
        return stack;
    }

    /** Obtiene usos restantes del ItemStack (0 si no existe NBT) */
    public static int getUsosRestantes(ItemStack stack) {
        if (stack == null || !stack.hasTag()) return 0;
        CompoundNBT tag = stack.getTag();
        if (!tag.contains(NBT_USOS_KEY)) return 0;
        return tag.getInt(NBT_USOS_KEY);
    }

    /** Decrementa un uso en el NBT, actualiza lore. No hace shrink; el handler decide si eliminar.*/
    public static void decrementarUso(ItemStack stack) {
        if (stack == null || !stack.hasTag()) return;
        CompoundNBT tag = stack.getTag();
        if (!tag.contains(NBT_USOS_KEY)) return;

        int usos = tag.getInt(NBT_USOS_KEY);
        if (usos <= 0) return;

        usos--;
        tag.putInt(NBT_USOS_KEY, usos);

        actualizarLore(stack, usos);

        stack.setTag(tag);
    }

    /** Reconstruye el lore en NBT “display.Lore” según el número de usos */
    private static void actualizarLore(ItemStack stack, int usosRestantes) {
        CompoundNBT tag = stack.getOrCreateTag();
        // Construir la lista de líneas de lore
        List<IFormattableTextComponent> lore = Arrays.asList(
                new StringTextComponent("§7✧ Un artefacto forjado con la esencia ancestral."),
                new StringTextComponent("§7✧ Haz clic derecho sobre un Pokémon Legendario"),
                new StringTextComponent("§7   o Ultraente de tu equipo para sacrificarlo."),
                new StringTextComponent(" "),
                new StringTextComponent("§7✧ El sacrificio extrae la Esencia específica"),
                new StringTextComponent("§7   que luego sirve para crear modificadores"),
                new StringTextComponent("§7   de ADN únicos para ese Pokémon."),
                new StringTextComponent(" "),
                new StringTextComponent("§7✧ También extrae la Esencia de Jefes,"),
                new StringTextComponent("§7   las cuales sirven para usarlas en el Altar"),
                new StringTextComponent("§7   para intercambiarlas por Esencias."),
                new StringTextComponent(" "),
                new StringTextComponent("§7✧ Usos restantes: §e" + usosRestantes),
                new StringTextComponent(" "),
                new StringTextComponent("§7▶ Tier: ").append(Tier.ESTELAR.getColor())
        );
        // Acceder/crear la sección display
        CompoundNBT displayTag = tag.contains("display") ? tag.getCompound("display") : new CompoundNBT();
        ListNBT loreList = new ListNBT();
        for (IFormattableTextComponent line : lore) {
            String json = IFormattableTextComponent.Serializer.toJson(line);
            loreList.add(StringNBT.valueOf(json));
        }
        displayTag.put("Lore", loreList);
        tag.put("display", displayTag);
    }
}
