// File: rl/sage/rangerlevels/items/tickets/TicketSuper.java
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
 * Define todo lo que identifica al “ticket_super”:
 *  - id = "ticket_super"
 *  - baseItem = PixelmonItems.rainbow_pass (Ultra Ball de Pixelmon)
 *  - tier = Tier.EPICO, pero usamos degradado pastel para el nombre y el lore
 *  - displayName = "✦ Ticket Pase Super ✦"
 *  - defaultLore: viñetas, caducidad y “Tier” en degradado.
 */
public class TicketSuper extends RangerItemDefinition {
    public static final String ID = "ticket_super";

    public TicketSuper() {
        super(
                ID,
                PixelmonItems.rainbow_pass,
                Tier.EPICO,
                null,                               // Color sólido ya no se usa
                "✦ Ticket Pase Super ✦",
                null                                // Lore se asigna en createStack()
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        // 1) Creamos el ItemStack base
        ItemStack stack = super.createStack(amount);

        // 2) Asignamos el hover-name con degradado pastel de Tier.EPICO
        stack.setHoverName(Tier.EPICO.applyGradient(getDisplayName()));

        // 3) Creamos el lore con la línea de Tier en degradado
        List<IFormattableTextComponent> generatedLore = Arrays.asList(
                // 3.1) Viñeta “✧” + descripción genérica
                new StringTextComponent("§7✧ Usa este Ticket para ventajas de pase."),
                // 3.2) Viñeta “✧” + caducidad en 24 horas
                new StringTextComponent("§7✧ Caduca en §e24 §7horas"),
                // 3.3) “▶ Tier:” en gris + “EPICO” en degradado pastel
                new StringTextComponent("§7▶ Tier: ").append(Tier.EPICO.getColor())
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
