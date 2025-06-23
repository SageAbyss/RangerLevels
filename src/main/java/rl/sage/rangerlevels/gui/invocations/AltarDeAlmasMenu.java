package rl.sage.rangerlevels.gui.invocations;

import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.gui.MenuItemBuilder;
import rl.sage.rangerlevels.gui.PlayerInfoUtils;
import rl.sage.rangerlevels.util.EnchantUtils;
import rl.sage.rangerlevels.util.NBTUtils;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

import java.util.Arrays;
import java.util.Collections;

public class AltarDeAlmasMenu {


    private static ItemStack decorateGlowy(ItemStack stack) {
        EnchantUtils.addEnchantment(stack, Enchantments.UNBREAKING, 1);
        CompoundNBT tag = stack.getOrCreateTag();
        NBTUtils.applyAllHideFlags(tag);
        stack.setTag(tag);
        return stack;
    }

    public static void open(ServerPlayerEntity player) {
        // sonido al abrir
        PlayerSoundUtils.playSoundToPlayer(
                player,
                SoundEvents.NOTE_BLOCK_CHIME,
                SoundCategory.MASTER,
                1.0f,
                1.0f
        );

        // Inventario de 6x9 (54 slots)
        Inventory inv = new Inventory(54);
        inv.clearContent();

        // Slot 4: info del jugador
        inv.setItem(4, PlayerInfoUtils.getInfoItem(player, 4));



        // Altar: Sacrificio de Almas Menor (slot 10)
        ItemStack ivs = MenuItemBuilder.createButton(
                "§7◈ Altar de Almas: §3Modificador de IVs",
                Arrays.asList(
                        "§7Ingredientes requeridos:",
                        " §f• x4 Esencias Legendarias | UltraEntes",
                        " §f• x1 Estrella del Nether",
                        " §f• x1 Núcleo de Sacrificio"
                ),
                PixelmonItems.legendary_clues,
                "modificadorIVS",
                10
        );
        inv.setItem(10, decorateGlowy(ivs));

        // Altar: Sacrificio de Almas Mayor (slot 12)
        ItemStack shiny = MenuItemBuilder.createButton(
                "§7◈ Altar de Almas: §eModificador Shiny",
                Arrays.asList(
                        "§7Ingredientes requeridos:",
                        " §f• x3 Esencias Legendarias | UltraEntes",
                        " §f• x16 Tintes Amarillos",
                        " §f• x1 Núcleo de Sacrificio"
                ),
                PixelmonItems.legendary_clues,
                "modificadorShiny",
                12
        );
        inv.setItem(12, decorateGlowy(shiny));

        ItemStack tamano = MenuItemBuilder.createButton(
                "§7◈ Altar de Almas: §cModificador de Tamaño",
                Arrays.asList(
                        "§7Ingredientes requeridos:",
                        " §f• x3 Esencias Legendarias | UltraEntes",
                        " §f• x64 Polvo de Hueso",
                        " §f• x1 Núcleo de Sacrificio"
                ),
                PixelmonItems.legendary_clues,
                "modificadorTamaño",
                14
        );
        inv.setItem(14, decorateGlowy(tamano));

        ItemStack naturaleza = MenuItemBuilder.createButton(
                "§7◈ Altar de Almas: §bModificador de Naturaleza",
                Arrays.asList(
                        "§7Ingredientes requeridos:",
                        " §f• x3 Esencias Legendarias | UltraEntes",
                        " §f• x1 Menta",
                        " §f• x1 Núcleo de Sacrificio"
                ),
                PixelmonItems.legendary_clues,
                "modificadorNatu",
                16
        );
        inv.setItem(16, decorateGlowy(naturaleza));

        ItemStack concentrado = MenuItemBuilder.createButton(
                "§7◈ Altar de Almas: §5Concentrado de Almas",
                Arrays.asList(
                        "§7Ingredientes requeridos:",
                        " §f• x5 Esencias Legendarias | UltraEntes",
                        " §f• x1 Estrellas del Nether",
                        " §f• x2 Núcleo de Sacrificio",
                        "§7Segunda opción: ",
                        " §f• x64 Esencias de Jefes",
                        " §f• x1 Estrellas del Nether",
                        " §f• x2 Núcleo de Sacrificio"
                ),
                PixelmonItems.intriguing_stone,
                "concentrado",
                20
        );
        inv.setItem(20, decorateGlowy(concentrado));

        ItemStack nucleo = MenuItemBuilder.createButton(
                "§7◈ Altar de Almas: §cNúcleo de Sacrificio",
                Arrays.asList(
                        "§7Ingredientes requeridos:",
                        " §f• x1 Esencias Legendarias | UltraEntes",
                        " §f• x1 Roca|Boulder (Pixelmon)",
                        " ",
                        " §f→ Puede obtenerse en Cajas Misteriosas"

                ),
                PixelmonItems.dark_stone,
                "nucleo",
                24
        );
        inv.setItem(24, decorateGlowy(nucleo));

        ItemStack ivsUni = MenuItemBuilder.createButton(
                "§7◈ Altar de Almas: §3Modificador de IVs Universal",
                Arrays.asList(
                        "§7Ingredientes requeridos:",
                        " §f• x4 Esencias Legendarias | UltraEntes",
                        " §f• x1 Estrella del Nether",
                        " §f• x1 Núcleo de Sacrificio"
                ),
                PixelmonItems.legendary_clues,
                "modificadorIVSUni",
                28
        );
        inv.setItem(28, decorateGlowy(ivsUni));

        ItemStack shinyUni = MenuItemBuilder.createButton(
                "§7◈ Altar de Almas: §eModificador Shiny Universal",
                Arrays.asList(
                        "§7Ingredientes requeridos:",
                        " §f• x6 Esencias Legendarias | UltraEntes",
                        " §f• x16 Tintes Amarillos",
                        " §f• x1 Núcleo de Sacrificio"
                ),
                PixelmonItems.legendary_clues,
                "modificadorShiny",
                30
        );
        inv.setItem(30, decorateGlowy(shinyUni));

        ItemStack tamanoUni = MenuItemBuilder.createButton(
                "§7◈ Altar de Almas: §cModificador de Tamaño Universal",
                Arrays.asList(
                        "§7Ingredientes requeridos:",
                        " §f• x6 Esencias Legendarias | UltraEntes",
                        " §f• x64 Polvo de Hueso",
                        " §f• x1 Núcleo de Sacrificio"
                ),
                PixelmonItems.legendary_clues,
                "modificadorTamaño",
                32
        );
        inv.setItem(32, decorateGlowy(tamanoUni));

        ItemStack naturalezaUni = MenuItemBuilder.createButton(
                "§7◈ Altar de Almas: §bModificador de Naturaleza Universal",
                Arrays.asList(
                        "§7Ingredientes requeridos:",
                        " §f• x6 Esencias Legendarias | UltraEntes",
                        " §f• x1 Menta",
                        " §f• x1 Núcleo de Sacrificio"
                ),
                PixelmonItems.legendary_clues,
                "modificadorNatu",
                34
        );
        inv.setItem(34, decorateGlowy(naturalezaUni));

        // Botón Volver (slot 53)
        ItemStack botonBack = MenuItemBuilder.createButton(
                "§cVolver",
                Collections.singletonList("§6Regresa al menú principal"),
                Items.ARROW,
                "back",
                53
        );
        inv.setItem(53, botonBack);

        // Abrir contenedor
        player.openMenu(new SimpleNamedContainerProvider(
                (windowId, playerInv, p) -> new AltarDeAlmasMenuContainer(windowId, playerInv, inv),
                new StringTextComponent("§6Altar de Almas")
        ));
    }

}
