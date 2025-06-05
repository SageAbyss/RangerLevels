package rl.sage.rangerlevels.gui.rewards;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import rl.sage.rangerlevels.gui.BaseMenuContainer6;

/**
 * Contenedor para EveryLevelMenu (6×9), hereda BaseMenuContainer6.
 * Conserva exactamente los mismos índices de botones:
 *   - Slot  4: info jugador (no “clicable” porque no lleva tag MenuButtonID en este caso).
 *   - Slot 45: “Página anterior” (si page > 1).
 *   - Slot 49: “Reclamar todas” (claim_all).
 *   - Slot 51: “Página siguiente” (si page < maxPages).
 *   - Slot 53: “Volver” (back).
 *   - Cualquier otro slot de la matriz SLOT_INDICES tiene tag “EveryLevel.<nivel>.<ruta>.<page>”.
 */
public class EveryLevelMenuContainer extends BaseMenuContainer6 {
    private final Inventory menuInv;

    public EveryLevelMenuContainer(int windowId, PlayerInventory inv, Inventory menuInv) {
        super(windowId, inv, menuInv);
        this.menuInv = menuInv;
    }

    @Override
    protected void handleButtonClick(String buttonId, ServerPlayerEntity player) {
        // 1) Paginación: id = "page:<número>"
        if (buttonId.startsWith("page:")) {
            try {
                int newPage = Integer.parseInt(buttonId.split(":")[1]);
                EveryLevelMenu.open(player, newPage);
            } catch (NumberFormatException ignore) {}
            return;
        }

        switch (buttonId) {
            case "claim_all":
                // Reclama todas y vuelve a página 1
                EveryLevelMenu.claimAll(player);
                player.getServer().execute(() -> {
                    EveryLevelMenu.open(player, 1);
                });
                break;

            case "back":
                // Cierra y en el siguiente tick abre RewardsMenu
                player.closeContainer();
                player.getServer().execute(() -> {
                    RewardsMenu.open(player);
                });
                break;

            default:
                // "EveryLevel.<nivel>.<ruta>.<page>"
                if (buttonId.startsWith("EveryLevel.")) {
                    String[] parts = buttonId.split("\\.");
                    if (parts.length == 4) {
                        String nivel = parts[1];
                        String ruta  = parts[2];
                        int page;
                        try {
                            page = Integer.parseInt(parts[3]);
                        } catch (NumberFormatException e) {
                            page = 1;
                        }
                        // Reclama esa recompensa y reabre en la misma página
                        EveryLevelMenu.claimSingle(player, nivel, ruta);
                        player.getServer().execute(() -> {
                            EveryLevelMenu.open(player, 1);
                        });
                    }
                }
                break;
        }
    }
}
