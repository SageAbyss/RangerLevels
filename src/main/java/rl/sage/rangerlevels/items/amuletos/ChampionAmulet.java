package rl.sage.rangerlevels.items.amuletos;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import rl.sage.rangerlevels.config.ItemsConfig;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.items.Tier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Amuleto de Campeón:
 *   - Otorga un +X% bonus EXP (X viene de ItemsConfig.yml) al derrotar Pokémon/NPC salvajes.
 *   - Tiene una o varias entradas (comando + probabilidad) configuradas en ItemsConfig.yml.
 *
 * El lore muestra SOLO “+X% bonus EXP” y la probabilidad total de premio extra;
 * no se muestran los comandos individuales.
 */
public class ChampionAmulet extends RangerItemDefinition {
    public static final String ID = "amuleto_campeon";

    public ChampionAmulet() {
        super(
                ID,
                Items.NETHER_STAR,
                Tier.LEGENDARIO,
                null,
                "✦ Amuleto de Campeón ✦",
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

        // 2) Leemos configuración
        ItemsConfig.ChampionAmuletConfig amCfg = ItemsConfig.get().championAmulet;
        double xpPercent = amCfg.xpPercent;

        // Calculamos la probabilidad total sumando todas las entradas
        double totalChance = 0.0;
        if (amCfg.commands != null) {
            for (ItemsConfig.ChampionAmuletConfig.CommandEntry entry : amCfg.commands) {
                totalChance += entry.chancePercent;
            }
        }
        // Clampeamos a 100 si supera ese valor
        if (totalChance > 100.0) totalChance = 100.0;

        // 3) Generamos el lore dinámico
        List<IFormattableTextComponent> generatedLore = new ArrayList<>(Arrays.asList(
                new StringTextComponent(
                        TextFormatting.GRAY
                                + "✧ Otorga +" + ((int) xpPercent)
                                + "% bonus EXP al derrotar Pokémon y NPC salvajes"
                ),
                new StringTextComponent(
                        TextFormatting.GRAY
                                + "✧ "
                                + (totalChance % 1.0 == 0
                                ? String.format("%d%%", (int) totalChance)
                                : String.format("%.1f%%", totalChance))
                                + " probabilidad de premio extra al derrotar"
                )
        ));

        // Añadimos línea de tier
        IFormattableTextComponent tierPrefix = new StringTextComponent("§7▶ Tier: ");
        IFormattableTextComponent tierGradient = Tier.LEGENDARIO.getColor();
        // Concatenamos ambos:
        IFormattableTextComponent tierLine = tierPrefix.append(tierGradient);
        generatedLore.add(tierLine);

        // 4) Insertamos ese lore en display.Lore
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

        // 5) Ocultamos atributos del Nether Star (HideFlags bit 32)
        int hide = tag.contains("HideFlags") ? tag.getInt("HideFlags") : 0;
        hide |= 32;
        tag.putInt("HideFlags", hide);

        stack.setTag(tag);
        return stack;
    }
}
