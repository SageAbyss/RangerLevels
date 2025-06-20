package rl.sage.rangerlevels.gui.modificadores;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.api.util.helpers.SpriteItemHelper;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.config.ShopConfig;
import rl.sage.rangerlevels.config.ShopRotationManager;
import rl.sage.rangerlevels.gui.MenuItemBuilder;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

import java.util.*;

public class ShopMenu {

    public static void open(ServerPlayerEntity player) {
        ShopConfig cfg = ShopConfig.get();

        PlayerSoundUtils.playSoundToPlayer(player,
                net.minecraft.util.SoundEvents.NOTE_BLOCK_CHIME,
                net.minecraft.util.SoundCategory.MASTER,
                1f, 1f);

        Inventory inv = new Inventory(cfg.menu.size);
        inv.clearContent();

        // — Tiempo restante —
        String tiempoRest = ShopRotationManager.getTimeRemaining();
        inv.setItem(
                cfg.menu.slots.timeDisplay,
                createIcon(cfg.menu.icons.time,
                        "§eTiempo restante",
                        Collections.singletonList("§7" + tiempoRest))
        );

        // — Botones de selección —
        List<String> selection = ShopRotationManager.getCurrentSelection();
        for (int i = 0; i < Math.min(selection.size(), cfg.menu.slots.items.size()); i++) {
            String raw = selection.get(i);
            ItemStack icon = getLegendSprite(raw);
            String displayName = "§6" + capitalize(raw);
            List<String> lore = Collections.singletonList(
                    "§7Costo: " + buildCostLore(ShopConfig.get().cost.entries)
            );
            inv.setItem(cfg.menu.slots.items.get(i),
                    MenuItemBuilder.decorateWithNameAndLore(
                            icon,
                            displayName,
                            lore,
                            "shop_select_" + i,
                            cfg.menu.slots.items.get(i)
                    )
            );
        }

        // — Random —
        String loreRand = buildCostLore(ShopConfig.get().cost.randomEntries);
        inv.setItem(cfg.menu.slots.randomButton,
                MenuItemBuilder.decorateWithNameAndLore(
                        createIcon(cfg.menu.icons.random,
                                "§aComprar al azar",
                                Collections.singletonList("§7Costo: " + loreRand)),
                        "§aRandom",
                        Collections.singletonList("§7Costo: " + loreRand),
                        "shop_random",
                        cfg.menu.slots.randomButton
                )
        );

        // — Volver —
        inv.setItem(cfg.menu.slots.backButton,
                MenuItemBuilder.decorateWithNameAndLore(
                        createIcon(cfg.menu.icons.back,
                                "§cVolver",
                                Collections.singletonList("§6Regresa al menú")),
                        "§cVolver",
                        Collections.singletonList("§6Regresa al menú"),
                        "shop_back",
                        cfg.menu.slots.backButton
                )
        );

        player.openMenu(new SimpleNamedContainerProvider(
                (id, plInv, pl) -> new ShopMenuContainer(id, plInv, inv),
                new TranslationTextComponent(cfg.menu.title)
        ));
    }

    /**
     * Construye “cantidad NombreLegible” para cada entrada,
     * soportando custom items del CustomItemRegistry.
     */
    private static String buildCostLore(List<ShopConfig.CostConfig.CostEntry> entries) {
        List<String> parts = new ArrayList<>();
        for (ShopConfig.CostConfig.CostEntry ce : entries) {
            String displayName;
            // --- Custom item? ---
            if (CustomItemRegistry.contains(ce.item)) {
                ItemStack tmp = CustomItemRegistry.create(ce.item, 1);
                displayName = tmp.getHoverName().getString();
            } else {
                // Fallback vanilla:
                try {
                    ResourceLocation rl = ShopConfig.resolveItemLocation(ce.item);
                    Item item = ForgeRegistries.ITEMS.getValue(rl);
                    if (item == null || item == Items.AIR) throw new IllegalStateException();
                    ItemStack tmp = new ItemStack(item);
                    displayName = tmp.getHoverName().getString();
                } catch (Exception e) {
                    RangerLevels.LOGGER.warn("buildCostLore: ítem no válido '{}'", ce.item, e);
                    displayName = ce.item;
                }
            }
            parts.add(ce.amount + " " + displayName);
        }
        return String.join(", ", parts);
    }

    private static ItemStack createIcon(String registryName, String name, List<String> lore) {
        ResourceLocation rl = ShopConfig.resolveItemLocation(registryName);
        Item item = ForgeRegistries.ITEMS.getValue(rl);
        if (item == null) {
            RangerLevels.LOGGER.warn("Ícono no encontrado para '{}'", registryName);
            item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft","barrier"));
        }
        return MenuItemBuilder.decorateWithNameAndLore(new ItemStack(item), name, lore);
    }

    /**
     * Genera el sprite real usando SpriteItemHelper:
     * 1) Obtiene Species base desde raw.
     * 2) Crea un Pokemon con nivel deseado.
     * 3) Usa SpriteItemHelper.getPhoto(...) para generar el ItemStack con sprite.
     * 4) Llama a SpriteItemHelper.updateSprite(...) para completar datos en servidor.
     */
    private static ItemStack getLegendSprite(String raw) {
        try {
            Optional<Species> optSpecies = getBaseSpecies(raw);
            if (!optSpecies.isPresent()) {
                RangerLevels.LOGGER.warn("getLegendSprite: especie no encontrada para '{}'", raw);
                return fallbackIcon();
            }
            Species species = optSpecies.get();

            // Crear Pokémon con nivel
            Pokemon pkm = PokemonFactory.create(species);
            if (pkm == null) {
                RangerLevels.LOGGER.warn("getLegendSprite: no se pudo crear Pokémon para '{}'", species.getName());
                return fallbackIcon();
            }
            pkm.setLevel(ShopConfig.get().pokemonLevel);
            // Si quieres shiny: pkm.setShiny(true);

            // Obtener la “foto”/sprite con todos los datos que Pixelmon espera
            ItemStack photo = SpriteItemHelper.getPhoto(pkm);
            // En servidor, actualizar el sprite por si faltan campos
            SpriteItemHelper.updateSprite(photo);

            RangerLevels.LOGGER.debug("getLegendSprite: raw='{}', NBT={}", raw, photo.getTag());
            return photo;
        } catch (Exception e) {
            RangerLevels.LOGGER.warn("getLegendSprite: excepción para '{}': {}", raw, e.getMessage());
            return fallbackIcon();
        }
    }

    private static Optional<Species> getBaseSpecies(String raw) {
        if (raw == null || raw.isEmpty()) return Optional.empty();
        String base = raw.split("-")[0].toLowerCase(Locale.ROOT);
        String specName = Character.toUpperCase(base.charAt(0)) + base.substring(1);
        Optional<Species> opt = PixelmonSpecies.fromName(specName).getValue();
        if (opt.isPresent()) return opt;
        // Si PixelmonSpecies no reconoce directamente, prueba con strippedName
        for (Species s : PixelmonSpecies.getAll()) {
            if (s.getStrippedName().equalsIgnoreCase(specName)) {
                return Optional.of(s);
            }
        }
        return Optional.empty();
    }

    private static ItemStack fallbackIcon() {
        ResourceLocation rl = ShopConfig.resolveItemLocation(ShopConfig.get().menu.icons.legendGeneric);
        Item item = ForgeRegistries.ITEMS.getValue(rl);
        return new ItemStack(item != null ? item : ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft","barrier")));
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
