// File: rl/sage/rangerlevels/items/tickets/TicketSuper.java
package rl.sage.rangerlevels.items.tickets;

import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.Tier;
import rl.sage.rangerlevels.util.EnchantUtils;
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
                "✦ Ticket Pase Super Temporal ✦",
                null                                // Lore se asigna en createStack()
        );
        CustomItemRegistry.register(this);
    }

    @Override
    public ItemStack createStack(int amount) {
        ItemStack stack = super.createStack(amount);
        stack.setHoverName(Tier.EPICO.applyGradient(getDisplayName()));

        List<IFormattableTextComponent> generatedLore = Arrays.asList(
                new StringTextComponent("§7✧ Usa este Ticket para ventajas de pase."),
                new StringTextComponent("§7✧ Caduca en §e24 §7horas"),
                new StringTextComponent("§7✧ Click para activar"),
                new StringTextComponent(" "),
                new StringTextComponent("§7▶ Tier: ").append(Tier.EPICO.getColor())
        );
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
        EnchantUtils.addEnchantment(stack, Enchantments.UNBREAKING, 1);
        NBTUtils.applyAllHideFlags(tag);
        stack.setTag(tag);
        return stack;
    }
}
