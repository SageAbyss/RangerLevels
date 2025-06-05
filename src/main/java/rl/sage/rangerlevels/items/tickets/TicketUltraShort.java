// File: rl/sage/rangerlevels/items/tickets/TicketUltraShort.java
package rl.sage.rangerlevels.items.tickets;

import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.Tier;

import java.util.Arrays;
import java.util.List;

/**
 * Define todo lo que identifica al “ticket_ultra_short”:
 *  - id = "ticket_ultra_short"
 *  - baseItem = PixelmonItems.rainbow_pass (Ultra Ball de Pixelmon)
 *  - tier = Tier.LEGENDARIO
 *  - gradiente del Tier en el nombre y en el lore
 *  - displayName = "✦ Ticket Pase Ultra Rápido ✦"
 *  - defaultLore:
 *      • Líneas con “✧” como viñetas
 *      • Incluye “✧ Caduca en §e10 §7segundos”
 *      • Flechas y “Tier:” con texto en degradado pastel según Tier.LEGENDARIO
 */
public class TicketUltraShort extends RangerItemDefinition {
    public static final String ID = "ticket_ultra_short";

    public TicketUltraShort() {
        super(
                ID,
                PixelmonItems.rainbow_pass,
                Tier.LEGENDARIO,
                /*
                 * Ya no pasamos un TextFormatting aquí, porque el "color"
                 * (ahora degradado) lo aplicaremos en createStack().
                 */
                null,
                "✦ Ticket Pase Ultra Rápido ✦",
                null  // Lore se asignará en createStack()
        );

        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        // 1) Creamos el ItemStack base con el constructor padre
        ItemStack stack = super.createStack(amount);

        // 2) Asignamos el hover-name en degradado del Tier.LEGENDARIO
        //    Usamos Tier.LEGENDARIO.getColor(), que devuelve un IFormattableTextComponent
        stack.setHoverName(Tier.LEGENDARIO.applyGradient(getDisplayName()));

        // 3) Generamos el lore dinámico con degradado para la línea del Tier
        List<IFormattableTextComponent> generatedLore = Arrays.asList(
                // 3.1) Viñeta “✧” + descripción genérica
                new StringTextComponent("§7✧ Usa este Ticket para ventajas de pase Ultra rápido."),
                // 3.2) Viñeta “✧” + caducidad en 10 segundos
                new StringTextComponent("§7✧ Caduca en §e10 §7segundos"),
                // 3.3) “▶ Tier:” en gris + nombre del Tier en degradado pastel
                //      Construimos el componente en dos partes: prefijo gris y degradado del Tier
                new StringTextComponent("§7▶ Tier: ")
                        .append(Tier.LEGENDARIO.getColor())
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
