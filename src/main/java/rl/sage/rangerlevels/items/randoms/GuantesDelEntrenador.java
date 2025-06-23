// File: rl/sage/rangerlevels/items/amuletos/GuantesDelEntrenador.java
package rl.sage.rangerlevels.items.randoms;

import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.Tier;
import rl.sage.rangerlevels.util.EnchantUtils;
import rl.sage.rangerlevels.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import java.util.Arrays;
import java.util.List;

/**
 * Guantes del Entrenador: Tier Estelar.
 * Bonus 50% de exp contra salvajes de nivel 50 o mayor.
 */
public class GuantesDelEntrenador extends RangerItemDefinition {
    public static final String ID = "guantes_entrenador";

    public GuantesDelEntrenador() {
        super(
                ID,
                PixelmonItems.punching_glove, // Cambia al ítem que desees usar como representación
                Tier.ESTELAR,
                null,
                "✦ Guantes del Entrenador ✦",
                null
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(amount);
        stack.setHoverName(Tier.ESTELAR.applyGradient(getDisplayName()));

        List<IFormattableTextComponent> lore = Arrays.asList(
                new StringTextComponent("§7❖ Bonus de EXP: §b+50% §7contra Pokémon nivel 50+"),
                new StringTextComponent("§7❖ Debe estar en tu inventario"),
                new StringTextComponent(" "),
                new StringTextComponent("§7▶ Tier: ").append(Tier.ESTELAR.getColor())
        );

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