package rl.sage.rangerlevels.setup;

import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.capability.LevelProvider;
import rl.sage.rangerlevels.capability.LimiterProvider;
import rl.sage.rangerlevels.config.ConfigLoader;
import rl.sage.rangerlevels.config.ExpConfig;
import rl.sage.rangerlevels.config.RewardConfig;
import rl.sage.rangerlevels.events.PixelmonEventHandler;
import rl.sage.rangerlevels.multiplier.MultiplierManager;
import rl.sage.rangerlevels.multiplier.MultiplierState;

@Mod.EventBusSubscriber(modid = RangerLevels.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SetupEvents {
    private static final Logger LOGGER = LogManager.getLogger(SetupEvents.class);

    // Extraemos la parte de texto del componente, sin estilos
    private static final String PREFIX_STR = RangerLevels.PREFIX.getString();

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("{}: iniciando configuración…", PREFIX_STR);
        event.enqueueWork(() -> {
            ExpConfig.load();
            ConfigLoader.load();
            RewardConfig.load();
            MultiplierState.load();
            MultiplierManager.instance().reload();
        });
        PixelmonEventHandler.register();
        LOGGER.info("{}: inicialización completada.", PREFIX_STR);
    }
}
