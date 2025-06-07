package rl.sage.rangerlevels.setup;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.config.ConfigLoader;
import rl.sage.rangerlevels.config.ExpConfig;
import rl.sage.rangerlevels.config.RewardConfig;
import rl.sage.rangerlevels.events.PixelmonEventHandler;
import rl.sage.rangerlevels.items.amuletos.ChampionAmulet;
import rl.sage.rangerlevels.items.amuletos.ShinyAmuletEstelar;
import rl.sage.rangerlevels.items.amuletos.ShinyAmuletLegendaria;
import rl.sage.rangerlevels.items.amuletos.ShinyAmuletMitico;
import rl.sage.rangerlevels.items.boxes.*;
import rl.sage.rangerlevels.items.cetro.CetroDivinoComun;
import rl.sage.rangerlevels.items.cetro.CetroDivinoEpico;
import rl.sage.rangerlevels.items.cetro.CetroDivinoMitico;
import rl.sage.rangerlevels.items.frasco.FrascoCalmaEpico;
import rl.sage.rangerlevels.items.frasco.FrascoCalmaEstelar;
import rl.sage.rangerlevels.items.frasco.FrascoCalmaRaro;
import rl.sage.rangerlevels.items.gemas.GemaExpComun;
import rl.sage.rangerlevels.items.gemas.GemaExpEpico;
import rl.sage.rangerlevels.items.gemas.GemaExpLegendario;
import rl.sage.rangerlevels.items.tickets.*;
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
        //ITEMS
        new TicketSuper();
        new TicketUltra();
        new TicketUltraShort();
        new TicketMaster();
        new TicketNivel();
        new CarameloNivel();
        new GemaExpComun();
        new GemaExpEpico();
        new GemaExpLegendario();
        new FrascoCalmaRaro();
        new FrascoCalmaEpico();
        new FrascoCalmaEstelar();
        new ChampionAmulet();
        new ShinyAmuletEstelar();
        new ShinyAmuletLegendaria();
        new ShinyAmuletMitico();
        new CetroDivinoComun();
        new CetroDivinoEpico();
        new MysteryBoxComun();
        new MysteryBoxRaro();
        new MysteryBoxEpico();
        new MysteryBoxLegendario();
        new MysteryBoxEstelar();
        new MysteryBoxMitico();

        PixelmonEventHandler.register();
        LOGGER.info("§8{}:§a Items cargados con éxito", PREFIX_STR);
    }
}
