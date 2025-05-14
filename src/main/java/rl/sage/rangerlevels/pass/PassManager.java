package rl.sage.rangerlevels.pass;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import rl.sage.rangerlevels.util.GradientText;

import java.util.Arrays;
import java.util.List;

/**
 * Gestiona los diferentes tipos de pase y permisos de acceso.
 */
public class PassManager {

    /**
     * Proveedor de permisos inyectable (por defecto usa PermissionAPI).
     */
    private static IPermissionProvider permissionProvider = new PermissionAPIProvider();

    /**
     * Orden de prioridad para determinar el pase actual.
     */
    private static final List<PassType> PRIORITY = Arrays.asList(
            PassType.MASTER, PassType.ULTRA, PassType.SUPER, PassType.FREE
    );

    /**
     * Permite reemplazar el proveedor de permisos (p. ej. para tests).
     */
    public static void setPermissionProvider(IPermissionProvider provider) {
        if (provider != null) {
            permissionProvider = provider;
        }
    }

    /**
     * Enum con datos completos de cada pase.
     */
    public enum PassType {
        FREE(0,
                "rangerlevels.passtype.free",      // clave de idioma para el nombre
                GradientText.of("◎ Free Pass", "#FFFFFF", "#B3B3B3"),
                "Pase básico sin coste",
                "https://tu-tienda.com/free"
        ),
        SUPER(1,
                "rangerlevels.passtype.super",
                GradientText.of("✷ Super Pass", "#9F99F7", "#CD6B90"),
                "XP ×2, acceso a zona VIP",
                "https://tu-tienda.com/super"
        ),
        ULTRA(2,
                "rangerlevels.passtype.ultra",
                GradientText.of("✸ Ultra Pass", "#ABBA5B", "#1CDD93", "#209A86"),
                "XP ×3, objetos exclusivos",
                "https://tu-tienda.com/ultra"
        ),
        MASTER(3,
                "rangerlevels.passtype.master",
                GradientText.of("✹ Master Pass", "#D7DF0C", "#F38326", "#D5C365"),
                "XP ×5, comandos especiales",
                "https://tu-tienda.com/master"
        );

        private final int tier;
        private final String langKey;
        private final IFormattableTextComponent gradientName;
        private final String description;
        private final String purchaseUrl;

        PassType(int tier,
                 String langKey,
                 IFormattableTextComponent gradientName,
                 String description,
                 String purchaseUrl) {
            this.tier = tier;
            this.langKey = langKey;
            // Creamos el componente traducible y aplicamos el estilo gradiente
            this.gradientName = new TranslationTextComponent(langKey)
                    .withStyle(Style.EMPTY.withBold(false))
                    .append(gradientName)
                    .withStyle(Style.EMPTY);
            this.description = description;
            this.purchaseUrl = purchaseUrl;
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

        public String getPurchaseUrl() {
            return purchaseUrl;
        }

        /** Nodo de permiso asociado a este pase, e.g. "rangerlevels.pass.super" */
        public String getPermissionNode() {
            return "rangerlevels.pass." + this.name().toLowerCase();
        }
    }

    /**
     * Devuelve el pase más alto que el jugador posee, según permisos.
     */
    public static PassType getPass(ServerPlayerEntity player) {
        for (PassType type : PRIORITY) {
            if (permissionProvider.hasPermission(player, type.getPermissionNode())) {
                return type;
            }
        }
        return PassType.FREE;
    }

    /**
     * Comprueba si el jugador tiene acceso al nivel de pase requerido o superior.
     */
    public static boolean hasAccessTo(ServerPlayerEntity player, PassType required) {
        return getPass(player).getTier() >= required.getTier();
    }

    /**
     * Registra todos los nodos de permiso automáticamente según PassType.
     */
    public static void registerPermissions() {
        for (PassType type : PassType.values()) {
            PermissionAPI.registerNode(
                    type.getPermissionNode(),
                    DefaultPermissionLevel.NONE,
                    "Acceso al " + type.name() + " (" + type.getDescription() + ")"
            );
        }
    }
}
