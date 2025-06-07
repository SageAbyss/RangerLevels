package rl.sage.rangerlevels.items.cetro;

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
 * Cetro Divino - Tier Épico:
 *  - Restaura 50% de HP de jugador + equipo Pokémon
 *  - Restaura 50% de PP de cada Pokémon
 *  - Tiene 30 usos (se guardan en NBT del ItemStack)
 */
public class CetroDivinoEpico extends RangerItemDefinition {
    public static final String ID = "cetro_divino_epico";
    private static final int USOS_INICIALES = 30;
    private static final double RESTAURA_HP_PORC = 0.50;
    private static final double RESTAURA_PP_PORC = 0.50;
    private static final String NBT_USOS_KEY = "CetroDivinoUsos";

    public CetroDivinoEpico() {
        super(
                ID,
                PixelmonItems.fairy_wand,     // Blaze Rod de base (puedes cambiarlo)
                Tier.EPICO,
                null,
                "✦ Cetro Divino ✦",
                null
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(amount);
        stack.setHoverName(Tier.EPICO.applyGradient(getDisplayName()));

        CompoundNBT tag = stack.getOrCreateTag();
        if (!tag.contains(NBT_USOS_KEY)) {
            tag.putInt(NBT_USOS_KEY, USOS_INICIALES);
        }

        int usosRestantes = tag.getInt(NBT_USOS_KEY);
        List<IFormattableTextComponent> lore = Arrays.asList(
                new StringTextComponent("§7✧ Restaura el 50% de tu HP y del equipo Pokémon"),
                new StringTextComponent("§7✧ Restaura 50% de PP de cada Pokémon"),
                new StringTextComponent("§7✧ Usos restantes: §e" + usosRestantes),
                new StringTextComponent("§7▶ Tier: ").append(Tier.EPICO.getColor())
        );

        CompoundNBT displayTag = tag.contains("display") ? tag.getCompound("display") : new CompoundNBT();
        ListNBT loreList = new ListNBT();
        for (IFormattableTextComponent line : lore) {
            String json = IFormattableTextComponent.Serializer.toJson(line);
            loreList.add(StringNBT.valueOf(json));
        }
        displayTag.put("Lore", loreList);
        tag.put("display", displayTag);

        NBTUtils.applyAllHideFlags(tag);

        stack.setTag(tag);
        return stack;
    }

    public static int getUsosRestantes(ItemStack stack) {
        if (stack == null || !stack.hasTag()) return 0;
        CompoundNBT tag = stack.getTag();
        if (!tag.contains(NBT_USOS_KEY)) return 0;
        return tag.getInt(NBT_USOS_KEY);
    }

    public static void decrementarUso(ItemStack stack) {
        if (stack == null || !stack.hasTag()) return;
        CompoundNBT tag = stack.getTag();
        if (!tag.contains(NBT_USOS_KEY)) return;

        int usos = tag.getInt(NBT_USOS_KEY);
        if (usos <= 0) return;

        usos--;
        tag.putInt(NBT_USOS_KEY, usos);

        // Actualizar lore con usos restantes
        CompoundNBT displayTag = tag.getCompound("display");
        ListNBT loreList = new ListNBT();
        List<IFormattableTextComponent> nuevaLore = Arrays.asList(
                new StringTextComponent("§7✧ Restaura el 50% de tu HP y del equipo Pokémon"),
                new StringTextComponent("§7✧ Restaura 50% de PP de cada Pokémon"),
                new StringTextComponent("§7✧ Usos restantes: §e" + usos),
                new StringTextComponent("§7▶ Tier: ").append(Tier.EPICO.getColor())
        );
        for (IFormattableTextComponent line : nuevaLore) {
            String json = IFormattableTextComponent.Serializer.toJson(line);
            loreList.add(StringNBT.valueOf(json));
        }
        displayTag.put("Lore", loreList);
        tag.put("display", displayTag);

        stack.setTag(tag);
    }

    public static double getHpRestorePorc() {
        return RESTAURA_HP_PORC;
    }

    public static double getPpRestorePorc() {
        return RESTAURA_PP_PORC;
    }
}
