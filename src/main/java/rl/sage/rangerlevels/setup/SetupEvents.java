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
import rl.sage.rangerlevels.items.altar.*;
import rl.sage.rangerlevels.items.amuletos.*;
import rl.sage.rangerlevels.items.destinos.DestinoVinculadoEpico;
import rl.sage.rangerlevels.items.destinos.DestinoVinculadoLegendario;
import rl.sage.rangerlevels.items.destinos.DestinoVinculadoRaro;
import rl.sage.rangerlevels.items.herramientas.HachaTrabajadorCompulsivoEpico;
import rl.sage.rangerlevels.items.herramientas.HachaTrabajadorCompulsivoRaro;
import rl.sage.rangerlevels.items.herramientas.PicoMineroPerdidoEpico;
import rl.sage.rangerlevels.items.herramientas.PicoMineroPerdidoRaro;
import rl.sage.rangerlevels.items.modificadores.*;
import rl.sage.rangerlevels.items.randoms.*;
import rl.sage.rangerlevels.items.reliquias.*;
import rl.sage.rangerlevels.items.sacrificios.*;
import rl.sage.rangerlevels.items.sello.SelloReflejoMaestroEstelar;
import rl.sage.rangerlevels.items.sello.SelloReflejoMaestroLegendario;
import rl.sage.rangerlevels.items.sello.SelloReflejoMaestroRaro;
import rl.sage.rangerlevels.items.totems.fragmentos.*;
import rl.sage.rangerlevels.items.sello.SelloCapturaEpico;
import rl.sage.rangerlevels.items.boxes.*;
import rl.sage.rangerlevels.items.cetro.CetroDivinoComun;
import rl.sage.rangerlevels.items.cetro.CetroDivinoEpico;
import rl.sage.rangerlevels.items.flag.BattleBannerEpico;
import rl.sage.rangerlevels.items.flag.BattleBannerEstelar;
import rl.sage.rangerlevels.items.flag.BattleBannerMitico;
import rl.sage.rangerlevels.items.frasco.FrascoCalmaEpico;
import rl.sage.rangerlevels.items.frasco.FrascoCalmaEstelar;
import rl.sage.rangerlevels.items.frasco.FrascoCalmaRaro;
import rl.sage.rangerlevels.items.gemas.GemaExpComun;
import rl.sage.rangerlevels.items.gemas.GemaExpEpico;
import rl.sage.rangerlevels.items.gemas.GemaExpLegendario;
import rl.sage.rangerlevels.items.manuales.ManualEntrenamientoEpico;
import rl.sage.rangerlevels.items.manuales.ManualEntrenamientoLegendario;
import rl.sage.rangerlevels.items.manuales.ManualEntrenamientoRaro;
import rl.sage.rangerlevels.items.polvo.PolvoExpEstelar;
import rl.sage.rangerlevels.items.polvo.PolvoExpMitico;
import rl.sage.rangerlevels.items.polvo.PolvoExpRaro;
import rl.sage.rangerlevels.items.tickets.*;
import rl.sage.rangerlevels.multiplier.MultiplierManager;
import rl.sage.rangerlevels.multiplier.MultiplierState;

@Mod.EventBusSubscriber(modid = RangerLevels.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SetupEvents {
    private static final Logger LOGGER = LogManager.getLogger(SetupEvents.class);

    // Extraemos la parte de texto del componente, sin estilos
    private static final String PREFIX_STR = RangerLevels.PREFIX.getString();
    public static SelloCapturaEpico selloCapturaEpico;

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
        selloCapturaEpico = new SelloCapturaEpico();
        //ITEMS
        new TicketSuper();
        new TicketUltra();
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
        new SelloCapturaEpico();
        new BattleBannerEpico();
        new BattleBannerEstelar();
        new BattleBannerMitico();
        new ManualEntrenamientoRaro();
        new ManualEntrenamientoEpico();
        new ManualEntrenamientoLegendario();
        new ReliquiaTemporalComun();
        new ReliquiaTemporalRaro();
        new ReliquiaTemporalLegendario();
        new ReliquiaTemporalEstelar();
        new PolvoExpEstelar();
        new PolvoExpRaro();
        new PolvoExpMitico();
        new SelloReflejoMaestroRaro();
        new SelloReflejoMaestroLegendario();
        new SelloReflejoMaestroEstelar();
        new FragmentoCorazonGaia();
        new TotemRaizPrimordial();
        new FragmentoIraAncestral();
        new TotemLamentoDioses();
        new FragmentoRealidadAlterna();
        new TotemAbismoGlacial();
        new AltarArcano();
        new GenesisArcano();
        new LagrimaDiosaTiempo();
        new MaletinMentor();
        new HachaTrabajadorCompulsivoEpico();
        new HachaTrabajadorCompulsivoRaro();
        new PicoMineroPerdidoEpico();
        new PicoMineroPerdidoRaro();
        new SangreQuetzalEstelar();
        new SangreQuetzalLegendario();
        new SangreQuetzalMitico();
        new AltarAlmas();
        new NucleoDeSacrificio();
        new ConcentradoDeAlmas();
        new DestinoVinculadoRaro();
        new DestinoVinculadoEpico();
        new DestinoVinculadoLegendario();
        new GuantesDelEntrenador();
        new CapsulaExperienciaVolatil();

        new CatalizadorAlmas();
        new CatalizadorAlmasLimitado();
        new EsenciaUltraente();
        new EsenciaLegendaria();
        new EsenciaBoss();
        new ModificadorNaturaleza();
        new ModificadorTamano();
        new ModificadorShiny();
        new ModificadorIVs();
        new ModificadorIVsUniversal();
        new ModificadorShinyUniversal();
        new ModificadorTamanoUniversal();
        new ModificadorNaturalezaUniversal();

        AltarAlmasRegistry.registerRecipes();
        AltarRegistry.registerRecipes();
        PixelmonEventHandler.register();
        ConcentradoDeAlmasHandler.register();
        LOGGER.info("§8{}:§a Items cargados con éxito", PREFIX_STR);
    }
}
