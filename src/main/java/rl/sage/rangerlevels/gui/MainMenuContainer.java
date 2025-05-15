package rl.sage.rangerlevels.gui;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import rl.sage.rangerlevels.pass.PassManager.PassType;
import rl.sage.rangerlevels.setup.ModContainers;

import java.util.Arrays;
import java.util.List;

public class MainMenuContainer extends BaseMenuContainer {

    public MainMenuContainer(int windowId,
                             PlayerInventory playerInv,
                             Inventory menuInventory) {
        super(ModContainers.MAIN_MENU.get(), windowId, menuInventory, playerInv, 3);

        // Replace first 27 slots with MenuSlot
        for (int i = 0; i < menuInventory.getContainerSize(); i++) {
            Slot old = this.slots.get(i);
            MenuSlot ms = new MenuSlot(menuInventory, i, old.x, old.y);
            ms.set(old.getItem());
            this.slots.set(i, ms);
        }

        initMenuItems();
    }

    private void initMenuItems() {
        // 1) Clear all slots
        for (int i = 0; i < menuInv.getContainerSize(); i++) {
            menuInv.setItem(i, ItemStack.EMPTY);
        }

        // 2) Info button (slot 10)
        menuInv.setItem(10, MenuItemBuilder.createButton(
                "🧑‍🏫 Información",
                Arrays.asList("Ver información de tu cuenta"),
                Items.PLAYER_HEAD,
                "info_button",
                10
        ));

        // 3) Rewards menu (slot 12)
        menuInv.setItem(12, MenuItemBuilder.createButton(
                "🎁 Recompensas",
                Arrays.asList("Abrir menú de recompensas"),
                Items.CHEST,
                "rewards_menu",
                12
        ));

        // 4) Help menu (slot 14)
        menuInv.setItem(14, MenuItemBuilder.createButton(
                "❓ Ayuda",
                Arrays.asList("Cómo usar el mod"),
                Items.BOOK,
                "help_menu",
                14
        ));

        // 5) Buy Pass menu (slot 16)
        menuInv.setItem(16, MenuItemBuilder.createButton(
                "💸 Comprar Pase",
                Arrays.asList("Adquiere tu pase"),
                Items.EMERALD,
                "buy_pass_menu",
                16
        ));

        // 6) Close (slot 26)
        menuInv.setItem(26, MenuItemBuilder.createButton(
                "❌ Cerrar",
                Arrays.asList("Cerrar este menú"),
                Items.BARRIER,
                "close",
                26
        ));
    }

    @Override
    protected void handleMenuAction(String id, ServerPlayerEntity player) {
        switch (id) {
            case "info_button":
                player.sendMessage(
                        new StringTextComponent("Tu información: Nivel " + /* tu lógica aquí */ ""),
                        player.getUUID());
                break;

           // case "rewards_menu":
             //   new RewardsMenuGui().open(player);
               // break;

            //case "help_menu":
              //  new HelpMenuGui().open(player);
                //break;

            case "buy_pass_menu":
                BuyPassMenu.open(player);
                break;

            case "close":
                player.closeContainer();
                break;
        }
    }
}
