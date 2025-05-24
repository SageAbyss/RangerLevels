package rl.sage.rangerlevels.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;


import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.text.*;

import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.minecraftforge.server.permission.PermissionAPI;
import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.capability.PassCapabilities;
import rl.sage.rangerlevels.config.ConfigLoader;
import rl.sage.rangerlevels.config.ExpConfig;
import rl.sage.rangerlevels.config.LevelsConfig;
import rl.sage.rangerlevels.config.RewardConfig;
import rl.sage.rangerlevels.capability.LevelProvider;
import rl.sage.rangerlevels.gui.HelpButtonUtils;
import rl.sage.rangerlevels.gui.help.HelpMenu;
import rl.sage.rangerlevels.multiplier.MultiplierManager;
import rl.sage.rangerlevels.multiplier.MultiplierState;
import rl.sage.rangerlevels.pass.PassManager;
import rl.sage.rangerlevels.permissions.PermissionRegistrar;
import rl.sage.rangerlevels.purge.PurgeData;
import rl.sage.rangerlevels.rewards.RewardManager;
import rl.sage.rangerlevels.util.GradientText;
import rl.sage.rangerlevels.util.TimeUtil;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static rl.sage.rangerlevels.RangerLevels.PREFIX;

@Mod.EventBusSubscriber(modid = RangerLevels.MODID)
public class CommandRegistry {
    private enum Mode { ADD, SET, REMOVE }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSource> dispatcher = event.getDispatcher();

        // sugerencias para <value>
        SuggestionProvider<CommandSource> valueSuggestions = (ctx, b) -> {
            b.suggest("2.0").suggest("3.0").suggest("5.0");
            return b.buildFuture();
        };
        // sugerencias para <time>
        SuggestionProvider<CommandSource> timeSuggestions = (ctx, b) -> {
            b.suggest("60s").suggest("5m").suggest("1h");
            return b.buildFuture();
        };

        LiteralCommandNode<CommandSource> base = dispatcher.register(
                Commands.literal("rangerlevels")
                        // help
                        .then(Commands.literal("help")
                                .requires(PermissionRegistrar.requireAny(
                                        PermissionRegistrar.USER,
                                        PermissionRegistrar.HELP,
                                        PermissionRegistrar.ADMIN
                                ))
                                .executes(ctx -> {
                                    ServerPlayerEntity player = ctx.getSource().getPlayerOrException();
                                    HelpMenu.open(player);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("pass")
                                .then(Commands.literal("info")
                                        .requires(PermissionRegistrar.requireAny(
                                                PermissionRegistrar.USER,
                                                PermissionRegistrar.PASS_INFO,
                                                PermissionRegistrar.ADMIN
                                        ))
                                        .executes(ctx -> showPassInfo(ctx.getSource().getPlayerOrException()))
                                )
                                .then(Commands.literal("buy")
                                        .requires(PermissionRegistrar.requireAny(
                                                PermissionRegistrar.USER,
                                                PermissionRegistrar.PASS_BUY,
                                                PermissionRegistrar.ADMIN
                                        ))                                        .executes(ctx -> {
                                            ServerPlayerEntity player = ctx.getSource().getPlayerOrException();
                                            return showPassBuyLinks(player);
                                        })
                                )
                                .then(Commands.literal("set")
                                        .requires(PermissionRegistrar.requireAny(
                                                PermissionRegistrar.PASS_SET,
                                                PermissionRegistrar.ADMIN
                                        ))
                                        .then(Commands.argument("target", EntityArgument.player())
                                                .then(Commands.argument("tier", StringArgumentType.word())
                                                        .suggests((ctx, sb) -> {
                                                            for (PassManager.PassType t : PassManager.PassType.values()) {
                                                                sb.suggest(t.name().toLowerCase());
                                                            }
                                                            return sb.buildFuture();
                                                        })
                                                        // Aquí estaba el error: antes era CommandRegister
                                                        .executes(CommandRegistry::setPassTier)
                                                )
                                        )
                                )

                        )
                        // stats
                        .then(Commands.literal("stats")
                                .requires(PermissionRegistrar.requireAny(
                                        PermissionRegistrar.USER,
                                        PermissionRegistrar.STATS,
                                        PermissionRegistrar.ADMIN
                                ))
                                .executes(CommandRegistry::showStats)
                        )
                        // addexp / setexp / removeexp
                        .then(Commands.literal("addexp")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                .requires(PermissionRegistrar.requireAny(
                                                        PermissionRegistrar.ADDEXP,
                                                        PermissionRegistrar.ADMIN
                                                ))
                                                .executes(ctx -> modifyExp(ctx, Mode.ADD))
                                        )
                                )
                        )

                        .then(Commands.literal("setexp")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                .requires(PermissionRegistrar.requireAny(
                                                        PermissionRegistrar.SETEXP,
                                                        PermissionRegistrar.ADMIN
                                                ))
                                                .executes(ctx -> modifyExp(ctx, Mode.SET))
                                        )
                                )
                        )
                        .then(Commands.literal("removeexp")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                .requires(PermissionRegistrar.requireAny(
                                                        PermissionRegistrar.REMOVEEXP,
                                                        PermissionRegistrar.ADMIN
                                                ))
                                                .executes(ctx -> modifyExp(ctx, Mode.REMOVE))
                                        )
                                )
                        )
                        // addlevel / setlevel / removelevel
                        .then(Commands.literal("addlevel")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .requires(PermissionRegistrar.requireAny(
                                                        PermissionRegistrar.ADDLEVEL,
                                                        PermissionRegistrar.ADMIN
                                                ))
                                                .executes(ctx -> modifyLevel(ctx, Mode.ADD))
                                        )
                                )
                        )
                        .then(Commands.literal("setlevel")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .requires(PermissionRegistrar.requireAny(
                                                        PermissionRegistrar.SETLEVEL,
                                                        PermissionRegistrar.ADMIN
                                                ))
                                                .executes(ctx -> modifyLevel(ctx, Mode.SET))
                                        )
                                )
                        )
                        .then(Commands.literal("removelevel")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .requires(PermissionRegistrar.requireAny(
                                                        PermissionRegistrar.REMOVELEVEL,
                                                        PermissionRegistrar.ADMIN
                                                ))
                                                .executes(ctx -> modifyLevel(ctx, Mode.REMOVE))
                                        )
                                )
                        )
                        // reset
                        .then(Commands.literal("reset")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .requires(PermissionRegistrar.requireAny(
                                                PermissionRegistrar.RESET,
                                                PermissionRegistrar.ADMIN
                                        ))
                                        .executes(CommandRegistry::resetStats)
                                )
                        )
                        // reload
                        .then(Commands.literal("reload")
                                .requires(PermissionRegistrar.requireAny(
                                        PermissionRegistrar.RELOAD,
                                        PermissionRegistrar.ADMIN
                                ))
                                .executes(ctx -> {
                                    ConfigLoader.load();
                                    ExpConfig.reload();
                                    MultiplierState.load();
                                    MultiplierManager.instance().reload();
                                    RewardConfig.reload();
                                    RangerLevels.INSTANCE.getAutoSaveTask().resetCounter();
                                    RangerLevels.INSTANCE.resetLimiterSchedule();
                                    ctx.getSource().sendSuccess(
                                            PREFIX.copy().append(
                                                    new StringTextComponent(" Configuración recargada con éxito.")
                                                            .withStyle(TextFormatting.GREEN)
                                            ),
                                            true
                                    );
                                    return 1;
                                })
                        )

                        // setmultiplier
                        .then(Commands.literal("SETmultiplier")
                                .requires(PermissionRegistrar.requireAny(
                                        PermissionRegistrar.SETMULTIPLIER,
                                        PermissionRegistrar.ADMIN
                                ))                                // GLOBAL
                                .then(Commands.literal("global")
                                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0))
                                                .suggests(valueSuggestions)
                                                .then(Commands.argument("time", StringArgumentType.word())
                                                        .suggests(timeSuggestions)
                                                        .executes(CommandRegistry::setMultiplierGlobal)
                                                )
                                        )
                                )
                                // PRIVATE
                                .then(Commands.literal("private")
                                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0))
                                                .suggests(valueSuggestions)
                                                .then(Commands.argument("time", StringArgumentType.word())
                                                        .suggests(timeSuggestions)
                                                        // sin target: aplica a ejecutor
                                                        .executes(ctx -> setMultiplierPrivate(ctx, null))
                                                        // con target: aplica a ese jugador
                                                        .then(Commands.argument("target", EntityArgument.player())
                                                                .executes(ctx -> setMultiplierPrivate(ctx,
                                                                        EntityArgument.getPlayer(ctx, "target")
                                                                ))
                                                        )
                                                )
                                        )
                                )
                                .then(Commands.literal("addmultiplier")
                                        .then(Commands.literal("global")
                                                .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0))
                                                        .suggests(valueSuggestions)
                                                        .then(Commands.argument("time", StringArgumentType.word())
                                                                .suggests(timeSuggestions)
                                                                .executes(CommandRegistry::addMultiplierGlobal)
                                                        )
                                                )
                                        )
                                        .then(Commands.literal("private")
                                                .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0))
                                                        .suggests(valueSuggestions)
                                                        .then(Commands.argument("time", StringArgumentType.word())
                                                                .suggests(timeSuggestions)
                                                                .executes(ctx -> addMultiplierPrivate(ctx, null))
                                                                .then(Commands.argument("target", EntityArgument.player())
                                                                        .executes(ctx -> addMultiplierPrivate(ctx,
                                                                                EntityArgument.getPlayer(ctx, "target")))
                                                                )
                                                        )
                                                )
                                        )

                                )
                        )
                        // multipliers
                        .then(Commands.literal("multipliers")
                                .requires(PermissionRegistrar.requireAny(
                                        PermissionRegistrar.USER,
                                        PermissionRegistrar.MULTIPLIERS,
                                        PermissionRegistrar.ADMIN
                                ))
                                .executes(CommandRegistry::showMultipliers)
                        )
                        .then(Commands.literal("menu")
                                .requires(PermissionRegistrar.requireAny(
                                        PermissionRegistrar.USER,
                                        PermissionRegistrar.MENU,
                                        PermissionRegistrar.ADMIN
                                ))                                .executes(ctx -> {
                                    ServerPlayerEntity player = ctx.getSource().getPlayerOrException();
                                    rl.sage.rangerlevels.gui.MainMenu.open(player);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("rewards")
                                .requires(PermissionRegistrar.requireAny(
                                        PermissionRegistrar.USER,
                                        PermissionRegistrar.REWARDS,
                                        PermissionRegistrar.ADMIN
                                ))                                .executes(ctx -> {
                                    ServerPlayerEntity player = ctx.getSource().getPlayerOrException();
                                    rl.sage.rangerlevels.gui.rewards.RewardsMenu.open(player);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("purga")
                                .requires(PermissionRegistrar.requireAny(
                                        PermissionRegistrar.PURGA,
                                        PermissionRegistrar.ADMIN
                                ))                                .executes(ctx -> {
                                    ServerPlayerEntity player = ctx.getSource().getPlayerOrException();
                                    ServerWorld world = player.getLevel(); // obtiene el mundo actual del jugador

                                    PurgeData data = PurgeData.get(world);

                                    long secondsLeft = data.getRemainingSeconds();
                                    if (data.hasPurgeEnded()) {
                                        player.sendMessage(new StringTextComponent("§cLa purga ha terminado."), player.getUUID());
                                    } else if (secondsLeft <= 0) {
                                        player.sendMessage(new StringTextComponent("§eLa purga está activa ahora mismo."), player.getUUID());
                                    } else {
                                        long days = secondsLeft / 86400;
                                        long hours = (secondsLeft % 86400) / 3600;
                                        long minutes = (secondsLeft % 3600) / 60;
                                        long seconds = secondsLeft % 60;

                                        String msg = String.format("§aTiempo restante para la purga: §f%dd %02dh %02dm %02ds", days, hours, minutes, seconds);
                                        player.sendMessage(new StringTextComponent(msg), player.getUUID());

                                    }

                                    return 1;
                                })
                        )
                        .then(Commands.literal("click_evento_1")
                                .requires(PermissionRegistrar.requireAny(
                                        PermissionRegistrar.CLICK_EVENTO,
                                        PermissionRegistrar.ADMIN
                                ))
                                .executes(ctx -> {
                                    ServerPlayerEntity player = ctx.getSource().getPlayerOrException();

                                    // Coordenadas desde SectionPos
                                    SectionPos sp = player.getLastSectionPos();
                                    double x = sp.getX();
                                    double y = sp.getY();
                                    double z = sp.getZ();

                                    // Ahora sí, usamos el SoundEvent directamente
                                    SPlaySoundEffectPacket packet = new SPlaySoundEffectPacket(
                                            SoundEvents.UI_BUTTON_CLICK,  // <— SoundEvent, no String
                                            SoundCategory.PLAYERS,
                                            x, y, z,
                                            1.0f,  // volumen
                                            1.0f   // tono
                                    );

                                    // Envío al jugador
                                    player.connection.send(packet);

                                    // Tu lógica extra
                                    //HelpButtonUtils.sendHelpMenu(player);
                                    return 1;
                                })
                        )


        );

        // alias /rlv
        dispatcher.register(Commands.literal("rlv").redirect(base));


        // --------------- ALIAS PARA STATS DIRECTO ---------------
        dispatcher.register(
                Commands.literal("stats")
                        .executes(CommandRegistry::showStats)
        );
        dispatcher.register(
                Commands.literal("level")
                        .executes(CommandRegistry::showStats)
        );
        dispatcher.register(
                Commands.literal("nivel")
                        .executes(CommandRegistry::showStats)
        );

        // justo después de tu registro de "rlv"
        dispatcher.register(
                Commands.literal("rewards")
                        // aquí podrías repetir tu .requires(...) de permisos si lo usas
                        .redirect(dispatcher.getRoot()
                                .getChild("rangerlevels")   // /rangerlevels
                                .getChild("rewards"))       // /rangerlevels rewards
        );
    }
    private static int showPassInfo(ServerPlayerEntity player) {
        int tier = PassCapabilities.get(player).getTier();
        PassManager.PassType pass = PassManager.PassType.values()[tier];

        // Encabezado con gradiente
        IFormattableTextComponent header = pass.getGradientDisplayName()
                .withStyle(style -> style.withColor(TextFormatting.GOLD))
                .append(new StringTextComponent("\n"));

        // Beneficios: usamos la descripción genérica del pase
        IFormattableTextComponent benefits = new StringTextComponent("Beneficios:\n")
                .withStyle(style -> style.withColor(TextFormatting.YELLOW))
                .append(new StringTextComponent(" - " + pass.getDescription() + "\n")
                        .withStyle(style -> style.withColor(TextFormatting.GRAY)));

        // URL de compra
        IFormattableTextComponent purchase = new StringTextComponent("Compra aquí: ")
                .withStyle(style -> style.withColor(TextFormatting.AQUA))
                .append(new StringTextComponent(pass.getPurchaseUrl())
                        .withStyle(style -> style.withColor(TextFormatting.BLUE)));

        // Envío de mensajes
        player.sendMessage(header, player.getUUID());
        player.sendMessage(benefits, player.getUUID());
        player.sendMessage(purchase, player.getUUID());

        return 0; // Si necesitas un entero de retorno, ajusta según tu lógica
    }


    private static int showPassBuyLinks(ServerPlayerEntity player) {
        UUID uuid = player.getUUID();
        Map<String, String> urls = ExpConfig.get().getPassBuyUrls();

        // ╔════════════════════════════════╗
        IFormattableTextComponent header = GradientText.of(
                " ╔═══════ ❖ Compra de Pases ❖ ═══════╗ ",
                "#FFEB99", // pastel amarillo
                "#FFD1DC", // pastel rosa
                "#C3E0E5"  // pastel azul
        ).withStyle(Style.EMPTY.withBold(true));
        player.sendMessage(header.append(new StringTextComponent("\n")), uuid);

        // ║   • Super Pass
        sendDecoratedPass(player, PassManager.PassType.SUPER,
                urls.getOrDefault("super", ""), uuid);

        // ╟────────────────────────────────╢
        player.sendMessage(
                new StringTextComponent(" ╟───────────────────────────────╢\n")
                        .withStyle(Style.EMPTY.withColor(TextFormatting.GRAY)),
                uuid
        );

        // ║   • Ultra Pass
        sendDecoratedPass(player, PassManager.PassType.ULTRA,
                urls.getOrDefault("ultra", ""), uuid);

        // ╟────────────────────────────────╢
        player.sendMessage(
                new StringTextComponent(" ╟───────────────────────────────╢\n")
                        .withStyle(Style.EMPTY.withColor(TextFormatting.GRAY)),
                uuid
        );

        // ║   • Master Pass
        sendDecoratedPass(player, PassManager.PassType.MASTER,
                urls.getOrDefault("master", ""), uuid);

        // ╚════════════════════════════════╝
        player.sendMessage(
                new StringTextComponent(" ╚═══════════════════════════════╝")
                        .withStyle(Style.EMPTY.withColor(TextFormatting.GRAY)),
                uuid
        );

        return 1;
    }

    /** Envía una línea de pase con viñeta, hover y click decorados */
    private static void sendDecoratedPass(ServerPlayerEntity player, PassManager.PassType type, String url, UUID uuid) {
        // Viñeta y espacio
        IFormattableTextComponent line = new StringTextComponent(" ║   • ")
                .append(type.getGradientDisplayName().copy());

        // Hover + Click
        Style style = Style.EMPTY
                .withHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        new StringTextComponent("§e" + type.getDescription() + "\n§eClick para abrir")
                ))
                .withClickEvent(new ClickEvent(
                        ClickEvent.Action.OPEN_URL,
                        url
                ));
        line.setStyle(style);

        // Envía la línea con salto de línea al final
        player.sendMessage(line.append(new StringTextComponent("\n")), uuid);
    }

    private static int setPassTier(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgument.getPlayer(ctx, "target");
        String tierName = StringArgumentType.getString(ctx, "tier").toUpperCase();

        PassManager.PassType chosen;
        try {
            chosen = PassManager.PassType.valueOf(tierName);
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(new StringTextComponent("Tier inválido: " + tierName));
            return 0;
        }

        PassCapabilities.get(target).setTier(chosen.ordinal());
        ctx.getSource().sendSuccess(
                new StringTextComponent(
                        "Tier de pase de " + target.getName().getString()
                                + " establecido a " + chosen.name().toLowerCase()
                ),
                true
        );
        return 1;
    }



    // ----------------------------
    // setmultiplier: GLOBAL branch
    // ----------------------------
    private static int setMultiplierGlobal(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        double value   = DoubleArgumentType.getDouble(ctx, "value");
        String timeArg = StringArgumentType.getString(ctx, "time");
        long seconds;
        try {
            seconds = TimeUtil.parseDuration(timeArg);
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(
                    PREFIX.copy().append(
                            new StringTextComponent(" Tiempo inválido: " + e.getMessage())
                                    .withStyle(TextFormatting.RED)
                    )
            );
            return 0;
        }

        MultiplierManager.instance().setGlobal(value, seconds);
        ctx.getSource().sendSuccess(
                PREFIX.copy().append(new StringTextComponent(" Multiplicador global x")
                                .withStyle(TextFormatting.GREEN))
                        .append(new StringTextComponent(String.valueOf(value))
                                .withStyle(TextFormatting.AQUA))
                        .append(new StringTextComponent(
                                seconds > 0
                                        ? " durante " + timeArg
                                        : " indefinido"
                        ).withStyle(TextFormatting.GREEN)),
                true
        );
        return 1;

    }

    // -----------------------------
    // setmultiplier: PRIVATE branch
    // -----------------------------
    private static int setMultiplierPrivate(CommandContext<CommandSource> ctx, ServerPlayerEntity target) throws CommandSyntaxException {
        double value   = DoubleArgumentType.getDouble(ctx, "value");
        String timeArg = StringArgumentType.getString(ctx, "time");
        long seconds;
        try {
            seconds = TimeUtil.parseDuration(timeArg);
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(
                    PREFIX.copy().append(new StringTextComponent(" Tiempo inválido: " + e.getMessage())
                            .withStyle(TextFormatting.RED))
            );
            return 0;
        }

        if (target == null) {
            target = ctx.getSource().getPlayerOrException();
        }
        MultiplierManager.instance().setPlayer(target.getName().getString(), value, seconds);
        ctx.getSource().sendSuccess(
                PREFIX.copy()
                        .append(new StringTextComponent(" Multiplicador privado para ")
                                .withStyle(TextFormatting.GREEN))
                        .append(new StringTextComponent(target.getName().getString())
                                .withStyle(TextFormatting.GOLD))
                        .append(new StringTextComponent(" x" + value)
                                .withStyle(TextFormatting.AQUA))
                        .append(new StringTextComponent(seconds > 0
                                ? " durante " + timeArg
                                : " indefinido").withStyle(TextFormatting.GREEN)),
                true
        );
        return 1;

    }
    // -----------------------------
    // addmultiplier: GLOBAL branch
    // -----------------------------
    private static int addMultiplierGlobal(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        double value   = DoubleArgumentType.getDouble(ctx, "value");
        String timeArg = StringArgumentType.getString(ctx, "time");
        long seconds;
        try {
            seconds = TimeUtil.parseDuration(timeArg);
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(
                    PREFIX.copy().append(
                            new StringTextComponent(" Tiempo inválido: " + e.getMessage())
                                    .withStyle(TextFormatting.RED)
                    )
            );
            return 0;
        }

        MultiplierManager.instance().addGlobal(value, seconds);
        ctx.getSource().sendSuccess(
                PREFIX.copy().append(new StringTextComponent(" Multiplicador global aumentado en x")
                                .withStyle(TextFormatting.GREEN))
                        .append(new StringTextComponent(String.valueOf(value))
                                .withStyle(TextFormatting.AQUA))
                        .append(new StringTextComponent(
                                seconds > 0
                                        ? " durante " + timeArg
                                        : " indefinido"
                        ).withStyle(TextFormatting.GREEN)),
                true
        );
        return 1;
    }
    // ------------------------------
    // addmultiplier: PRIVATE branch
    // ------------------------------
    private static int addMultiplierPrivate(CommandContext<CommandSource> ctx, ServerPlayerEntity target) throws CommandSyntaxException {
        double value   = DoubleArgumentType.getDouble(ctx, "value");
        String timeArg = StringArgumentType.getString(ctx, "time");
        long seconds;
        try {
            seconds = TimeUtil.parseDuration(timeArg);
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(
                    PREFIX.copy().append(new StringTextComponent(" Tiempo inválido: " + e.getMessage())
                            .withStyle(TextFormatting.RED))
            );
            return 0;
        }

        if (target == null) target = ctx.getSource().getPlayerOrException();
        MultiplierManager.instance().addPlayerMultiplier(target.getName().getString(), value, seconds);
        ctx.getSource().sendSuccess(
                PREFIX.copy()
                        .append(new StringTextComponent(" Multiplicador privado aumentado en x")
                                .withStyle(TextFormatting.GREEN))
                        .append(new StringTextComponent(target.getName().getString())
                                .withStyle(TextFormatting.GOLD))
                        .append(new StringTextComponent(" +" + value)
                                .withStyle(TextFormatting.AQUA))
                        .append(new StringTextComponent(seconds > 0
                                ? " durante " + timeArg
                                : " indefinido").withStyle(TextFormatting.GREEN)),
                true
        );
        return 1;
    }
    // -----------------------------------
    // RESTO DE COMANDOS SIN CAMBIOS
    // -----------------------------------
    private static int showStats(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayerOrException();

        player.getCapability(LevelProvider.LEVEL_CAP).ifPresent(cap -> {
            int lvl = cap.getLevel();
            int exp = cap.getExp();

            // 1) EXP necesaria para el siguiente nivel usando la curva exponencial
            int next = ExpConfig.get().getLevels().getExpForLevel(lvl + 1);

            // 2) Porcentaje de progreso en este nivel
            int perc = (int) (exp * 100.0 / next);
            perc = MathHelper.clamp(perc, 0, 100);

            // 3) Barra de progreso
            int bars = 20;
            int filled = perc * bars / 100;
            StringBuilder bar = new StringBuilder();
            for (int i = 0; i < bars; i++) {
                bar.append(i < filled
                        ? TextFormatting.GREEN.toString() + "░"
                        : TextFormatting.DARK_GRAY.toString() + "░");
            }

            // 4) Header decorativo
            IFormattableTextComponent header = new StringTextComponent("")
                    .append(new StringTextComponent(TextFormatting.DARK_GRAY.toString()
                            + TextFormatting.STRIKETHROUGH.toString()
                            + "                    "))
                    .append(PREFIX.copy())
                    .append(new StringTextComponent(TextFormatting.DARK_GRAY.toString()
                            + TextFormatting.STRIKETHROUGH.toString()
                            + "                    "));

            // 5) Cuerpo con stats
            IFormattableTextComponent body = new StringTextComponent("")
                    .append(new StringTextComponent(TextFormatting.GRAY.toString()
                            + "         ☻ Jugador: "))
                    .append(new StringTextComponent(TextFormatting.WHITE.toString()
                            + player.getName().getString()))
                    .append(new StringTextComponent("\n"))
                    .append(new StringTextComponent(TextFormatting.GRAY.toString()
                            + "         ⚔ Nivel: "))
                    .append(new StringTextComponent(TextFormatting.WHITE.toString()
                            + lvl))
                    .append(new StringTextComponent("\n"))
                    .append(new StringTextComponent(TextFormatting.GRAY.toString()
                            + "         ❖ Exp: "))
                    .append(new StringTextComponent(TextFormatting.AQUA.toString()
                            + exp))
                    .append(new StringTextComponent(TextFormatting.WHITE.toString()
                            + "/"
                            + TextFormatting.AQUA.toString()
                            + next))
                    .append(new StringTextComponent(TextFormatting.GRAY.toString()
                            + " ["
                            + TextFormatting.GREEN.toString()
                            + perc
                            + "%"
                            + TextFormatting.GRAY.toString()
                            + "]"))
                    .append(new StringTextComponent("\n"))
                    .append(new StringTextComponent(TextFormatting.WHITE.toString()
                            + "     "
                            + bar.toString()))
                    .append(new StringTextComponent("\n"))
                    .append(new StringTextComponent(TextFormatting.DARK_GRAY.toString()
                            + TextFormatting.STRIKETHROUGH.toString()
                            + "                                                           "));

            // 6) Envío
            player.sendMessage(header, player.getUUID());
            player.sendMessage(body,   player.getUUID());
        });

        return 1;
    }



    private static int modifyExp(CommandContext<CommandSource> ctx, Mode mode) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgument.getPlayer(ctx, "player");
        int amt = IntegerArgumentType.getInteger(ctx, "amount");

        target.getCapability(LevelProvider.LEVEL_CAP).ifPresent(cap -> {
            switch (mode) {
                case ADD:
                    LevelProvider.giveExpAndNotify(target, amt);  // Usa tu método especial
                    break;
                case REMOVE:
                    int currentExp = cap.getExp();
                    int newExp = Math.max(0, currentExp - amt);   // Evita negativos
                    cap.setExp(newExp);
                    break;
                case SET:
                    cap.setExp(amt);
                    break;
            }

            target.sendMessage(new StringTextComponent(
                    TextFormatting.LIGHT_PURPLE + "☑ EXP de " +
                            TextFormatting.GOLD + target.getName().getString() +
                            TextFormatting.LIGHT_PURPLE + " ahora es " +
                            TextFormatting.AQUA + cap.getExp()
            ), target.getUUID());
        });

        return 1;
    }



    private static int modifyLevel(CommandContext<CommandSource> ctx, Mode mode) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgument.getPlayer(ctx, "player");
        int amt = IntegerArgumentType.getInteger(ctx, "amount");

        target.getCapability(LevelProvider.LEVEL_CAP).ifPresent(cap -> {
            int currentLevel = cap.getLevel();
            int newLevel = currentLevel;

            // Calculamos el nuevo nivel según el modo
            switch (mode) {
                case ADD:
                    newLevel += amt;
                    break;
                case REMOVE:
                    newLevel -= amt;
                    break;
                case SET:
                    newLevel = amt;
                    break;
            }

            int maxLevel = ExpConfig.get().getMaxLevel();
            int minLevel = ExpConfig.get().getLevels().getStartingLevel();
            newLevel = MathHelper.clamp(newLevel, minLevel, maxLevel);

            LevelsConfig levelsCfg = ExpConfig.get().getLevels();

            if (mode == Mode.ADD) {
                // --- Opción 2: subimos 'amt' niveles directamente
                List<Integer> nivelesSubidos = cap.addLevel(amt);
                for (int lvl : nivelesSubidos) {
                    // 1) Marca recompensas PENDING
                    RewardManager.handleLevelUp(target, lvl);

                    // 2) Mensajes y sonido para este nivel
                    IFormattableTextComponent sep = GradientText.of(
                            "                                                                      ",
                            "#FF0000","#FF7F00","#FFFF00",
                            "#00FF00","#0000FF","#4B0082","#9400D3"
                    ).withStyle(TextFormatting.STRIKETHROUGH);

                    IFormattableTextComponent title = new StringTextComponent(
                            TextFormatting.GOLD +
                                    (lvl >= maxLevel
                                            ? "¡Alcanzaste el Nivel Máximo! ¡Felicidades!"
                                            : "Subiste a Nivel §7(§f" + lvl + "§7)")
                    );

                    target.displayClientMessage(sep, false);
                    target.displayClientMessage(title, false);
                    target.displayClientMessage(sep, false);

                    target.displayClientMessage(
                            new StringTextComponent("§e⇧ §3Nivel Ranger " + lvl),
                            true
                    );

                    target.level.playSound(
                            null, target.blockPosition(),
                            SoundEvents.PLAYER_LEVELUP, SoundCategory.PLAYERS,
                            1.0f, 1.0f
                    );
                }
            } else {
                // REMOVE y SET: ajustamos nivel y EXP acumulada
                int targetTotalExp = levelsCfg.getTotalExpForLevel(newLevel);
                cap.setLevel(newLevel);
                cap.setExp(targetTotalExp);

                target.sendMessage(new StringTextComponent(
                        TextFormatting.LIGHT_PURPLE + "☑ Nivel de " +
                                TextFormatting.GOLD + target.getName().getString() +
                                TextFormatting.LIGHT_PURPLE + " ahora es " +
                                TextFormatting.YELLOW + newLevel
                ), target.getUUID());
            }
        });

        return 1;
    }


    private static int resetStats(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgument.getPlayer(ctx, "player");
        target.getCapability(LevelProvider.LEVEL_CAP).ifPresent(cap -> {
            cap.setLevel(1);
            cap.setExp(0);
            target.sendMessage(new StringTextComponent(
                    TextFormatting.LIGHT_PURPLE + "☑ Estadísticas de " +
                            TextFormatting.GOLD + target.getName().getString() +
                            TextFormatting.LIGHT_PURPLE + " reseteadas a Nivel 1 y EXP 0"
            ), target.getUUID());
        });
        return 1;
    }

    private static int showMultipliers(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayerOrException();
        MultiplierManager mgr = MultiplierManager.instance();

        double globalVal   = mgr.getGlobal();
        double personalVal = mgr.getPlayer(player);

        long globalRem   = mgr.getGlobalRemainingSeconds();
        long personalRem = mgr.getPlayerRemainingSeconds(player);

        String globalTime   = globalRem   < 0 ? "∞" : TimeUtil.formatDuration(globalRem);
        String personalTime = personalRem < 0 ? "∞" : TimeUtil.formatDuration(personalRem);

        // Cabecera decorativa
        IFormattableTextComponent header = new StringTextComponent("")
                .append(new StringTextComponent("         ")
                        .withStyle(style -> style.setStrikethrough(true).withColor(Color.fromRgb(0xFFFF00))))
                .append(GradientText.of(" Multiplicadores ", "#FFFF00", "#FFA500"))
                .append(new StringTextComponent("         ")
                        .withStyle(style -> style.setStrikethrough(true).withColor(Color.fromRgb(0xFFA500))));
        player.sendMessage(header, player.getUUID());

        // ✨ Nuevo: multiplicador por pase actual desde Capability
        int tierOrdinal = PassCapabilities.get(player).getTier();
        PassManager.PassType pass;
        // Convertimos ordinal a PassType, si está fuera de rango, fallback a FREE
        PassManager.PassType[] types = PassManager.PassType.values();
        if (tierOrdinal >= 0 && tierOrdinal < types.length) {
            pass = types[tierOrdinal];
        } else {
            pass = PassManager.PassType.FREE;
        }

        double passMul;
        switch (pass) {
            case SUPER:  passMul = 1.25; break;
            case ULTRA:  passMul = 1.5;  break;
            case MASTER: passMul = 2.0;  break;
            default:     passMul = 1.0;  break;
        }
        player.sendMessage(
                new StringTextComponent(TextFormatting.GRAY + "Pase: ")
                        .append(pass.getGradientDisplayName())
                        .append(new StringTextComponent(TextFormatting.GRAY + " (×" + passMul + ")")),
                player.getUUID()
        );

        // Multiplicador global
        player.sendMessage(
                new StringTextComponent(
                        TextFormatting.GRAY + "Global: " + TextFormatting.AQUA + globalVal +
                                TextFormatting.GRAY + " (Tiempo restante: " + TextFormatting.GREEN + globalTime + TextFormatting.GRAY + ")"
                ),
                player.getUUID()
        );

        // Multiplicador privado
        player.sendMessage(
                new StringTextComponent(
                        TextFormatting.GRAY + "Privado: " + TextFormatting.AQUA + personalVal +
                                TextFormatting.GRAY + " (Tiempo restante: " + TextFormatting.GREEN + personalTime + TextFormatting.GRAY + ")"
                ),
                player.getUUID()
        );

        return 1;
    }

}
