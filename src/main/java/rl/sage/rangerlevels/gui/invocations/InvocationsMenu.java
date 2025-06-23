package rl.sage.rangerlevels.gui.invocations;

import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.registries.ForgeRegistries;
import rl.sage.rangerlevels.gui.MenuItemBuilder;
import rl.sage.rangerlevels.gui.PlayerInfoUtils;
import rl.sage.rangerlevels.util.EnchantUtils;
import rl.sage.rangerlevels.util.NBTUtils;
import rl.sage.rangerlevels.util.PlayerSoundUtils;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import java.util.Arrays;
import java.util.Collections;

import rl.sage.rangerlevels.gui.BaseMenuContainer6;

public class InvocationsMenu {
    private static Item findArcChalice() {
        Item i = ForgeRegistries.ITEMS
                .getValue(new ResourceLocation("pixelmon", "arc_chalice"));
        return i != null ? i : Items.NETHER_STAR;
    }

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

        // Slot 22: info del jugador (centro superior del menú)
        inv.setItem(4, PlayerInfoUtils.getInfoItem(player, 4));

        // Ejemplo Invocaciones en filas superiores
        ItemStack inv1 = MenuItemBuilder.createButton(
                "§7◈ Invocación: §aTótem de Raíz Primordial",
                Arrays.asList(
                        "§7Ingredientes requeridos:",
                        " §f• x6 Fragmento del Corazón de Gaia",
                        " §f• x2 Chapa Dorada",
                        " §f• x1 Génesis ⚶ Arcano",
                        "§7Click para ver sus mejoras"
                ),
                PixelmonItems.smooth_rock,

                "totemRaizPrimordial",
                10

        );
        inv.setItem(10, decorateGlowy(inv1));

        ItemStack inv2 = MenuItemBuilder.createButton(
                "§7◈ Invocación: §cTótem del Lamento de los Dioses",
                Arrays.asList(
                        "§7Ingredientes requeridos:",
                        " §f• x6 Fragmento de Ira Ancestral",
                        " §f• x2 Chapa Dorada",
                        " §f• x1 Génesis ⚶ Arcano",
                        "§7Click para ver sus mejoras"
                ),
                PixelmonItems.heat_rock,
                "totemLamentoDioses",
                12
        );
        inv.setItem(12, decorateGlowy(inv2));

        ItemStack inv3 = MenuItemBuilder.createButton(
                "§7◈ Invocación: §bTótem del Abismo Glacial",
                Arrays.asList(
                        "§7Ingredientes requeridos:",
                        " §f• x6 Fragmento de Realidad Alterna",
                        " §f• x2 Chapa Dorada",
                        " §f• x1 Génesis ⚶ Arcano",
                        "§7Click para ver sus mejoras"
                ),
                PixelmonItems.icy_rock,
                "totemAbismoGlacial",
                14
        );
        inv.setItem(14, decorateGlowy(inv3));

        ItemStack inv4 = MenuItemBuilder.createButton(
                "§7◈ Invocación: §bLágrima de la Diosa del Tiempo",
                Arrays.asList(
                        "§7Ingredientes requeridos:",
                        " §f• x2 Ticket Nivel",
                        " §f• x2 Caramelo Nivel",
                        " §f• x2 Gema de Dominio Legendario",
                        " §f• x1 Génesis ⚶ Arcano",
                        "§7Click para ver sus mejoras"
                ),
                PixelmonItems.white_mane_hair,
                "lagrimaDiosa",
                16
        );
        inv.setItem(16, decorateGlowy(inv4));

        ItemStack inv5 = MenuItemBuilder.createButton(
                "§7◈ Invocación: §4Sangre de Quetzalcóatl Mítico",
                Arrays.asList(
                        "§7Ingredientes requeridos:",
                        " §f• x2 Sangre de Quetzalcóatl Estelar",
                        " §f• x2 Sangre de Quetzalcóatl Legendario",
                        " §f• x2 Gema de Dominio Legendario",
                        " §f• x1 Génesis ⚶ Arcano",
                        "§7Click para ver sus mejoras"
                ),
                PixelmonItems.curry_seasoned,
                "sangreQuetzal",
                20

        );
        inv.setItem(20, decorateGlowy(inv5));

        ItemStack inv6 = MenuItemBuilder.createButton(
                "§7◈ Invocación: §4Ticket Pase Master Temporal",
                Arrays.asList(
                        "§7Ingredientes requeridos:",
                        " §f• x2 Ticket Pase Super Temporal",
                        " §f• x1 Ticket Pase Ultra Temporal",
                        " §f• x1 Génesis ⚶ Arcano"
                ),
                PixelmonItems.rainbow_pass,
                "masterPass",
                24

        );
        inv.setItem(24, decorateGlowy(inv6));

        ItemStack inv7 = MenuItemBuilder.createButton(
                "§7◈ Invocación: §4Catalizador de Almas Infinito",
                Arrays.asList(
                        "§7Ingredientes requeridos:",
                        " §f• x4 Esencia Legendaria",
                        " §f• x4 Esencia UltraEnte",
                        " §f• x32 Esencia Boss",
                        " §f• x2 Génesis ⚶ Arcano"

                ),
                PixelmonItems.professors_mask,
                "catalizadorAlmas",
                22

        );
        inv.setItem(22, decorateGlowy(inv7));

        // Botón Volver en esquina inferior derecha (slot 53)
        ItemStack botonBack = MenuItemBuilder.createButton(
                "§cVolver",
                Collections.singletonList("§6Regresa al menú principal"),
                Items.ARROW,
                "back",
                53
        );
        inv.setItem(53, botonBack);

        //ALTAR DE ALMAS
        ItemStack botonInvocaciones = MenuItemBuilder.createButton(
                "§4§l✦ Altar de Almas ✦",
                Arrays.asList(buildInvocaciones().getString().split("\n")),
                findArcChalice(),
                "invocaciones",
                45
        );
        inv.setItem(45, botonInvocaciones);

        // Abrir contenedor con BaseMenuContainer6
        player.openMenu(new SimpleNamedContainerProvider(
                (windowId, playerInv, p) -> new InvocationsMenuContainer(windowId, playerInv, inv),
                new StringTextComponent("§6Invocaciones")
        ));
    }
    public static ITextComponent buildInvocaciones() {
        IFormattableTextComponent hover = new StringTextComponent("");
        hover.append(new StringTextComponent("§f▶ §7Catalizador de Esencias de Legendarios/Ultra Entes.\n"));
        hover.append(new StringTextComponent("§f▶ §7Invoca modificadores de ADN únicos.\n\n"));
        hover.append(new StringTextComponent("§7Click para abrir\n"));
        return hover;
    }
}