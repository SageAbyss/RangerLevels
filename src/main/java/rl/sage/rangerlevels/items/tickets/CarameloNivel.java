// File: rl/sage/rangerlevels/items/tickets/CarameloNivel.java
package rl.sage.rangerlevels.items.tickets;

import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.Tier;
import rl.sage.rangerlevels.util.NBTUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Definition del “Caramelo de Nivel”.
 * Al hacer clic derecho, otorga +1 nivel (si no está en nivel máximo).
 * El nombre y la línea de Tier usan degradado pastel de Tier.RARO.
 */
public class CarameloNivel extends RangerItemDefinition {
    public static final String ID = "caramelo_nivel";

    public CarameloNivel() {
        super(
                ID,
                PixelmonItems.rare_candy,
                Tier.RARO,     // Tier RARO
                null,          // Ya no pasamos TextFormatting, sobreescribimos en createStack()
                "✹ Caramelo de Nivel ✹",
                null           // Lore por defecto se asigna en createStack()
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        // 1) Creamos el ItemStack base
        ItemStack stack = super.createStack(amount);

        // 2) Asignamos el hover-name con degradado pastel de Tier.RARO
        stack.setHoverName(Tier.RARO.applyGradient(getDisplayName()));

        // 3) Generamos el lore con la línea de Tier en degradado
        List<IFormattableTextComponent> generatedLore = Arrays.asList(
                // Viñeta “✧” + descripción genérica
                new StringTextComponent("§7✧ Come este Caramelo para subir +1 nivel."),
                // Viñeta “✧” + texto de consumo
                new StringTextComponent("§7✧ Se consume al usarlo."),
                // “▶ Tier:” en gris + “RARO” en degradado pastel
                new StringTextComponent("§7▶ Tier: ").append(Tier.RARO.getColor())
        );

        // 4) Insertamos el lore en NBT
        CompoundNBT tag = stack.getOrCreateTag();
        CompoundNBT display = tag.contains("display")
                ? tag.getCompound("display")
                : new CompoundNBT();
        net.minecraft.nbt.ListNBT loreList = new net.minecraft.nbt.ListNBT();
        for (IFormattableTextComponent line : generatedLore) {
            String json = IFormattableTextComponent.Serializer.toJson(line);
            loreList.add(net.minecraft.nbt.StringNBT.valueOf(json));
        }
        display.put("Lore", loreList);
        tag.put("display", display);

        // 5) Ocultamos atributos innecesarios (HideFlags bit 32)
        NBTUtils.applyAllHideFlags(tag);

        stack.setTag(tag);
        return stack;
    }
}
