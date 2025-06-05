package rl.sage.rangerlevels.gui.rewards;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.capability.RewardStatus;
import rl.sage.rangerlevels.capability.PlayerRewardsProvider;
import rl.sage.rangerlevels.config.RewardConfig;
import rl.sage.rangerlevels.gui.MenuItemBuilder;
import rl.sage.rangerlevels.gui.PlayerInfoUtils;
import rl.sage.rangerlevels.rewards.RewardManager;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Menú paginado que muestra todas las recompensas “EveryLevel” que el jugador
 * tiene en estado PENDING. Permite “Reclamar” individual o todas, navegar entre páginas,
 * y volver al menú anterior.
 */
public class EveryLevelMenu {
    // ranuras donde se colocan los botones de recompensa (4 filas × 7 columnas)
    private static final int[] SLOT_INDICES = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };
    private static final int SLOTS_PER_PAGE = SLOT_INDICES.length;

    /** Abre la página 1 por defecto */
    public static void open(ServerPlayerEntity player) {
        open(player, 1);
    }

    /**
     * Abre el menú “EveryLevel” en la página indicada (1-based), mostrando solo
     * las recompensas con estatus PENDING, ordenadas por nivel y ruta.
     */
    public static void open(final ServerPlayerEntity player, int page) {
        @Nullable
        rl.sage.rangerlevels.capability.IPlayerRewards cap =
                player.getCapability(PlayerRewardsProvider.REWARDS_CAP, null).orElse(null);
        if (cap == null) return;

        // 1) Filtrar y ordenar claves “EveryLevel:<nivel>:<ruta>” con estado PENDING
        List<String> pendingKeys = new ArrayList<>();
        for (Entry<String, RewardStatus> entry : cap.getStatusMap().entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("EveryLevel:") && entry.getValue() == RewardStatus.PENDING) {
                pendingKeys.add(key);
            }
        }
        Collections.sort(pendingKeys, new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                String[] pa = a.split(":");
                String[] pb = b.split(":");
                int lvlA = Integer.parseInt(pa[1]);
                int lvlB = Integer.parseInt(pb[1]);
                if (lvlA != lvlB) return lvlA - lvlB;
                return pa[2].compareTo(pb[2]);
            }
        });

        // 2) Calcular total y páginas
        int total    = pendingKeys.size();
        int maxPages = Math.max(1, (int) Math.ceil(total / (double) SLOTS_PER_PAGE));
        if (page < 1) page = 1;
        if (page > maxPages) page = maxPages;

        // 3) Crear inventario de 54 ranuras (6×9) y título dinámico
        final Inventory inv = new Inventory(54);
        inv.clearContent();
        final ITextComponent title = new StringTextComponent(
                "§6Recompensas por Nivel §7(" + page + "/" + maxPages + ")"
        );

        // 4) Botones fijos
        inv.setItem(4, PlayerInfoUtils.getInfoItem(player, 4));
        inv.setItem(49, MenuItemBuilder.createButton(
                "§aReclamar todas",
                Collections.singletonList("§7Haz clic para reclamar todas las recompensas pendientes"),
                Items.EMERALD_BLOCK,
                "claim_all",
                49
        ));
        inv.setItem(53, MenuItemBuilder.createButton(
                "§cVolver",
                Collections.singletonList("§7Haz clic para regresar al menú anterior"),
                Items.BARRIER,
                "back",
                53
        ));

        // 5) Navegación: “página anterior” y “página siguiente”
        if (page > 1) {
            inv.setItem(45, MenuItemBuilder.createButton(
                    "§e« Página " + (page - 1),
                    Collections.singletonList("§7Ir a la página anterior"),
                    Items.ARROW,
                    "page:" + (page - 1),
                    45
            ));
        }
        if (page < maxPages) {
            inv.setItem(51, MenuItemBuilder.createButton(
                    "§ePágina " + (page + 1) + " »",
                    Collections.singletonList("§7Ir a la página siguiente"),
                    Items.ARROW,
                    "page:" + (page + 1),
                    51
            ));
        }

        // 6) Rellenar botones de recompensa para esta página
        int start = (page - 1) * SLOTS_PER_PAGE;
        int end   = Math.min(start + SLOTS_PER_PAGE, total);
        for (int i = start; i < end; i++) {
            String key   = pendingKeys.get(i);
            String[] parts = key.split(":");
            String nivel  = parts[1];
            String ruta   = parts[2];
            int slotIndex = SLOT_INDICES[i - start];

            // ID: "EveryLevel.<nivel>.<ruta>.<página>"
            String btnId = "EveryLevel." + nivel + "." + ruta + "." + page;
            ItemStack button = MenuItemBuilder.createButton(
                    "§eNivel " + nivel + " → " + ruta,
                    Collections.singletonList("§7Haz clic para reclamar esta recompensa."),
                    Items.DIAMOND,
                    btnId,
                    slotIndex
            );
            inv.setItem(slotIndex, button);
        }

        // 7) Proveedor anónimo para abrir el contenedor
        SimpleNamedContainerProvider provider = new SimpleNamedContainerProvider(
                (windowId, playerInv, unused) -> new EveryLevelMenuContainer(windowId, playerInv, inv),
                title
        );
        player.openMenu(provider);
    }

    /** Reclama una sola recompensa “EveryLevel” identificada por nivel y ruta. */
    public static void claimSingle(ServerPlayerEntity player, String nivel, String ruta) {
        PlayerSoundUtils.playSoundToPlayer(
                player,
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundCategory.MASTER,
                1.0f,
                0.5f
        );
        String key = "EveryLevel:" + nivel + ":" + ruta;
        @Nullable
        rl.sage.rangerlevels.capability.IPlayerRewards cap =
                player.getCapability(PlayerRewardsProvider.REWARDS_CAP, null).orElse(null);
        if (cap == null) return;

        RewardStatus status = cap.getStatusMap().getOrDefault(key, RewardStatus.BLOCKED);
        if (status == RewardStatus.PENDING) {
            cap.setStatus(key, RewardStatus.CLAIMED);
            RewardConfig.RouteRewards rr = RewardConfig.get().Rewards.EveryLevel.get(ruta);
            RewardManager.executeRouteRewards(player, player.server, rr, "EveryLevel");
            player.sendMessage(
                    new StringTextComponent("§aRecompensa Nivel " + nivel + " (" + ruta + ") reclamada."),
                    player.getUUID()
            );
        } else {
            player.sendMessage(
                    new StringTextComponent("§cEsta recompensa no está pendiente."),
                    player.getUUID()
            );
        }
    }

    /** Reclama todas las recompensas “EveryLevel” que estén en estado PENDING. */
    public static void claimAll(ServerPlayerEntity player) {
        @Nullable
        rl.sage.rangerlevels.capability.IPlayerRewards cap =
                player.getCapability(PlayerRewardsProvider.REWARDS_CAP, null).orElse(null);
        if (cap == null) return;

        boolean any = false;
        for (Entry<String, RewardStatus> entry : cap.getStatusMap().entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("EveryLevel:") && entry.getValue() == RewardStatus.PENDING) {
                String[] parts = key.split(":");
                claimSingle(player, parts[1], parts[2]);
                any = true;
            }
        }
        if (any) {
            PlayerSoundUtils.playSoundToPlayer(
                    player,
                    SoundEvents.PLAYER_LEVELUP,
                    SoundCategory.MASTER,
                    1.0f,
                    0.5f
            );
        } else {
            player.sendMessage(
                    new StringTextComponent("§cNo hay recompensas por reclamar."),
                    player.getUUID()
            );
        }
    }
}
