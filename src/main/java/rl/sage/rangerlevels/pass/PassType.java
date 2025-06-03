package rl.sage.rangerlevels.pass;

import net.minecraft.util.text.IFormattableTextComponent;
import rl.sage.rangerlevels.util.GradientText;

/**
 * Enumeración de tipos de pase y sus propiedades. Solo representa el
 * número de tier; toda la lógica de “temporalidad” va en PassManager/Capability.
 */
public enum PassType {
    FREE(0,
            GradientText.of("◎ Free Pass", "#FFFFFF", "#B3B3B3"),
            "Pase básico sin costo"
    ),
    SUPER(1,
            GradientText.of("✷ Super Pass", "#9F99F7", "#CD6B90"),
            "XP ×1.25, Recompensas por pase, Limite +10%"
    ),
    ULTRA(2,
            GradientText.of("✸ Ultra Pass", "#ABBA5B", "#1CDD93", "#209A86"),
            "XP ×1.5, Recompensas por pase, Limite +20%"
    ),
    MASTER(3,
            GradientText.of("✹ Master Pass", "#D7DF0C", "#F38326", "#D5C365"),
            "XP ×2.0, Recompensas por pase, Limite +50%"
    );

    private final int tier;
    private final IFormattableTextComponent gradientName;
    private final String description;

    PassType(int tier,
             IFormattableTextComponent gradientName,
             String description) {
        this.tier = tier;
        this.gradientName = gradientName;
        this.description = description;
    }

    public int getTier() {
        return tier;
    }

    public IFormattableTextComponent getGradientDisplayName() {
        return gradientName.copy();
    }

    public String getDescription() {
        return description;
    }
}
