// File: rl/sage/rangerlevels/items/ShinyAmuletLegendaria.java
package rl.sage.rangerlevels.items.amuletos;

import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import rl.sage.rangerlevels.config.ItemsConfig;
import rl.sage.rangerlevels.config.MysteryBoxesConfig;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.Tier;
import rl.sage.rangerlevels.util.NBTUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Amuleto Shiny (Legendaria):
 *   - Tier LEGENDARIO
 *   - 5 % de probabilidad de convertir en Shiny al capturar.
 *   - Se consume al activarse.
 *   - Lore y tag NBT preparados para mostrar 5 %.
 */
public class ShinyAmuletLegendaria extends RangerItemDefinition {
    public static final String ID = "amuleto_shiny_legendaria";
    private static final double CHANCE_PERCENT = ItemsConfig.get().shinyAmulet.legendariaPercent;

    public ShinyAmuletLegendaria() {
        super(
                ID,
                PixelmonItems.rainbow_flower,
                Tier.LEGENDARIO,   // Tier LEGENDARIO
                null,
                "✦ Amuleto Shiny Legendaria ✦",
                null
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        // 1) Creamos el ItemStack base
        ItemStack stack = super.createStack(amount);
        IFormattableTextComponent gradientName = Tier.LEGENDARIO.applyGradient(getDisplayName());
        stack.setHoverName(gradientName);

        // 2) Generamos el lore dinámico (mostrando 5 %)
        List<IFormattableTextComponent> generatedLore = new ArrayList<>();
        generatedLore.add(new StringTextComponent(
                TextFormatting.GRAY
                        + "✧ "
                        + String.format("%d%%", (int) CHANCE_PERCENT)
                        + " de probabilidad de capturar Shiny"
        ));
        generatedLore.add(new StringTextComponent(
                TextFormatting.GRAY
                        + "✧ Se consume al cumplir su función"
        ));
        generatedLore.add(new StringTextComponent(
                TextFormatting.GRAY
                        + " "
        ));
        IFormattableTextComponent tierPrefix = new StringTextComponent("§7▶ Tier: ");
        IFormattableTextComponent tierGradient = Tier.LEGENDARIO.getColor();
        // Concatenamos ambos:
        IFormattableTextComponent tierLine = tierPrefix.append(tierGradient);
        generatedLore.add(tierLine);

        // 3) Insertamos el lore en NBT
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

    /** Devuelve la probabilidad (en %) para el handler. */
    public static double getChancePercent() {
        return CHANCE_PERCENT;
    }
}
