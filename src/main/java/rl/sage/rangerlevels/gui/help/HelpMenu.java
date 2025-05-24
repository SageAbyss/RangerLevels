// src/main/java/rl/sage/rangerlevels/gui/help/HelpMenu.java
package rl.sage.rangerlevels.gui.help;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.Items;
import net.minecraft.util.text.StringTextComponent;
import rl.sage.rangerlevels.gui.HelpButtonUtils;
import rl.sage.rangerlevels.gui.MenuItemBuilder;
import rl.sage.rangerlevels.gui.PlayerInfoUtils;

import java.util.Arrays;

public class HelpMenu {

    public static void open(ServerPlayerEntity player) {
        // 1) Creamos un inventario virtual de 27 slots (3×9)
        Inventory inv = new Inventory(27);
        inv.clearContent();

        inv.setItem(4, PlayerInfoUtils.getInfoItem(player, 4));

        // 2) Para cada sección, extraemos el texto hover y lo usamos como lore
        //    buildEventosHover(): devuelve un ITextComponent con líneas separadas por '\n'
        String[] eventos = HelpButtonUtils.buildEventosHover().getString().split("\n");
        inv.setItem(22, MenuItemBuilder.createButton(
                "§f§l✦ Eventos Activos",
                Arrays.asList(eventos),
                Items.FIREWORK_STAR,
                "topic1",
                22
        ));

        String[] compra = HelpButtonUtils.buildCompraHover().getString().split("\n");
        inv.setItem(10, MenuItemBuilder.createButton(
                "§e§l✧ Cómo comprar el pase",
                Arrays.asList(compra),
                Items.EMERALD,
                "topic2",
                10
        ));

        String maxLevel = "§7Nivel Máximo: §f" + rl.sage.rangerlevels.config.ExpConfig.get().getMaxLevel();
        inv.setItem(12, MenuItemBuilder.createButton(
                "§f§l✦ Nivel Máximo Actual",
                Arrays.asList(maxLevel),
                Items.EXPERIENCE_BOTTLE,
                "topic3",
                12
        ));

        String[] reinicio = HelpButtonUtils.buildReinicioHover(player).getString().split("\n");
        inv.setItem(14, MenuItemBuilder.createButton(
                "§e§l✧ Próximo Reinicio del pase",
                Arrays.asList(reinicio),
                Items.CLOCK,
                "topic4",
                14
        ));

        String[] limiter = HelpButtonUtils.buildLimiterHover(player).getString().split("\n");
        inv.setItem(16, MenuItemBuilder.createButton(
                "§f§l✦ Limitador Activo",
                Arrays.asList(limiter),
                Items.BARRIER,
                "topic5",
                16
        ));

        // Botón de Volver al menú principal
        inv.setItem(26, MenuItemBuilder.createButton(
                "§cVolver",
                Arrays.asList("§6Regresa al menú principal"),
                Items.ARROW,
                "back",
                26
        ));

        // 3) Abrimos el menú
        player.openMenu(new SimpleNamedContainerProvider(
                (windowId, playerInv, p) ->
                        new HelpMenuContainer(windowId, playerInv, inv),
                new StringTextComponent("§6Ayuda RangerLevels")
        ));
    }
}
