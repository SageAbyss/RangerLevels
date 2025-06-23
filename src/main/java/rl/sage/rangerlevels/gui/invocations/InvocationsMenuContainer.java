package rl.sage.rangerlevels.gui.invocations;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import rl.sage.rangerlevels.config.ItemsConfig;
import rl.sage.rangerlevels.gui.MainMenu;
import net.minecraft.util.text.StringTextComponent;
import java.util.Objects;
import rl.sage.rangerlevels.gui.BaseMenuContainer6;
import rl.sage.rangerlevels.items.Tier;
import rl.sage.rangerlevels.util.GradientText;

public class InvocationsMenuContainer extends BaseMenuContainer6 {

    public InvocationsMenuContainer(int windowId,
                                    PlayerInventory playerInv,
                                    Inventory menuInventory) {
        super(windowId, playerInv, menuInventory);
    }

    ItemsConfig.LagrimaTiempoConfig cfgl = ItemsConfig.get().lagrimaTiempo;
    int expAmount = cfgl.expAmount;
    int intervalMin = cfgl.intervalMinutes;

    ItemsConfig.BloodConfig cfgs = ItemsConfig.get().blood;
    double pct = cfgs.miticoPercent;
    int   dur = cfgs.miticoDurationMinutes;

    @Override
    protected void handleButtonClick(String buttonId, ServerPlayerEntity player) {
        switch (buttonId) {
            case "totemRaizPrimordial":
                player.sendMessage(new StringTextComponent("§8§m                                                          "), player.getUUID());
                player.sendMessage(
                        new StringTextComponent("§7◈ Invocación: §aTótem de Raíz Primordial"), player.getUUID());
                        player.sendMessage(new StringTextComponent("§7✧ Tipos Afectados: Bicho, Tierra, Roca, Normal, Eléctrico, Planta"), player.getUUID());
                player.sendMessage(new StringTextComponent("§7✧ Inmunidad al daño de caída"), player.getUUID());
                player.sendMessage(new StringTextComponent("§7✧ Captura de Tipos:"), player.getUUID());
                player.sendMessage(new StringTextComponent("   §7- 50% probabilidad de §6+5-12% IVs"), player.getUUID());
                player.sendMessage(new StringTextComponent("   §7- 5% probabilidad de convertir en Shiny"), player.getUUID());
                player.sendMessage(new StringTextComponent("   §7- Las dimensiones del Pokémon se ven afectadas"), player.getUUID());
                player.sendMessage(new StringTextComponent("§7✧ Bonus de §6+30%§7 EXP en:"), player.getUUID());
                player.sendMessage(new StringTextComponent("   §7- Capturas por Tipo"), player.getUUID());
                player.sendMessage(new StringTextComponent("   §7- Derrotas por Tipo"), player.getUUID());
                player.sendMessage(new StringTextComponent("   §7- Invocación de Dialga, Palkia o Giratina"), player.getUUID());
                player.sendMessage(new StringTextComponent("§7✧ Debe estar en el inventario"), player.getUUID());
                player.sendMessage(new StringTextComponent(" "), player.getUUID());
                player.sendMessage(new StringTextComponent("§7▶ Tier: ").append(Tier.MITICO.getColor()), player.getUUID());
                player.sendMessage(new StringTextComponent("§8§m                                                          "), player.getUUID());
                break;
            case "totemLamentoDioses":
                player.sendMessage(new StringTextComponent("§8§m                                                          "), player.getUUID());
                player.sendMessage(
                        new StringTextComponent("§7◈ Invocación: §cTótem del Lamento de los Dioses"),
                        player.getUUID()
                );
                player.sendMessage(new StringTextComponent("§7✧ Tipos Afectados: Fuego, Dragón, Lucha, Psíquico, Fantasma, Siniestro"), player.getUUID());
                player.sendMessage(new StringTextComponent("§7✧ Inmunidad al fuego"), player.getUUID());
                player.sendMessage(new StringTextComponent("§7✧ Otorga Fuerza II"), player.getUUID());
                player.sendMessage(new StringTextComponent("§7✧ Captura de Tipos:"), player.getUUID());
                player.sendMessage(new StringTextComponent("   §7- 50% probabilidad de §6+5-12% IVs"), player.getUUID());
                player.sendMessage(new StringTextComponent("   §7- 5% probabilidad de convertir en Shiny"), player.getUUID());
                player.sendMessage(new StringTextComponent("   §7- Las dimensiones del Pokémon se ven afectadas"), player.getUUID());
                player.sendMessage(new StringTextComponent("§7✧ Bonus de §6+30%§7 EXP en:"), player.getUUID());
                player.sendMessage(new StringTextComponent("   §7- Capturas por Tipo"), player.getUUID());
                player.sendMessage(new StringTextComponent("   §7- Derrotas por Tipo"), player.getUUID());
                player.sendMessage(new StringTextComponent("   §7- Invocación de Articuno, Zapdos o Moltres"), player.getUUID());
                player.sendMessage(new StringTextComponent("§7✧ Debe estar en el inventario"), player.getUUID());
                player.sendMessage(new StringTextComponent(" "), player.getUUID());
                player.sendMessage(new StringTextComponent("§7▶ Tier: ").append(Tier.MITICO.getColor()), player.getUUID());
                player.sendMessage(new StringTextComponent("§8§m                                                          "), player.getUUID());
                break;

            case "totemAbismoGlacial":
                player.sendMessage(new StringTextComponent("§8§m                                                          "), player.getUUID());
                player.sendMessage(
                        new StringTextComponent("§7◈ Invocación: §bTótem del Abismo Glacial"),
                        player.getUUID()
                );
                player.sendMessage(new StringTextComponent("§7✧ Tipos Afectados: Hielo, Agua, Volador, Veneno, Hada, Acero"), player.getUUID());
                player.sendMessage(new StringTextComponent("§7✧ Inmunidad a ahogarse y daño de caída"), player.getUUID());
                player.sendMessage(new StringTextComponent("§7✧ Captura de Tipos:"), player.getUUID());
                player.sendMessage(new StringTextComponent("   §7- 50% probabilidad de §6+5-12% IVs"), player.getUUID());
                player.sendMessage(new StringTextComponent("   §7- 5% probabilidad de convertir en Shiny"), player.getUUID());
                player.sendMessage(new StringTextComponent("   §7- Las dimensiones del Pokémon se ven afectadas"), player.getUUID());
                player.sendMessage(new StringTextComponent("§7✧ Bonus de §6+30%§7 EXP en:"), player.getUUID());
                player.sendMessage(new StringTextComponent("   §7- Capturas de Tipo"), player.getUUID());
                player.sendMessage(new StringTextComponent("   §7- Derrotas de Tipo"), player.getUUID());
                player.sendMessage(new StringTextComponent("   §7- Invocación de Arceus"), player.getUUID());
                player.sendMessage(new StringTextComponent("§7✧ Debe estar en el inventario"), player.getUUID());
                player.sendMessage(new StringTextComponent(" "), player.getUUID());
                player.sendMessage(new StringTextComponent("§7▶ Tier: ").append(Tier.MITICO.getColor()), player.getUUID());
                player.sendMessage(new StringTextComponent("§8§m                                                          "), player.getUUID());
                break;

            case "lagrimaDiosa":
                player.sendMessage(new StringTextComponent("§8§m                                                          "), player.getUUID());
                player.sendMessage(
                        new StringTextComponent("§7◈ Invocación: §bLágrima de la Diosa del Tiempo"),
                        player.getUUID()
                );
                player.sendMessage(new StringTextComponent(
                                String.format("§7✧ Otorga §6%d§7 de EXP cada §6%d minutos",
                                        expAmount, intervalMin)),
                        player.getUUID()
                );
                player.sendMessage(new StringTextComponent("§7✧ No requiere activación, debe permanecer en inventario"),
                        player.getUUID()
                );
                player.sendMessage(new StringTextComponent(" "), player.getUUID());
                player.sendMessage(new StringTextComponent("§7▶ Tier: ").append(Tier.MITICO.getColor()), player.getUUID());
                player.sendMessage(new StringTextComponent("§8§m                                                          "), player.getUUID());
                break;
            case "sangreQuetzal":
                player.sendMessage(new StringTextComponent("§8§m                                                          "), player.getUUID());
                player.sendMessage(
                        new StringTextComponent("§7◈ Invocación: §4Sangre de Quetzalcóatl Mítico"),
                        player.getUUID()
                );
                player.sendMessage(new StringTextComponent("§7❖ Bonus al x2 de EXP general"),
                        player.getUUID()
                );
                player.sendMessage(new StringTextComponent(
                                String.format("§7❖ Chance de +%.0f%% por %d min", pct, dur)),
                        player.getUUID()
                );
                player.sendMessage(new StringTextComponent("§7✧ Usar para activar."),
                        player.getUUID()
                );
                player.sendMessage(new StringTextComponent(" "), player.getUUID());
                player.sendMessage(new StringTextComponent("§7▶ Tier: ").append(Tier.MITICO.getColor()), player.getUUID());
                player.sendMessage(new StringTextComponent("§8§m                                                          "), player.getUUID());
                break;
            case "invocaciones":
                player.closeContainer();
                Objects.requireNonNull(player.getServer()).execute(() ->
                        AltarDeAlmasMenu.open(player)
                );
                break;
            case "catalizadorAlmas":
                player.sendMessage(new StringTextComponent("§8§m                                                          "), player.getUUID());
                player.sendMessage(
                        new StringTextComponent("§7◈ Invocación: §4Catalizador de Almas"),
                        player.getUUID()
                );
                player.sendMessage(new StringTextComponent("§7✧ Un artefacto forjado con la esencia ancestral."), player.getUUID());
                player.sendMessage(new StringTextComponent("§7✧ Haz clic derecho sobre un Pokémon Legendario"), player.getUUID());
                player.sendMessage(new StringTextComponent("§7   o Ultraente de tu equipo para sacrificarlo."), player.getUUID());
                player.sendMessage(new StringTextComponent(" "), player.getUUID());
                player.sendMessage(new StringTextComponent("§7✧ El sacrificio extrae la Esencia específica"), player.getUUID());
                player.sendMessage(new StringTextComponent("§7   que luego sirve para crear modificadores"), player.getUUID());
                player.sendMessage(new StringTextComponent("§7   de ADN únicos para ese Pokémon."), player.getUUID());
                player.sendMessage(new StringTextComponent(" "), player.getUUID());
                player.sendMessage(new StringTextComponent("§7✧ También extrae la Esencia de Jefes,"), player.getUUID());
                player.sendMessage(new StringTextComponent("§7   las cuales sirven para usarlas en el Altar"), player.getUUID());
                player.sendMessage(new StringTextComponent("§7   para intercambiarlas por Esencias."), player.getUUID());
                player.sendMessage(new StringTextComponent(" "), player.getUUID());
                player.sendMessage(new StringTextComponent("§7▶ Tier: ").append(Tier.ESTELAR.getColor()), player.getUUID());
                player.sendMessage(new StringTextComponent("§8§m                                                          "), player.getUUID());
                break;
            case "back":
                player.closeContainer();
                Objects.requireNonNull(player.getServer(), "MinecraftServer es nulo").execute(() ->
                        MainMenu.open(player)
                );
                break;
            default:
                break;
        }
    }
}