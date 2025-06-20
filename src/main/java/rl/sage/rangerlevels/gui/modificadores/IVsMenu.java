// File: rl/sage/rangerlevels/gui/modificadores/IVsMenu.java
package rl.sage.rangerlevels.gui.modificadores;

import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.gui.MenuItemBuilder;
import rl.sage.rangerlevels.util.EnchantUtils;
import rl.sage.rangerlevels.util.NBTUtils;
import rl.sage.rangerlevels.util.PlayerSoundUtils;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;

import java.util.Arrays;
import java.util.Collections;

public class IVsMenu {
    private static ItemStack decorateGlowy(ItemStack stack) {
        CompoundNBT tag = stack.getOrCreateTag();
        NBTUtils.applyAllHideFlags(tag);
        stack.setTag(tag);
        return stack;
    }
    public static void open(ServerPlayerEntity player) {
        PlayerSoundUtils.playSoundToPlayer(player, SoundEvents.NOTE_BLOCK_PLING, SoundCategory.MASTER, 1f, 1f);

        Inventory inv = new Inventory(27);
        inv.clearContent();

        // Acción: Subir a 31 (slot 11) y Bajar a 0 (slot 15)
        inv.setItem(11, decorateGlowy(MenuItemBuilder.createButton(
                "§aSubir a 31",
                Collections.singletonList("§7Haz clic para subir el IV a 31"),
                PixelmonItems.green_shard,
                "up",
                11
        )));
        inv.setItem(15, decorateGlowy(MenuItemBuilder.createButton(
                "§cBajar a 0",
                Collections.singletonList("§7Haz clic para bajar el IV a 0"),
                PixelmonItems.red_shard,
                "down",
                15
        )));

        // Stat HP en slot 2
        inv.setItem(2, decorateGlowy(MenuItemBuilder.createButton(
                "§aHP",
                Collections.singletonList("§7Selecciona HP para modificar"),
                PixelmonItems.grass_gem,
                "HP",
                2
        )));

        // Stat ATTACK en slot 4
        inv.setItem(4, decorateGlowy(MenuItemBuilder.createButton(
                "§cATTACK",
                Collections.singletonList("§7Selecciona Attack para modificar"),
                PixelmonItems.fire_gem,
                "ATTACK",
                4
        )));

        // Stat DEFENSE en slot 6
        inv.setItem(6, decorateGlowy(MenuItemBuilder.createButton(
                "§6DEFENSE",
                Collections.singletonList("§7Selecciona Defense para modificar"),
                PixelmonItems.ground_gem,
                "DEFENSE",
                6
        )));

        // Stat SP_ATTACK en slot 20
        inv.setItem(20, decorateGlowy(MenuItemBuilder.createButton(
                "§dSP_ATTACK",
                Collections.singletonList("§7Selecciona Sp. Attack para modificar"),
                PixelmonItems.ghost_gem,
                "SP_ATTACK",
                20
        )));

        // Stat SP_DEFENSE en slot 22
        inv.setItem(22, decorateGlowy(MenuItemBuilder.createButton(
                "§5SP_DEFENSE",
                Collections.singletonList("§7Selecciona Sp. Defense para modificar"),
                PixelmonItems.fighting_gem,
                "SP_DEFENSE",
                22
        )));

        // Stat SPEED en slot 24
        inv.setItem(24, decorateGlowy(MenuItemBuilder.createButton(
                "§bSPEED",
                Collections.singletonList("§7Selecciona Speed para modificar"),
                PixelmonItems.water_gem,
                "SPEED",
                24
        )));

        // Cerrar en slot 26
        inv.setItem(26, MenuItemBuilder.createButton(
                "§cCerrar",
                Collections.singletonList("§7Sin cambios"),
                net.minecraft.item.Items.BARRIER,
                "close",
                26
        ));

        inv.setItem(9, decorateGlowy(MenuItemBuilder.createButton(
                "§6¿Cómo seleccionar?",
                Arrays.asList("§7• Selecciona primero SUBIR o BAJAR IVs", "§7• Después selecciona el IV a modificar"),
                PixelmonItems.quest_editor,
                "quest",
                9
        )));

        player.openMenu(new SimpleNamedContainerProvider(
                (wid, pinv, pl) -> new IVsMenuContainer(wid, pinv, inv),
                new StringTextComponent("✦ Modificador IVs ✦")
        ));
    }
}
