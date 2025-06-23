package rl.sage.rangerlevels.gui.help;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.registries.ForgeRegistries;
import rl.sage.rangerlevels.gui.MenuItemBuilder;
import rl.sage.rangerlevels.gui.PlayerInfoUtils;
import rl.sage.rangerlevels.util.PlayerSoundUtils;

import java.util.Arrays;

/**
 * HelpMenu: construye el inventario de 27 ranuras y coloca cada botón con su NBT
 * (MenuButtonID + MenuSlot). Al abrirse, imprime en consola los tags NBT de cada botón
 * para confirmar que llevan correctamente MenuButtonID y MenuSlot.
 */
public class HelpMenu {
    private static Item findArcChalice() {
        Item i = ForgeRegistries.ITEMS
                .getValue(new ResourceLocation("pixelmon", "arc_chalice"));
        return i != null ? i : Items.NETHER_STAR;
    }
    private static Item findClock() {
        Item i = ForgeRegistries.ITEMS.getValue(new ResourceLocation("pixelmon", "black_clock"));
        return i != null ? i : Items.CLOCK;
    }

    public static void open(ServerPlayerEntity player) {
        // Reproducir sonido al abrir
        PlayerSoundUtils.playSoundToPlayer(
                player,
                SoundEvents.NOTE_BLOCK_BIT,
                SoundCategory.MASTER,
                1.0f,
                0.8f
        );

        // 1) Inventario virtual de 27 slots (3×9)
        Inventory inv = new Inventory(27);
        inv.clearContent();

        // 1.a) Slot 4: Info del jugador (no es “botón”; no lleva MenuButtonID/MenuSlot)
        inv.setItem(4, PlayerInfoUtils.getInfoItem(player, 4));

        // 2) Botones “Help” (estos sí llevan NBT de botón)

        //Sección Invocaciones
        ItemStack botonInvocaciones = MenuItemBuilder.createButton(
                "§f§l✦ Altar Arcano de la Creación ✦",
                Arrays.asList(HelpButtonUtils.buildInvocaciones().getString().split("\n")),
                findArcChalice(),
                "invocaciones",
                0
        );
        inv.setItem(0, botonInvocaciones);

        // – Eventos Activos (slot 22)
        ItemStack botonTopic1 = MenuItemBuilder.createButton(
                "§f§l✦ Eventos Activos",
                Arrays.asList(HelpButtonUtils.buildEventosHover().getString().split("\n")),
                Items.FIREWORK_STAR,
                "topic1",
                22
        );
        inv.setItem(22, botonTopic1);

        // – Cómo comprar el pase (slot 10)
        ItemStack botonTopic2 = MenuItemBuilder.createButton(
                "§e§l✧ Cómo tener un pase",
                Arrays.asList(HelpButtonUtils.buildCompraHover().getString().split("\n")),
                Items.EMERALD,
                "topic2",
                10
        );
        inv.setItem(10, botonTopic2);

        // – Nivel Máximo Actual (slot 12)
        String maxLevel = "§7Nivel Máximo: §f" + rl.sage.rangerlevels.config.ExpConfig.get().getMaxLevel();
        ItemStack botonTopic3 = MenuItemBuilder.createButton(
                "§f§l✦ Nivel Máximo Actual",
                Arrays.asList(maxLevel),
                Items.EXPERIENCE_BOTTLE,
                "topic3",
                12
        );
        inv.setItem(12, botonTopic3);

        // – Próximo Reinicio del pase (slot 14)
        ItemStack botonTopic4 = MenuItemBuilder.createButton(
                "§e§l✧ Próximo Reinicio del pase",
                Arrays.asList(HelpButtonUtils.buildReinicioHover(player).getString().split("\n")),
                findClock(),
                "topic4",
                14
        );
        inv.setItem(14, botonTopic4);

        // – Limitador Activo (slot 16)
        ItemStack botonTopic5 = MenuItemBuilder.createButton(
                "§f§l✦ Limitador Activo",
                Arrays.asList(HelpButtonUtils.buildLimiterHover(player).getString().split("\n")),
                Items.BARRIER,
                "topic5",
                16
        );
        inv.setItem(16, botonTopic5);

        // 3) Botón “Volver” (slot 26)
        ItemStack botonBack = MenuItemBuilder.createButton(
                "§cVolver",
                Arrays.asList("§6Regresa al menú principal"),
                Items.ARROW,
                "back",
                26
        );
        inv.setItem(26, botonBack);

        // 4) Abrir el contenedor con SimpleNamedContainerProvider y GENERIC_9x3
        player.openMenu(new SimpleNamedContainerProvider(
                (windowId, playerInv, p) -> new HelpMenuContainer(windowId, playerInv, inv),
                new StringTextComponent("§6Ayuda RangerLevels")
        ));
    }
}
