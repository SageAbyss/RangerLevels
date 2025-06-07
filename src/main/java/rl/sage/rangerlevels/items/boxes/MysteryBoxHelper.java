// src/main/java/rl/sage/rangerlevels/items/boxes/MysteryBoxHelper.java
package rl.sage.rangerlevels.items.boxes;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.registries.ForgeRegistries;
import rl.sage.rangerlevels.capability.LevelProvider;
import rl.sage.rangerlevels.config.MysteryBoxesConfig;
import rl.sage.rangerlevels.config.MysteryBoxesConfig.MysteryBoxConfig.TierBoxConfig;
import rl.sage.rangerlevels.config.MysteryBoxesConfig.MysteryBoxConfig.CommandEntry;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.items.ItemsHelper;

import java.util.*;

public class MysteryBoxHelper {
    private static final Random RNG = new Random();

    /**
     * Abre la caja y ejecuta TODO lo configurado en MysteryBoxesConfig.yml para ese tier.
     */
    public static void open(ServerPlayerEntity player, String boxId, EventType event) {
        MysteryBoxesConfig.MysteryBoxConfig cfg = MysteryBoxesConfig.get().mysteryBox;
        TierBoxConfig c;
        switch(boxId) {
            case MysteryBoxComun.ID:      c = cfg.comun;      break;
            case MysteryBoxRaro.ID:       c = cfg.raro;       break;
            case MysteryBoxEpico.ID:      c = cfg.epico;      break;
            case MysteryBoxLegendario.ID: c = cfg.legendario; break;
            case MysteryBoxEstelar.ID:    c = cfg.estelar;    break;
            case MysteryBoxMitico.ID:     c = cfg.mitico;     break;
            default: return;
        }
        if (!c.enable) return;

        // 1) Dar ítems aleatorios ponderados + no-drop
        int count = c.randomItemMin + RNG.nextInt(c.randomItemMax - c.randomItemMin + 1);
        List<String> rewards = ItemsHelper.getRewardItemIds();

        for (int i = 0; i < count; i++) {
            // Construir pesos: NONE vs cada ítem de tu mod
            LinkedHashMap<String, Double> weights = new LinkedHashMap<>();
            if (c.randomItemsNoDropChance > 0) {
                weights.put("NONE", c.randomItemsNoDropChance);
            }
            double itemWeight = (100.0 - c.randomItemsNoDropChance) / rewards.size();
            for (String itemId : rewards) {
                weights.put(itemId, itemWeight);
            }

            // Tirada ponderada
            double total = weights.values().stream().mapToDouble(d -> d).sum();
            double roll  = RNG.nextDouble() * total;
            double cum   = 0;
            for (Map.Entry<String, Double> e : weights.entrySet()) {
                cum += e.getValue();
                if (roll <= cum) {
                    String key = e.getKey();
                    if (!"NONE".equals(key)) {
                        player.addItem(CustomItemRegistry.create(key, 1));
                        player.sendMessage(
                                new StringTextComponent(
                                        TextFormatting.AQUA + "✦ ¡Has recibido " +
                                                CustomItemRegistry.create(key, 1)
                                                        .getHoverName().getString() + "!"
                                ),
                                player.getUUID()
                        );
                    }
                    break;
                }
            }
        }


        // 2) Comandos configurados
        for (CommandEntry ce : c.commands) {
            if (RNG.nextDouble() * 100 < ce.chancePercent) {
                String cmd = ce.command.replace("%player%", player.getName().getString());
                player.getServer().getCommands().performCommand(
                        player.createCommandSourceStack(), cmd);
            }
        }

        // 3) Upgrade box (con nombre estilizado en lugar de ID)
        if (c.upgradeBoxChance > 0 && RNG.nextDouble() * 100 < c.upgradeBoxChance) {
            List<String> upgrades = c.boxesUpgrade;
            if (upgrades != null && !upgrades.isEmpty()) {
                String nextId = upgrades.get(RNG.nextInt(upgrades.size()));
                if (CustomItemRegistry.contains(nextId)) {
                    ItemStack nextStack = CustomItemRegistry.create(nextId, 1);
                    player.addItem(nextStack);
                    // Obtenemos el nombre con estilo del item
                    ITextComponent nameComp = nextStack.getHoverName();
                    player.sendMessage(
                            new StringTextComponent(TextFormatting.GREEN + "¡Tu caja misteriosa se ha transformado en ")
                                    .append(nameComp)
                                    .append(new StringTextComponent(TextFormatting.GREEN + "!")),
                            player.getUUID()
                    );
                } else {
                    RangerLevels.LOGGER.warn("Intento de upgrade a caja desconocida: {}", nextId);
                }
            }
        }

        // 4) Mensaje de apertura con nombre estilizado (gradient) en lugar de ID
        //    Creamos un ItemStack temporal para obtener su nombre (con estilo)
        ItemStack boxStack = CustomItemRegistry.create(boxId, 1);
        ITextComponent nameComp = boxStack.getHoverName();
        player.sendMessage(
                new StringTextComponent(TextFormatting.GOLD + "✦ ¡Has abierto una ")
                        .append(nameComp)
                        .append(new StringTextComponent(TextFormatting.GOLD + "!")),
                player.getUUID()
        );

        // 5) Rango de Exp
        int expGain = c.expMin + RNG.nextInt(c.expMax - c.expMin + 1);
        LevelProvider.giveExpAndNotify(player, expGain);

        // 6) Recompensas de items configuradas
        giveConfiguredRewards(c, player);

        // 7) Intento de drop de UNA caja adicional (o ninguna)
        tryDropOneOnEvent(player, event, c);
    }

    public static void giveConfiguredRewards(TierBoxConfig c, ServerPlayerEntity player) {
        Map<String, Integer> itemsChance = c.itemsChance;
        if (itemsChance == null || itemsChance.isEmpty()) return;

        // 1) Suma de pesos y tirada
        int totalWeight = itemsChance.values().stream().mapToInt(i -> i).sum();
        int roll = RNG.nextInt(totalWeight);
        int cumulative = 0;

        for (Map.Entry<String, Integer> entry : itemsChance.entrySet()) {
            cumulative += entry.getValue();
            if (roll < cumulative) {
                String rawKey = entry.getKey();            // p.ej. "IRON_BLOCK" o "PIXELMON_MASTER_BALL"
                String modid, path;

                // 2) Decidir namespace y path
                if (rawKey.startsWith("PIXELMON_")) {
                    modid = "pixelmon";
                    path = rawKey.substring("PIXELMON_".length()).toLowerCase(Locale.ROOT);
                } else {
                    modid = "minecraft";
                    path = rawKey.toLowerCase(Locale.ROOT);
                }

                ResourceLocation rl = new ResourceLocation(modid, path);
                Item item = ForgeRegistries.ITEMS.getValue(rl);

                if (item == null) {
                    // Si no existe, avisa y sigue al siguiente
                    RangerLevels.LOGGER.warn("Item desconocido en giveConfiguredRewards: {}", rl);
                    continue;
                }

                // 3) Crea y entrega el ItemStack
                ItemStack stack = new ItemStack(item, 1);
                player.addItem(stack);
                break;
            }
        }
    }


    public enum EventType {
        BEAT_BOSS,
        LEVEL_UP,
        RAID,
        HUNDRED,
        OPEN_BOX_BLOCK
    }

    /**
     * Intenta soltar UNA caja basada en todos los tiers + la probabilidad de NO DROP
     * del tier de la caja abierta (sourceTier.noDropChance).
     */
    public static void tryDropOneOnEvent(ServerPlayerEntity player, EventType event, TierBoxConfig sourceTier) {
        MysteryBoxesConfig.MysteryBoxConfig cfg = MysteryBoxesConfig.get().mysteryBox;

        // 0) Peso de NO DROP según el tier de la caja abierta
        LinkedHashMap<String, Double> weights = new LinkedHashMap<>();
        if (sourceTier.noDropChance > 0) {
            weights.put("NONE", sourceTier.noDropChance);
        }

        // 1) Pesos de todas las cajas disponibles para este evento
        addWeight(cfg.comun,      MysteryBoxComun.ID,      event, weights);
        addWeight(cfg.raro,       MysteryBoxRaro.ID,       event, weights);
        addWeight(cfg.epico,      MysteryBoxEpico.ID,      event, weights);
        addWeight(cfg.legendario, MysteryBoxLegendario.ID, event, weights);
        addWeight(cfg.estelar,    MysteryBoxEstelar.ID,    event, weights);
        addWeight(cfg.mitico,     MysteryBoxMitico.ID,     event, weights);

        // 2) Totalizar y salir si no hay nada
        double total = weights.values().stream().mapToDouble(d -> d).sum();
        if (total <= 0) return;

        // 3) Tirada ponderada
        double roll = RNG.nextDouble() * total;
        double cumulative = 0;
        for (Map.Entry<String, Double> entry : weights.entrySet()) {
            cumulative += entry.getValue();
            if (roll <= cumulative) {
                String key = entry.getKey();
                if ("NONE".equals(key)) {
                    // No drop: terminamos sin dar nada
                    return;
                }
                // Damos la caja seleccionada
                ItemStack stack = CustomItemRegistry.create(key, 1);
                player.addItem(stack);
                String displayName = stack.getHoverName().getString();
                player.sendMessage(
                        new StringTextComponent(
                                TextFormatting.GOLD + "✦ ¡Has recibido x1 " + displayName
                                        + " por " + describeEvent(event) + "!"
                        ),
                        player.getUUID()
                );
                return;
            }
        }
    }

    private static void addWeight(TierBoxConfig c, String boxId, EventType event,
                                  LinkedHashMap<String, Double> weights) {
        if (!c.enable) return;
        double chance;
        switch (event) {
            case BEAT_BOSS:  chance = c.dropChanceBeatBoss;  break;
            case LEVEL_UP:   chance = c.dropChanceLevelUp;   break;
            case RAID:       chance = c.dropChanceRaid;      break;
            case HUNDRED:    chance = 100.0;                  break;
            default:         return;
        }
        if (chance > 0) {
            weights.put(boxId, chance);
        }
    }

    private static String describeEvent(EventType event) {
        switch (event) {
            case BEAT_BOSS:       return "derrotar un jefe";
            case LEVEL_UP:        return "subir de nivel";
            case RAID:            return "completar una raid";
            case HUNDRED:         return "evento especial";
            case OPEN_BOX_BLOCK:  return "abrir una caja misteriosa";
            default:              return "un evento";
        }
    }
}
