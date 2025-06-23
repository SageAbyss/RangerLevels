// File: rl/sage/rangerlevels/items/randoms/DestinoVinculadoEpico.java
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

public class DestinoVinculadoEpico extends RangerItemDefinition {
    public static final String ID = "destino_vinculado_epico";

    public DestinoVinculadoEpico() {
        super(
                ID,
                PixelmonItems.escape_rope,
                Tier.EPICO,
                null,
                "✦ Destino Vinculado ✦",
                null
        );
        CustomItemRegistry.register(this);
    }
    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(1);
        stack.setHoverName(Tier.EPICO.applyGradient(getDisplayName()));

        List<IFormattableTextComponent> lore = Arrays.asList(
                new StringTextComponent("§7✧ Usa este ítem sobre otro jugador"),
                new StringTextComponent("§7   para compartir un bonus privado de EXP"),
                new StringTextComponent("§7✧ Bonus: §6+50% EXP"),
                new StringTextComponent("§7✧ Tiempo: §61 hora"),
                new StringTextComponent("§7▶ Haz click derecho sobre un jugador"),
                new StringTextComponent(" "),
                new StringTextComponent("§7▶ Tier: ").append(Tier.EPICO.getColor())
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
