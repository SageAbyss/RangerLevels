// File: rl/sage/rangerlevels/items/altar/AltarAlmas.java
package rl.sage.rangerlevels.items.altar;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.registries.ForgeRegistries;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.Tier;
import rl.sage.rangerlevels.util.EnchantUtils;
import rl.sage.rangerlevels.util.NBTUtils;
import rl.sage.rangerlevels.util.GradientText;

import java.util.ArrayList;
import java.util.List;

/**
 * Ítem: Altar de Almas. Se usa para invocar el Modificador de Naturaleza
 * a partir de una Esencia ligada a un Pokémon.
 */
public class AltarAlmas extends RangerItemDefinition {
    public static final String ID = "altar_almas";

    private static Item findIcon() {
        // Elige un ítem para representar el altar; aquí usamos Block de oro como icono de ejemplo
        Item i = ForgeRegistries.ITEMS.getValue(new ResourceLocation("pixelmon", "arc_chalice"));
        return i != null ? i : Items.LECTERN;
    }

    public AltarAlmas() {
        super(
                ID,
                findIcon(),
                Tier.LEGENDARIO,
                null,
                "✦ Altar de Almas ✦",
                null
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(amount);

        // Nombre con degradado o color fijo:
        stack.setHoverName(
                GradientText.of(getDisplayName(), "#800000", "#FF0000")
                        .withStyle(s -> s.withItalic(false).withBold(true))
        );

        // Lore descriptivo:
        List<IFormattableTextComponent> lore = new ArrayList<>();
        lore.add(new StringTextComponent("§7✧ Maneja las almas de Pokémon"));
        lore.add(new StringTextComponent("§7   consumiendo una Esencia ligada."));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7✧ Requisitos para usar:"));
        lore.add(new StringTextComponent("§7▶ Estructura: 3×3 Ladrillos Rojos del Nether"));
        lore.add(new StringTextComponent("§7   con Glowstone en el centro y"));
        lore.add(new StringTextComponent("§7   4 Muros de Ladrillos Rojos del Nether."));
        lore.add(new StringTextComponent("§7   sobre las esquinas."));
        lore.add(new StringTextComponent(" "));
        lore.add(new StringTextComponent("§7▶ Coloca esto en la mano y haz click"));
        lore.add(new StringTextComponent("§7   sobre el bloque central de la estructura."));
        CompoundNBT tag = stack.getOrCreateTag();
        CompoundNBT display = tag.contains("display") ? tag.getCompound("display") : new CompoundNBT();
        ListNBT loreList = new ListNBT();
        for (IFormattableTextComponent line : lore) {
            loreList.add(StringNBT.valueOf(IFormattableTextComponent.Serializer.toJson(line)));
        }
        display.put("Lore", loreList);
        tag.put("display", display);
        EnchantUtils.addEnchantment(stack, Enchantments.UNBREAKING, 1);
        NBTUtils.applyAllHideFlags(tag);
        stack.setTag(tag);
        return stack;
    }
}
