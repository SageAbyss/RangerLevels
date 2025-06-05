// File: rl/sage/rangerlevels/items/tickets/TicketUltra.java
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

import java.util.Arrays;
import java.util.List;

/**
 * Define todo lo que identifica al “ticket_ultra”:
 *  - id = "ticket_ultra"
 *  - baseItem = PixelmonItems.rainbow_pass (Ultra Ball de Pixelmon)
 *  - tier = Tier.LEGENDARIO, pero usamos degradado pastel para el nombre y el lore
 *  - displayName = "✦ Ticket Pase Ultra ✦"
 *  - defaultLore: viñetas, caducidad y “Tier” en degradado.
 */
public class TicketUltra extends RangerItemDefinition {
    public static final String ID = "ticket_ultra";

    public TicketUltra() {
        super(
                ID,
                PixelmonItems.rainbow_pass,
                Tier.LEGENDARIO,
                null,                               // Color sólido ya no se usa
                "✦ Ticket Pase Ultra ✦",
                null                                // Lore se asigna en createStack()
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        // 1) Creamos el ItemStack base
        ItemStack stack = super.createStack(amount);

        // 2) Asignamos el hover-name con degradado pastel de Tier.LEGENDARIO
        stack.setHoverName(Tier.LEGENDARIO.applyGradient(getDisplayName()));

        // 3) Creamos el lore con la línea de Tier en degradado
        List<IFormattableTextComponent> generatedLore = Arrays.asList(
                // 3.1) Viñeta “✧” + descripción genérica
                new StringTextComponent("§7✧ Usa este Ticket para ventajas de pase Ultra."),
                // 3.2) Viñeta “✧” + caducidad en 24 horas
                new StringTextComponent("§7✧ Caduca en §e24 §7horas"),
                // 3.3) “▶ Tier:” en gris + “LEGENDARIO” en degradado pastel
                new StringTextComponent("§7▶ Tier: ").append(Tier.LEGENDARIO.getColor())
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
        int hide = tag.contains("HideFlags") ? tag.getInt("HideFlags") : 0;
        hide |= 32;
        tag.putInt("HideFlags", hide);

        stack.setTag(tag);
        return stack;
    }
}
