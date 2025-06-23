// File: rl/sage/rangerlevels/items/randoms/DestinoVinculadoLegendario.java
package rl.sage.rangerlevels.items.destinos;

import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import net.minecraft.item.ItemStack;
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

public class DestinoVinculadoLegendario extends RangerItemDefinition {
    public static final String ID = "destino_vinculado_legendario";

    public DestinoVinculadoLegendario() {
        super(
                ID,
                PixelmonItems.escape_rope,
                Tier.LEGENDARIO,
                null,
                "✦ Destino Vinculado ✦",
                null
        );
        CustomItemRegistry.register(this);
    }
    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(1);
        stack.setHoverName(Tier.LEGENDARIO.applyGradient(getDisplayName()));

        List<IFormattableTextComponent> lore = Arrays.asList(
                new StringTextComponent("§7✧ Usa este ítem sobre otro jugador"),
                new StringTextComponent("§7   para compartir un bonus privado de EXP"),
                new StringTextComponent("§7✧ Bonus: §6+100% EXP"),
                new StringTextComponent("§7✧ Tiempo: §63 horas"),
                new StringTextComponent("§7▶ Haz click derecho sobre un jugador"),
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
