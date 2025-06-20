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
import net.minecraftforge.registries.ForgeRegistries;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.Tier;
import rl.sage.rangerlevels.util.EnchantUtils;
import rl.sage.rangerlevels.util.GradientText;
import rl.sage.rangerlevels.util.NBTUtils;

import java.util.Arrays;
import java.util.List;

public class AltarArcano extends RangerItemDefinition {
    public static final String ID = "altar_arcano";
    private static Item findArcChalice() {
        Item i = ForgeRegistries.ITEMS
                .getValue(new ResourceLocation("pixelmon", "arc_chalice"));
        return i != null ? i : Items.LECTERN;
    }
    public AltarArcano() {
        super(
                ID,
                findArcChalice(),
                Tier.LEGENDARIO,
                null,
                "✦ Altar Arcano de la Creación ✦",
                null
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(amount);

        stack.setHoverName(GradientText.of(getDisplayName(), "#D65353","#C6A592","#EC521F")
                .withStyle(style -> style.withItalic(false))
                .withStyle(style -> style.withBold(true)));
        // Añadir lore
        List<IFormattableTextComponent> lore = Arrays.asList(
                new StringTextComponent("§7⚝ Invoca Artefactos Míticos"),
                new StringTextComponent("§7⚝ Contiene un inmenso poder destructivo"),
                new StringTextComponent(" "),
                new StringTextComponent("§7✧ Requisitos para usar:"),
                new StringTextComponent("§7▶  Estructura base Arcana"),
                new StringTextComponent("§7▶  Diferentes ingredientes por invocación"),
                new StringTextComponent(" "),
                new StringTextComponent("§7✧ Puntos clave:"),
                new StringTextComponent("§7▶ Una vez activado no hay marcha atrás"),
                new StringTextComponent("§7▶ Fallar trae consecuencias destructivas"),
                new StringTextComponent("§7▶ La fuerza Arcana elige el destino"),
                new StringTextComponent("§4⚡ Úsalo bajo tu propio riesgo"),
                new StringTextComponent(" "),
                new StringTextComponent("§7Colócalo sobre el suelo para iniciar."),
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
        EnchantUtils.addEnchantment(stack, Enchantments.UNBREAKING, 1);
        NBTUtils.applyAllHideFlags(tag);
        stack.setTag(tag);
        return stack;
    }
}
