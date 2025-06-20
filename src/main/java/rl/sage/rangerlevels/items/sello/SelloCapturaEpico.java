// File: rl/sage/rangerlevels/items/SelloCapturaEpico.java
package rl.sage.rangerlevels.items.sello;

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

import java.util.ArrayList;
import java.util.List;

/**
 * Sello de Captura (Épico):
 * - Tier ÉPICO
 * - Garantiza al 100 % la siguiente captura.
 * - Se consume al activarse.
 * - Muestra lore y nombre decorado.
 */
public class SelloCapturaEpico extends RangerItemDefinition {
    public static final String ID = "sello_captura_epico";

    public SelloCapturaEpico() {
        super(
                ID,
                PixelmonItems.legendary_clues,           // ítem vanilla base (ajusta al que prefieras)
                Tier.EPICO,
                TextFormatting.LIGHT_PURPLE,
                "✦ Sello de Captura ✦",
                null
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(amount);

        // Nombre con degradado del tier
        IFormattableTextComponent title = Tier.EPICO.applyGradient(getDisplayName());
        stack.setHoverName(title);

        // Lore dinámico
        List<IFormattableTextComponent> lore = new ArrayList<>();
        lore.add(new StringTextComponent(TextFormatting.GRAY + "✧ Garantiza al 100% tu siguiente captura"));
        lore.add(new StringTextComponent(TextFormatting.GRAY + "✧ Aplica para Cualquier Pokémon"));
        lore.add(new StringTextComponent(TextFormatting.GRAY + "✧ Tenlo en el Inventario para usarlo"));
        lore.add(new StringTextComponent(" "));
        lore.add(
                new StringTextComponent("§7▶ Tier: ")
                        .append(Tier.EPICO.getColor())
        );

        // Insertar lore en NBT
        CompoundNBT tag = stack.getOrCreateTag();
        CompoundNBT display = tag.contains("display") ? tag.getCompound("display") : new CompoundNBT();
        net.minecraft.nbt.ListNBT loreList = new net.minecraft.nbt.ListNBT();
        for (IFormattableTextComponent line : lore) {
            loreList.add(net.minecraft.nbt.StringNBT.valueOf(IFormattableTextComponent.Serializer.toJson(line)));
        }
        display.put("Lore", loreList);
        tag.put("display", display);

        // Ocultar atributos y UUID único
        NBTUtils.applyAllHideFlags(tag);

        stack.setTag(tag);
        return stack;
    }
}
