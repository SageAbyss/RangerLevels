// src/main/java/rl/sage/rangerlevels/gui/rewards/PackagesLevelMenu.java
package rl.sage.rangerlevels.gui.rewards;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Items;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import rl.sage.rangerlevels.capability.IPlayerRewards;
import rl.sage.rangerlevels.capability.PlayerRewardsProvider;
import rl.sage.rangerlevels.capability.RewardStatus;
import rl.sage.rangerlevels.config.RewardConfig;
import rl.sage.rangerlevels.gui.MenuItemBuilder;
import rl.sage.rangerlevels.gui.PlayerInfoUtils;
import rl.sage.rangerlevels.rewards.RewardManager;

public class PackagesLevelMenu {
    private static final int[] SLOT_INDICES = {
            10,11,12,13,14,15,16,
            19,20,21,22,23,24,25,
            28,29,30,31,32,33,34,
            37,38,39,40,41,42,43
    };
    private static final int SLOTS_PER_PAGE = SLOT_INDICES.length;

    public static void open(ServerPlayerEntity player) {
        open(player, 1);
    }

    public static void open(final ServerPlayerEntity player, int page) {
        IPlayerRewards cap = player
                .getCapability(PlayerRewardsProvider.REWARDS_CAP, null)
                .orElse(null);
        if (cap == null) return;

        // DEBUG
        System.out.println("[RewardsMenu][DEBUG] cap.getStatusMap() = " + cap.getStatusMap());

        // 1) Filtrar claves válidas PENDING de Packages (format “Packages:iv:nivel:ruta”)
        List<String> pending = new ArrayList<>();
        for (Entry<String, RewardStatus> e : cap.getStatusMap().entrySet()) {
            String key = e.getKey();
            String[] parts = key.split(":");
            if (parts.length == 4
                    && parts[0].equals("Packages")
                    && e.getValue() == RewardStatus.PENDING) {
                pending.add(key);
            }
        }

        // DEBUG
        System.out.println("[RewardsMenu][DEBUG] pendingPackages = " + pending);

        // 2) Ordenar por nivel numérico (parts[2]) y luego ruta (parts[3])
        Collections.sort(pending, new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                String[] pa = a.split(":");
                String[] pb = b.split(":");
                int lvlA = Integer.parseInt(pa[2]);
                int lvlB = Integer.parseInt(pb[2]);
                if (lvlA != lvlB) return lvlA - lvlB;
                return pa[3].compareTo(pb[3]);
            }
        });

        // 3) Paginación
        int total    = pending.size();
        int maxPages = Math.max(1, (int)Math.ceil(total / (double) SLOTS_PER_PAGE));
        if (page < 1) page = 1;
        if (page > maxPages) page = maxPages;

        // 4) Inventario y título
        Inventory inv = new Inventory(54);
        inv.clearContent();
        ITextComponent title = new StringTextComponent(
                "§6Recompensas por Paquetes §7(" + page + "/" + maxPages + ")"
        );

        // 5) Botones fijos
        inv.setItem(4,  PlayerInfoUtils.getInfoItem(player, 4));
        inv.setItem(49, MenuItemBuilder.createButton(
                "§aReclamar todas",
                Collections.singletonList("§7Haz clic para reclamar todas las recompensas pendientes"),
                Items.EMERALD_BLOCK, "claim_all", 49
        ));
        inv.setItem(53, MenuItemBuilder.createButton(
                "§cVolver",
                Collections.singletonList("§7Regresar al menú anterior"),
                Items.BARRIER, "back", 53
        ));

        // 6) Navegación
        if (page > 1) {
            inv.setItem(45, MenuItemBuilder.createButton(
                    "§e« Página " + (page - 1),
                    Collections.singletonList("§7Ir a la página anterior"),
                    Items.ARROW, "page:" + (page - 1), 45
            ));
        }
        if (page < maxPages) {
            inv.setItem(51, MenuItemBuilder.createButton(
                    "§ePágina " + (page + 1) + " »",
                    Collections.singletonList("§7Ir a la página siguiente"),
                    Items.ARROW, "page:" + (page + 1), 51
            ));
        }

        // 7) Rellenar ítems de recompensas
        int start = (page - 1) * SLOTS_PER_PAGE;
        int end   = Math.min(start + SLOTS_PER_PAGE, total);
        for (int i = start; i < end; i++) {
            String key = pending.get(i);
            String[] parts = key.split(":");
            String iv    = parts[1];
            String nivel = parts[2];
            String ruta  = parts[3];
            int slotIndex = SLOT_INDICES[i - start];
            String btnId = String.join(".", "Packages", iv, nivel, ruta, String.valueOf(page));

            inv.setItem(slotIndex, MenuItemBuilder.createButton(
                    "§ePaquete " + nivel + " → " + ruta,
                    Collections.singletonList("§7Haz clic para reclamar esta recompensa"),
                    Items.CHEST, btnId, slotIndex
            ));
        }

        // 8) Abrir contenedor
        INamedContainerProvider provider = new INamedContainerProvider() {
            @Override public ITextComponent getDisplayName() { return title; }
            @Override public Container createMenu(int windowId,
                                                  PlayerInventory playerInv,
                                                  PlayerEntity playerEntity) {
                return new PackagesLevelMenuContainer(windowId, playerInv, inv);
            }
        };
        player.openMenu(provider);
    }

    public static void claimSingle(ServerPlayerEntity player, String iv, String nivel, String ruta) {
        String key = String.join(":", "Packages", iv, nivel, ruta);
        @Nullable IPlayerRewards cap = player
                .getCapability(PlayerRewardsProvider.REWARDS_CAP, null)
                .orElse(null);
        if (cap == null) return;

        if (cap.getStatusMap().getOrDefault(key, RewardStatus.BLOCKED) == RewardStatus.PENDING) {
            cap.setStatus(key, RewardStatus.CLAIMED);
            RewardConfig.RouteRewards rr =
                    RewardConfig.get().Rewards.Packages.get(iv).get(ruta);
            RewardManager.executeRouteRewards(player, player.server, rr, "Packages");
            player.sendMessage(
                    new StringTextComponent("§aRecompensa paquete " + nivel + " (" + ruta + ") reclamada."),
                    player.getUUID()
            );
        } else {
            player.sendMessage(
                    new StringTextComponent("§cEsta recompensa no está pendiente."),
                    player.getUUID()
            );
        }
    }

    public static void claimAll(ServerPlayerEntity player) {
        @Nullable IPlayerRewards cap = player
                .getCapability(PlayerRewardsProvider.REWARDS_CAP, null)
                .orElse(null);
        if (cap == null) return;

        boolean anyClaimed = false;
        for (Map.Entry<String, RewardStatus> e : cap.getStatusMap().entrySet()) {
            String key = e.getKey();
            String[] parts = key.split(":");
            if (parts.length != 4 || !parts[0].equals("Packages") || e.getValue() != RewardStatus.PENDING)
                continue;

            String iv    = parts[1];
            String nivel = parts[2];
            String ruta  = parts[3];
            claimSingle(player, iv, nivel, ruta);
            anyClaimed = true;
        }

        if (!anyClaimed) {
            player.sendMessage(
                    new StringTextComponent("§cNo hay recompensas de paquete pendientes que puedas reclamar."),
                    player.getUUID()
            );
        }
    }
}
