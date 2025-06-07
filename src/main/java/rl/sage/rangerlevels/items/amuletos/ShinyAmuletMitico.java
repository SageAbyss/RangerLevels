// File: rl/sage/rangerlevels/items/ShinyAmuletMitico.java
package rl.sage.rangerlevels.items.amuletos;

import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import rl.sage.rangerlevels.config.MysteryBoxesConfig;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.Tier;
import rl.sage.rangerlevels.util.NBTUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Amuleto Shiny (Mítico):
 *   - Tier MÍTICO
 *   - 15 % de probabilidad de convertir en Shiny al capturar.
 *   - Se consume al activarse.
 *   - Lore y tag NBT preparados para mostrar 15 %.
 */
public class ShinyAmuletMitico extends RangerItemDefinition {
    public static final String ID = "amuleto_shiny_mitico";
    private static final double CHANCE_PERCENT = MysteryBoxesConfig.get().shinyAmulet.miticoPercent;

    public ShinyAmuletMitico() {
        super(
                ID,
                PixelmonItems.rainbow_flower,
                Tier.MITICO,         // Tier MÍTICO
                null,
                "✦ Amuleto Shiny Mítico ✦",
                null
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        // 1) Creamos el ItemStack base
        ItemStack stack = super.createStack(amount);
        IFormattableTextComponent gradientName = Tier.MITICO.applyGradient(getDisplayName());
        stack.setHoverName(gradientName);

        // 2) Generamos el lore dinámico (mostrando 15 %)
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
        IFormattableTextComponent tierPrefix = new StringTextComponent("§7▶ Tier: ");
        IFormattableTextComponent tierGradient = Tier.MITICO.getColor();
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
