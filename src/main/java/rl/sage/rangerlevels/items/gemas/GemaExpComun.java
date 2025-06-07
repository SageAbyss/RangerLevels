// File: rl/sage/rangerlevels/items/gemas/GemaExpComun.java
package rl.sage.rangerlevels.items.gemas;

import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
 * Gema de Experiencia COMÚN:
 *  - +10% EXP post-captura/derrota durante 15 minutos (900_000 ms)
 *  - Nombre y línea de Tier en degradado pastel de Tier.COMUN
 */
public class GemaExpComun extends RangerItemDefinition {
    public static final String ID = "gema_exp_comun";

    public GemaExpComun() {
        super(
                ID,
                PixelmonItems.jade_orb,    // Ítem base: esmeralda
                Tier.COMUN,       // Tier COMÚN
                null,             // Ya no pasamos TextFormatting
                "✦ Gema de Dominio ✦",
                null              // Lore se asigna en createStack()
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        // 1) Creamos el ItemStack base
        ItemStack stack = super.createStack(amount);

        // 2) Asignamos el hover-name con degradado pastel de Tier.COMUN
        stack.setHoverName(Tier.COMUN.applyGradient(getDisplayName()));

        // 3) Generamos el lore con la línea de Tier en degradado
        List<IFormattableTextComponent> generatedLore = Arrays.asList(
                // 3.1) Viñeta “✧” + descripción
                new StringTextComponent("§7✧ Otorga +10% de EXP al capturar/derrotar Pokémon"),
                // 3.2) Viñeta “✧” + duración
                new StringTextComponent("§7✧ Dura 15 minutos"),
                // 3.3) “▶ Tier:” en gris + “COMÚN” en degradado pastel
                new StringTextComponent("§7▶ Tier: ").append(Tier.COMUN.getColor())
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

        NBTUtils.applyAllHideFlags(tag);

        stack.setTag(tag);
        return stack;
    }
}
