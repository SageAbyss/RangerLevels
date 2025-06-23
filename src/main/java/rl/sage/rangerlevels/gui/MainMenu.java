package rl.sage.rangerlevels.gui;

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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.util.EnchantUtils;
import rl.sage.rangerlevels.util.NBTUtils;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

import java.util.Arrays;

/**
 * Abre el “Main Menu” (3 filas x 9 columnas = 27 slots).
 * Coloca en cada posición los botones con su NBT “MenuButtonID” y “MenuSlot”.
 */
public class MainMenu {
    private static ItemStack decorateGlowy(ItemStack stack) {
        CompoundNBT tag = stack.getOrCreateTag();
        NBTUtils.applyAllHideFlags(tag);
        stack.setTag(tag);
        return stack;
    }

    public static void open(ServerPlayerEntity player) {
        // Reproducir sonido al abrir
        PlayerSoundUtils.playSoundToPlayer(
                player,
                SoundEvents.NOTE_BLOCK_CHIME,
                SoundCategory.MASTER,
                1.0f,
                0.8f
        );

        // 1) Inventario interno de 27 slots
        Inventory inv = new Inventory(27);
        inv.clearContent();

        // – Info (slot 10)
        inv.setItem(10, PlayerInfoUtils.getInfoItem(player, 10));

        // – Recompensas (slot 12)
        inv.setItem(12, MenuItemBuilder.createButton(
                "§bRecompensas",
                Arrays.asList("§7Haz clic para ver tus recompensas"),
                Items.ENDER_CHEST,
                "rewards",
                12
        ));

        // – Ayuda (slot 14)
        inv.setItem(14, decorateGlowy(MenuItemBuilder.createButton(
                "§eAyuda",
                Arrays.asList("§7Información sobre RangerLevels"),
                PixelmonItems.quest_editor,
                "help",
                14
        )));

        // – Comprar Pase (slot 16)
        inv.setItem(16, MenuItemBuilder.createButton(
                "§6Tienda Rotativa",
                Arrays.asList("§7Haz clic para ver la rotación actual"),
                PixelmonItems.coin_case,
                "shop",
                16
        ));

        ITextComponent title = new StringTextComponent("§6෴҉ Ranger Levels ҉෴");
        player.openMenu(new SimpleNamedContainerProvider(
                (windowId, playerInv, p) -> new MainMenuContainer(windowId, playerInv, inv),
                title
        ));
    }
}
