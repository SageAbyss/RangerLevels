package rl.sage.rangerlevels.items.totems.fragmentos;

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

public class FragmentoRealidadAlterna extends RangerItemDefinition {
    public static final String ID = "fragmento_realidad_alterna";

    public FragmentoRealidadAlterna() {
        super(
                ID,
                PixelmonItems.blue_petal,  // o el ítem base que prefieras para representar “Realidad Alterna”
                Tier.LEGENDARIO,
                null,
                "✦ Fragmento de Realidad Alterna ✦",
                null
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(amount);

        // 1) Nombre con degradado LEGENDARIO (igual al ejemplo)
        stack.setHoverName(Tier.LEGENDARIO.applyGradient(getDisplayName()));

        // 2) Lore con descripción de efectos
        List<IFormattableTextComponent> lore = Arrays.asList(
                new StringTextComponent("§7✧ Tipos Afectados: Hielo, Agua, Volador, Veneno, Hada, Acero"),
                new StringTextComponent("§7✧ Otorga los efectos:"),
                new StringTextComponent("   §7- Saturación I"),
                new StringTextComponent("   §7- Lentitud I"),
                new StringTextComponent("§7✧ Bonus de §6+15% §7de EXP por derrotas y capturas salvajes"),
                new StringTextComponent(" "),
                new StringTextComponent("§7▶ Tier: ").append(Tier.LEGENDARIO.getColor())
        );

        CompoundNBT tag = stack.getOrCreateTag();
        CompoundNBT display = tag.contains("display")
                ? tag.getCompound("display")
                : new CompoundNBT();
        ListNBT loreList = new ListNBT();
        for (IFormattableTextComponent line : lore) {
            loreList.add(StringNBT.valueOf(IFormattableTextComponent.Serializer.toJson(line)));
        }
        display.put("Lore", loreList);
        tag.put("display", display);

        // 3) Ocultar atributos extra
        NBTUtils.applyAllHideFlags(tag);
        stack.setTag(tag);
        return stack;
    }
}
