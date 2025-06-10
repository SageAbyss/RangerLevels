// File: rl/sage/rangerlevels/items/manuales/ManualEntrenamientoLegendario.java
package rl.sage.rangerlevels.items.manuales;

import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
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
import rl.sage.rangerlevels.util.NBTUtils;

import java.util.Arrays;
import java.util.List;

public class ManualEntrenamientoLegendario extends RangerItemDefinition {
    public static final String ID = "manual_entrenamiento_legendario";

    public ManualEntrenamientoLegendario() {
        super(
                ID,
                PixelmonItems.sonias_book,
                Tier.LEGENDARIO,
                null,
                "✦ Manual de Entrenamiento Intensivo ✦",
                null
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(amount);

        stack.setHoverName(Tier.LEGENDARIO.applyGradient(getDisplayName()));

        List<IFormattableTextComponent> lore = Arrays.asList(
                new StringTextComponent("§7❖ Aplica un bonus de experiencia en combates"),
                new StringTextComponent("§7❖ Contra Pokémon, NPCs o Jugadores"),
                new StringTextComponent("§7✧ Debe estar en el Inventario"),
                new StringTextComponent("§7✧ Bonus de EXP: §6+20%"),
                new StringTextComponent(" "),
                new StringTextComponent("§7▶ Tier: ").append(Tier.LEGENDARIO.getColor())
        );

        CompoundNBT tag = stack.getOrCreateTag();
        CompoundNBT display = tag.contains("display") ? tag.getCompound("display") : new CompoundNBT();
        ListNBT loreList = new ListNBT();
        for (IFormattableTextComponent line : lore) {
            loreList.add(StringNBT.valueOf(IFormattableTextComponent.Serializer.toJson(line)));
        }
        display.put("Lore", loreList);
        tag.put("display", display);

        NBTUtils.applyAllHideFlags(tag);

        stack.setTag(tag);
        return stack;
    }
}
