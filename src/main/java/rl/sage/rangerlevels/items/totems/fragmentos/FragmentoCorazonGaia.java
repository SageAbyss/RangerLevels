package rl.sage.rangerlevels.items.totems.fragmentos;

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

/**
 * Fragmento del Corazón de Gaia: Tier LEGENDARIO.
 * Otorga Resistencia I y Suerte I mientras esté en inventario.
 * Bonus +20% EXP contra salvajes de tipos Bug, Ground, Rock, Normal, Electric y Grass.
 */
public class FragmentoCorazonGaia extends RangerItemDefinition {
    public static final String ID = "fragmento_corazon_gaia";

    public FragmentoCorazonGaia() {
        super(
                ID,
                PixelmonItems.quick_claw,    // Cambia si quieres otro ítem base
                Tier.LEGENDARIO,
                null,
                "✦ Fragmento del Corazón de Gaia ✦",
                null  // lore por defecto vacío; lo añadimos en createStack
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        // siempre count = 1 internamente
        ItemStack stack = super.createStack(amount);

        // 1) Nombre con degradado Tier LEGENDARIO
        stack.setHoverName(Tier.LEGENDARIO.applyGradient(getDisplayName()));

        // 2) Lore con descripción de efectos
        List<IFormattableTextComponent> lore = Arrays.asList(
                new StringTextComponent("§7✧ Tipos Afectados: Bicho, Tierra, Roca, Normal, Eléctrico, Planta"),
                new StringTextComponent("§7✧ Otorga los efectos:"),
                new StringTextComponent("   §7- Resistencia I"),
                new StringTextComponent("   §7- Suerte I"),
                new StringTextComponent("§7✧ Bonus de §6+15% §7de EXP en combates y capturas salvajes"),
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

        // 3) Ocultar atributos y demás
        NBTUtils.applyAllHideFlags(tag);

        stack.setTag(tag);
        return stack;
    }
}
