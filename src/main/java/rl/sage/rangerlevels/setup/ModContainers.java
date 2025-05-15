package rl.sage.rangerlevels.setup;

import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.gui.BuyPassContainer;
import rl.sage.rangerlevels.gui.MainMenuContainer;

public class ModContainers {

    public static final DeferredRegister<ContainerType<?>> CONTAINERS =
            DeferredRegister.create(ForgeRegistries.CONTAINERS, RangerLevels.MODID);

    // 1) Buy Pass (3 filas)
    public static final RegistryObject<ContainerType<BuyPassContainer>> BUY_PASS_MENU =
            CONTAINERS.register("buy_pass_menu",
                    () -> IForgeContainerType.create((windowId, inv, data) -> {
                        int rows = data.readInt();              // leerá 3
                        Inventory menuInv = new Inventory(rows * 9);
                        return new BuyPassContainer(windowId, inv, menuInv);
                    })
            );

    // 2) Main Menu (3 filas)
    public static final RegistryObject<ContainerType<MainMenuContainer>> MAIN_MENU =
            CONTAINERS.register("main_menu",
                    () -> IForgeContainerType.create((windowId, inv, data) -> {
                        int rows = data.readInt();              // leerá 3
                        Inventory menuInv = new Inventory(rows * 9);
                        return new MainMenuContainer(windowId, inv, menuInv);
                    })
            );

    // 3) Rewards Menu (3 filas)
    /*public static final RegistryObject<ContainerType<RewardsMenuContainer>> REWARDS_MENU =
            CONTAINERS.register("rewards_menu",
                    () -> IForgeContainerType.create((windowId, inv, data) -> {
                        int rows = data.readInt();              // leerá 3
                        Inventory menuInv = new Inventory(rows * 9);
                        return new RewardsMenuContainer(windowId, inv, menuInv);
                    })
            );*/

    // Si más submenús: repite el patrón...
}
