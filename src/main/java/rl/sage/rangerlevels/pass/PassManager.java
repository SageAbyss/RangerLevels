package rl.sage.rangerlevels.pass;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import rl.sage.rangerlevels.config.ExpConfig;
import rl.sage.rangerlevels.util.GradientText;

import java.util.Arrays;
import java.util.List;

/**
 * Gestiona los diferentes tipos de pase y permisos de acceso.
 */
public class PassManager {

    private static IPermissionProvider permissionProvider = new PermissionAPIProvider();

    /** Orden de prioridad para determinar el pase actual. */
    private static final List<PassType> PRIORITY = Arrays.asList(
            PassType.MASTER, PassType.ULTRA, PassType.SUPER, PassType.FREE
    );

    public static void setPermissionProvider(IPermissionProvider provider) {
        if (provider != null) permissionProvider = provider;
    }

    public enum PassType {
        FREE(0,
                GradientText.of("◎ Free Pass", "#FFFFFF", "#B3B3B3"),
                "Pase básico sin coste"
        ),
        SUPER(1,
                GradientText.of("✷ Super Pass", "#9F99F7", "#CD6B90"),
                "XP ×1.25, Recompensas por pase"
        ),
        ULTRA(2,
                GradientText.of("✸ Ultra Pass", "#ABBA5B", "#1CDD93", "#209A86"),
                "XP ×1.5, Recompensas por pase"
        ),
        MASTER(3,
                GradientText.of("✹ Master Pass", "#D7DF0C", "#F38326", "#D5C365"),
                "XP ×2.0, Recompensas por pase" //+ "\n" + "S" POR SI QUIERES AGREGAR MAS LINEAS
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

        /** Devuelve una copia del nombre gradient. */
        public IFormattableTextComponent getGradientDisplayName() {
            return gradientName.copy();
        }

        public String getDescription() {
            return description;
        }

        /**
         * Toma la URL de compra desde tu Config.yml
         * (keys: "super","ultra","master")
         */
        public String getPurchaseUrl() {
            // Asegúrate de que en Config.yml las claves coincidan en minúsculas
            return ExpConfig.get().getPassBuyUrls()
                    .getOrDefault(this.name().toLowerCase(), "");
        }

        /** Ejemplo de nodo: "rangerlevels.pass.super" */
        public String getPermissionNode() {
            return "rangerlevels.pass." + this.name().toLowerCase();
        }
    }

    /** Pase más alto que tiene el jugador. */
    public static PassType getPass(ServerPlayerEntity player) {
        for (PassType type : PRIORITY) {
            if (permissionProvider.hasPermission(player, type.getPermissionNode())) {
                return type;
            }
        }
        return PassType.FREE;
    }

    /** Comprueba acceso al pase requerido o superior. */
    public static boolean hasAccessTo(ServerPlayerEntity player, PassType required) {
        return getPass(player).getTier() >= required.getTier();
    }

    /** Registra automáticamente los nodos de permiso de cada PassType. */
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
