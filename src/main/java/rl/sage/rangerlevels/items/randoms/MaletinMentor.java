// File: src/main/java/rl/sage/rangerlevels/items/randoms/MaletinMentor.java
package rl.sage.rangerlevels.items.randoms;

import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.Tier;

import java.util.Arrays;
import java.util.List;

public class MaletinMentor extends RangerItemDefinition {
    public static final String ID = "maletin_mentor";

    public MaletinMentor() {
        super(
                ID,
                PixelmonItems.prop_case,
                Tier.RARO,
                null,
                "✦ Maletín del Mentor ✦",
                buildLore()
        );
        CustomItemRegistry.register(this);
    }

    private static List<IFormattableTextComponent> buildLore() {
        return Arrays.asList(
                new StringTextComponent("§7✧ Usa este maletín sobre otro"),
                new StringTextComponent("§7   jugador para transferirle tu EXP actual"),
                new StringTextComponent("§7✧ Se borrará tu EXP y se sumará"),
                new StringTextComponent("§7   al receptor. ¡Cuidado!"),
                new StringTextComponent(" "),
                new StringTextComponent("§7▶ Tier: ").append(Tier.RARO.getColor())
        );
    }

    @Override
    public ItemStack createStack(int amount) {
        // Forza cantidad=1 siempre
        return super.createStack(1);
    }
}
