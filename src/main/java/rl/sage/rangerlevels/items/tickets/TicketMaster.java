// File: rl/sage/rangerlevels/items/tickets/TicketMaster.java
package rl.sage.rangerlevels.items.tickets;

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

import java.util.Arrays;
import java.util.List;

/**
 * Define todo lo que identifica al “ticket_master”:
 *  - id = "ticket_master"
 *  - baseItem = PixelmonItems.rainbow_pass (Ultra Ball de Pixelmon)
 *  - tier = Tier.MITICO, pero usamos degradado pastel para el nombre y el lore
 *  - displayName = "✦ Ticket Pase Master ✦"
 *  - defaultLore: viñetas, caducidad y “Tier” en degradado.
 */
public class TicketMaster extends RangerItemDefinition {
    public static final String ID = "ticket_master";

    public TicketMaster() {
        super(
                ID,
                PixelmonItems.rainbow_pass,
                Tier.MITICO,
                null,                               // Color sólido ya no se usa
                "✦ Ticket Pase Master ✦",
                null                                // Lore se asigna en createStack()
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        // 1) Creamos el ItemStack base
        ItemStack stack = super.createStack(amount);

        // 2) Asignamos el hover-name con degradado pastel de Tier.MITICO
        stack.setHoverName(Tier.MITICO.applyGradient(getDisplayName()));

        // 3) Creamos el lore con la línea de Tier en degradado
        List<IFormattableTextComponent> generatedLore = Arrays.asList(
                // 3.1) Viñeta “✧” + descripción genérica
                new StringTextComponent("§7✧ Usa este Ticket para ventajas de pase Ultra."),
                // 3.2) Viñeta “✧” + caducidad en 24 horas
                new StringTextComponent("§7✧ Caduca en §e24 §7horas"),
                new StringTextComponent("§7✧ Click para activar"),
                new StringTextComponent(" "),
                // 3.3) “▶ Tier:” en gris + “MITICO” en degradado pastel
                new StringTextComponent("§7▶ Tier: ").append(Tier.MITICO.getColor())
        );

        // 4) Insertamos el lore en NBT
        CompoundNBT tag = stack.getOrCreateTag();
        CompoundNBT display = tag.contains("display")
                ? tag.getCompound("display")
                : new CompoundNBT();
        net.minecraft.nbt.ListNBT loreList = new net.minecraft.nbt.ListNBT();
        for (IFormattableTextComponent line : generatedLore) {
            String json = IFormattableTextComponent.Serializer.toJson(line);
            loreList.add(net.minecraft.nbt.StringNBT.valueOf(json));
        }
        display.put("Lore", loreList);
        tag.put("display", display);

        // 5) Ocultamos atributos innecesarios (HideFlags bit 32)
        NBTUtils.applyAllHideFlags(tag);

        stack.setTag(tag);
        return stack;
    }
}
