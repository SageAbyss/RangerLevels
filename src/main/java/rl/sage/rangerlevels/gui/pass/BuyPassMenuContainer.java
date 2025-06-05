package rl.sage.rangerlevels.gui.pass;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import rl.sage.rangerlevels.config.ExpConfig;
import rl.sage.rangerlevels.gui.BaseMenuContainer;
import rl.sage.rangerlevels.gui.MainMenu;

import net.minecraft.util.text.Style;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

/**
 * Contenedor para “Comprar Pase” que hereda toda la lógica de BaseMenuContainer.
 * Al pulsar un botón (“buy_super”, “buy_ultra”, “buy_master”), envía al jugador
 * el enlace correspondiente. En “back” cierra y vuelve al MainMenu (en el tick siguiente).
 */
public class BuyPassMenuContainer extends BaseMenuContainer {

    public BuyPassMenuContainer(int windowId,
                                PlayerInventory playerInv,
                                Inventory menuInventory) {
        super(windowId, playerInv, menuInventory);
    }

    @Override
    protected void handleButtonClick(String buttonId, ServerPlayerEntity player) {
        switch (buttonId) {
            case "buy_super": {
                String url = ExpConfig.get().getPassBuyUrls().getOrDefault("super", "");
                if (!url.isEmpty()) {
                    StringTextComponent label = new StringTextComponent("§eEnlace SuperPass: ");
                    StringTextComponent link = (StringTextComponent) new StringTextComponent(url)
                            .withStyle(Style.EMPTY
                                    .withColor(TextFormatting.GREEN)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            new StringTextComponent("§6Clic para abrir enlace"))));
                    player.sendMessage(label.append(link), player.getUUID());
                }
                player.closeContainer();
                break;
            }

            case "buy_ultra": {
                String url = ExpConfig.get().getPassBuyUrls().getOrDefault("ultra", "");
                if (!url.isEmpty()) {
                    StringTextComponent label = new StringTextComponent("§eEnlace UltraPass: ");
                    StringTextComponent link = (StringTextComponent) new StringTextComponent(url)
                            .withStyle(Style.EMPTY
                                    .withColor(TextFormatting.AQUA)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            new StringTextComponent("§6Clic para abrir enlace"))));
                    player.sendMessage(label.append(link), player.getUUID());
                }
                player.closeContainer();
                break;
            }

            case "buy_master": {
                String url = ExpConfig.get().getPassBuyUrls().getOrDefault("master", "");
                if (!url.isEmpty()) {
                    StringTextComponent label = new StringTextComponent("§eEnlace MasterPass: ");
                    StringTextComponent link = (StringTextComponent) new StringTextComponent(url)
                            .withStyle(Style.EMPTY
                                    .withColor(TextFormatting.GOLD)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            new StringTextComponent("§6Clic para abrir enlace"))));
                    player.sendMessage(label.append(link), player.getUUID());
                }
                player.closeContainer();
                break;
            }

            case "back":
                // Cierra este contenedor y, en el siguiente tick, abre MainMenu
                player.closeContainer();
                player.getServer().execute(() -> {
                    MainMenu.open(player);
                });
                break;

            default:
                // Ninguna acción
                break;
        }
    }
}
