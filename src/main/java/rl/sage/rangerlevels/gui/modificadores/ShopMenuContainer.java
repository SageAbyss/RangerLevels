// File: rl/sage/rangerlevels/gui/modificadores/ShopMenuContainer.java
package rl.sage.rangerlevels.gui.modificadores;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.config.ShopConfig;
import rl.sage.rangerlevels.config.ShopRotationManager;
import rl.sage.rangerlevels.config.ShopState;
import rl.sage.rangerlevels.gui.BaseMenuContainer6;
import rl.sage.rangerlevels.gui.MainMenu;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.items.RangerItemDefinition;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

import java.util.List;
import java.util.UUID;

public class ShopMenuContainer extends BaseMenuContainer6 {

    public ShopMenuContainer(int windowId, PlayerInventory playerInv, Inventory menuInventory) {
        super(windowId, playerInv, menuInventory);
    }

    @Override
    protected void handleButtonClick(String buttonId, ServerPlayerEntity player) {
        List<String> sel = ShopRotationManager.getCurrentSelection();
        if (buttonId.startsWith("shop_select_")) {
            int idx = Integer.parseInt(buttonId.substring("shop_select_".length()));
            if (idx < sel.size()) {
                processPurchase(player, sel.get(idx), ShopConfig.get().cost.entries);
            }
        } else if ("shop_random".equals(buttonId)) {
            if (sel.isEmpty()) {
                player.sendMessage(new StringTextComponent("§cTienda vacía."), player.getUUID());
            } else {
                String raw = sel.get(player.getRandom().nextInt(sel.size()));
                processPurchase(player, raw, ShopConfig.get().cost.randomEntries);
            }
        } else if ("shop_back".equals(buttonId)) {
            player.closeContainer();
            MainMenu.open(player);
        }
    }

    private void processPurchase(ServerPlayerEntity player, String raw, List<ShopConfig.CostConfig.CostEntry> costEntries) {
        ShopConfig cfg = ShopConfig.get();
        UUID uuid = player.getUUID();
        ShopState state = ShopState.get();

        // — 1) Un solo legendario por rotación —
        if (state.purchasedPlayers.contains(uuid)) {
            player.sendMessage(new StringTextComponent("§cYa compraste un legendario en esta rotación."), uuid);
            PlayerSoundUtils.playSoundToPlayer(player, SoundEvents.VILLAGER_NO, SoundCategory.MASTER, 1f, 1f);
            return;
        }

        // — 2) Verificar costes (custom o vanilla) —
        boolean ok = true;
        for (ShopConfig.CostConfig.CostEntry ce : costEntries) {
            int have = 0;
            // Custom:
            if (CustomItemRegistry.contains(ce.item)) {
                for (ItemStack s : player.inventory.items) {
                    if (!s.isEmpty() && ce.item.equals(RangerItemDefinition.getIdFromStack(s))) {
                        have += s.getCount();
                    }
                }
            } else {
                // Vanilla:
                ResourceLocation rl = ShopConfig.resolveItemLocation(ce.item);
                for (ItemStack s : player.inventory.items) {
                    if (!s.isEmpty() && rl.equals(s.getItem().getRegistryName())) {
                        have += s.getCount();
                    }
                }
            }
            if (have < ce.amount) {
                ok = false;
                break;
            }
        }
        if (!ok) {
            player.sendMessage(new StringTextComponent(cfg.messages.noEssence), player.getUUID());
            PlayerSoundUtils.playSoundToPlayer(player, SoundEvents.VILLAGER_NO, SoundCategory.MASTER, 1f, 1f);
            return;
        }

        // — 3) Consumir costes —
        for (ShopConfig.CostConfig.CostEntry ce : costEntries) {
            int rem = ce.amount;
            if (CustomItemRegistry.contains(ce.item)) {
                for (ItemStack s : player.inventory.items) {
                    if (rem <= 0) break;
                    if (!s.isEmpty() && ce.item.equals(RangerItemDefinition.getIdFromStack(s))) {
                        int cnt = s.getCount();
                        if (cnt <= rem) {
                            rem -= cnt;
                            s.setCount(0);
                        } else {
                            s.shrink(rem);
                            rem = 0;
                        }
                    }
                }
            } else {
                ResourceLocation rl = ShopConfig.resolveItemLocation(ce.item);
                for (ItemStack s : player.inventory.items) {
                    if (rem <= 0) break;
                    if (!s.isEmpty() && rl.equals(s.getItem().getRegistryName())) {
                        int cnt = s.getCount();
                        if (cnt <= rem) {
                            rem -= cnt;
                            s.setCount(0);
                        } else {
                            s.shrink(rem);
                            rem = 0;
                        }
                    }
                }
            }
        }

        // — 4) Entregar Pokémon con pokegive —
        int level = cfg.pokemonLevel;
        String speciesArg = raw.contains("-") || !raw.equals(raw.toLowerCase())
                ? "\"" + raw + "\""
                : raw;
        String cmd = String.format("pokegive %s %s lvl:%d",
                player.getName().getString(),
                speciesArg,
                level);
        RangerLevels.LOGGER.debug("Ejecutando comando: {}", cmd);
        try {
            CommandSource src = player.createCommandSourceStack().withPermission(2);
            player.getServer().getCommands().performCommand(src, cmd);
        } catch (Exception e) {
            RangerLevels.LOGGER.error("Error al ejecutar pokegive para '{}': {}", raw, e.getMessage());
            player.sendMessage(new StringTextComponent(cfg.messages.error), player.getUUID());
            PlayerSoundUtils.playSoundToPlayer(player, SoundEvents.VILLAGER_NO, SoundCategory.MASTER, 1f, 1f);
            return;
        }

        // — 5) Registrar compra y feedback —
        state.purchasedPlayers.add(uuid);
        ShopState.save();

        player.sendMessage(new StringTextComponent(
                cfg.messages.bought.replace("%pokemon%", capitalize(raw))
        ), player.getUUID());
        PlayerSoundUtils.playSoundToPlayer(player, SoundEvents.PLAYER_LEVELUP, SoundCategory.MASTER, 1f, 1f);
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
