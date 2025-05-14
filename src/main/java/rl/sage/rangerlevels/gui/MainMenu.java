package rl.sage.rangerlevels.gui;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.Arrays;

public class MainMenu {

    /**
     * Abre el menú principal como cofre virtual de 3 filas (27 slots).
     */
    public static void open(ServerPlayerEntity player) {
        // 1) Creamos un inventario interno de 27 slots (3x9)
        Inventory inv = new Inventory(27);
        inv.clearContent();

        // 2) Posicionamos nuestros botones en slots concretos
        inv.setItem(10, createInfoButton(player));
        inv.setItem(12, MenuItemBuilder.createButton(
                "§bRecompensas",
                Arrays.asList("Haz clic para ver tus recompensas"),
                Items.CHEST,
                "rewards",
                12
        ));
        inv.setItem(14, MenuItemBuilder.createButton(
                "§eAyuda",
                Arrays.asList("Información sobre el mod"),
                Items.BOOK,
                "help",
                14
        ));
        inv.setItem(16, MenuItemBuilder.createButton(
                "§aComprar Pase",
                Arrays.asList("Ver beneficios y opciones de compra"),
                Items.EMERALD,
                "buy",
                16
        ));

        // 3) Abrimos el menú con el constructor de ChestContainer
        player.openMenu(new SimpleNamedContainerProvider(
                (windowId, playerInv, p) ->
                        new MainMenuContainer(
                                windowId,
                                playerInv,
                                inv
                        ),
                new StringTextComponent("§6RangerLevels")
        ));
    }

    /**
     * Construye el botón de “Información” usando la cabeza del jugador.
     */
    private static ItemStack createInfoButton(ServerPlayerEntity player) {
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);

        // 1) Identificador de botón
        CompoundNBT tag = head.getOrCreateTag();
        tag.putString("MenuButtonID", "info");

        // 2) Display: nombre + lore
        CompoundNBT display = new CompoundNBT();

        // Nombre
        ITextComponent name = new StringTextComponent("§fInformación de " + player.getName().getString());
        display.putString("Name", ITextComponent.Serializer.toJson(name));

        // Lore
        ListNBT loreList = new ListNBT();

        // Nivel
        loreList.add(StringNBT.valueOf(
                ITextComponent.Serializer.toJson(
                        new StringTextComponent("Nivel: §a" + /* getLevel(player) */ "0")
                )
        ));
        // Exp
        loreList.add(StringNBT.valueOf(
                ITextComponent.Serializer.toJson(
                        new StringTextComponent("Exp: §b" + /* getExp(player) */ "0" + " (0%)")
                )
        ));

        display.put("Lore", loreList);

        // Adjuntamos el display al tag principal
        tag.put("display", display);
        head.setTag(tag);

        return head;
    }
}
