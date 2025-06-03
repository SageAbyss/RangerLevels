package rl.sage.rangerlevels.items.tickets;

import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.items.Tier;

import java.util.Arrays;

/**
 * Definition del “Caramelo de Nivel”.
 * Al hacer clic derecho, otorga +1 nivel (si no está en nivel máximo).
 */
public class CarameloNivel extends RangerItemDefinition {
    public static final String ID = "caramelo_nivel";

    public CarameloNivel() {
        super(
                ID,
                PixelmonItems.rare_candy,
                Tier.RARO,                 // Tier RARO
                Tier.RARO.getColor(),      // Color azul
                "✹ Caramelo de Nivel ✹",    // Nombre que verá el jugador
                Arrays.asList(             // Lore por defecto
                        (IFormattableTextComponent) new StringTextComponent(
                                TextFormatting.GRAY + "✧ Come este Caramelo para subir +1 nivel."
                        ),
                        (IFormattableTextComponent) new StringTextComponent(
                                TextFormatting.GRAY + "✧ Se consume al usarlo."
                        ),
                        (IFormattableTextComponent) new StringTextComponent(
                                Tier.RARO.getColor() + "§7▶ Tier: " + Tier.RARO.getColor() + Tier.RARO.getDisplayName()
                        )
                )
        );

        // Registrar en el CustomItemRegistry
        CustomItemRegistry.register(this);
    }
}
