package rl.sage.rangerlevels.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;


import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.text.*;

import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import rl.sage.rangerlevels.RangerLevels;
import rl.sage.rangerlevels.capability.IPassCapability;
import rl.sage.rangerlevels.capability.PassCapabilities;
import rl.sage.rangerlevels.config.*;
import rl.sage.rangerlevels.capability.LevelProvider;
import rl.sage.rangerlevels.database.FlatFilePlayerDataManager;
import rl.sage.rangerlevels.gui.help.HelpMenu;
import rl.sage.rangerlevels.items.CustomItemRegistry;
import rl.sage.rangerlevels.multiplier.MultiplierManager;
import rl.sage.rangerlevels.multiplier.MultiplierState;
import rl.sage.rangerlevels.pass.PassManager;
import rl.sage.rangerlevels.pass.PassType;
import rl.sage.rangerlevels.purge.PurgeData;
import rl.sage.rangerlevels.rewards.RewardManager;
import rl.sage.rangerlevels.util.GradientText;
import rl.sage.rangerlevels.util.PlayerSoundUtils;
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
                                .executes(ctx -> {
                                    ServerPlayerEntity player = ctx.getSource().getPlayerOrException();
                                    HelpMenu.open(player);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("pass")
                                .then(Commands.literal("info")
                                        .executes(ctx -> showPassInfo(ctx.getSource().getPlayerOrException()))
                                )
                                .then(Commands.literal("buy")
                                                            .executes(ctx -> {
                                            ServerPlayerEntity player = ctx.getSource().getPlayerOrException();
                                            return showPassBuyLinks(player);
                                        })
                                )
                                .then(Commands.literal("set")
                                        .requires(src -> {
                                            try {
                                                ServerPlayerEntity p = src.getPlayerOrException();
                                                return AdminConfig.isAdmin(p.getName().getString());
                                            } catch (Exception e) {
                                                return true; // consola
                                            }
                                        })
                                        .executes(ctx -> {
                                            ctx.getSource().sendFailure(
                                                    new StringTextComponent(TextFormatting.RED +
                                                            "Uso correcto: /rlv pass set <Player> <Pase>")
                                            );
                                            return 0;
                                        })
                                        .then(Commands.argument("target", EntityArgument.player())
                                                .then(Commands.argument("tier", StringArgumentType.word())
                                                        .suggests((ctx, sb) -> {
                                                            for (PassType t : PassType.values()) {
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
                        .then(Commands.literal("resetAll")
                                .requires(src -> {
                                    try {
                                        ServerPlayerEntity p = src.getPlayerOrException();
                                        return AdminConfig.isAdmin(p.getName().getString());
                                    } catch (Exception e) {
                                        return true; // consola
                                    }
                                })
                                .executes(CommandResetAll::resetAll)
                        )
                        // stats
                        .then(Commands.literal("stats")
                                .executes(CommandRegistry::showStats)
                        )

                        .then(Commands.literal("seen")
                                .requires(src -> {
                                    try {
                                        ServerPlayerEntity p = src.getPlayerOrException();
                                        return AdminConfig.isAdmin(p.getName().getString());
                                    } catch (Exception e) {
                                        return true; // permite que la consola también lo ejecute
                                    }
                                })
                                .executes(ctx -> {
                                    ctx.getSource().sendFailure(
                                            new StringTextComponent(TextFormatting.RED +
                                                    "Uso correcto: /rlv seen <Player>")
                                    );
                                    return 0;
                                })
                                // 2) Ahora definimos el argumento "playerName" con sugerencias de jugadores online:
                                .then(Commands.argument("playerName", StringArgumentType.word())
                                        .suggests(playerNameSuggestions()) // <-- aquí registramos nuestras sugerencias
                                        .executes(CommandSeen::seen)       // <-- el método que tienes implementado
                                )
                        )

                        .then(Commands.literal("top")
                                .requires(src -> {
                                    try {
                                        ServerPlayerEntity p = src.getPlayerOrException();
                                        return AdminConfig.isAdmin(p.getName().getString());
                                    } catch (Exception e) {
                                        return true; // consola
                                    }
                                })
                                .executes(CommandTop::top)
                        )

                        .then(Commands.literal("give")
                                .requires(src -> {
                                    try {
                                        ServerPlayerEntity p = src.getPlayerOrException();
                                        return AdminConfig.isAdmin(p.getName().getString());
                                    } catch (Exception e) {
                                        return true; // consola
                                    }
                                })
                                .then(Commands.argument("target", EntityArgument.player())
                                        .then(Commands.argument("item", StringArgumentType.word())
                                                .suggests(ITEM_SUGGESTIONS)
                                                .executes(ctx -> {
                                                    ServerPlayerEntity target = EntityArgument.getPlayer(ctx, "target");
                                                    String itemId = StringArgumentType.getString(ctx, "item").toLowerCase();
                                                    int count = 1; // valor por defecto
                                                    return CommandGiveHelper.giveItem(target, itemId, count, ctx.getSource());
                                                })
                                                .then(
                                                        Commands.argument("count", IntegerArgumentType.integer(1))
                                                                .suggests((CommandContext<CommandSource> ctx, SuggestionsBuilder builder) -> {
                                                                    // Sugerimos explicitamente los valores 1, 2 y 3.
                                                                    builder.suggest("1");
                                                                    builder.suggest("2");
                                                                    builder.suggest("3");
                                                                    return builder.buildFuture();
                                                                })
                                                                .executes(ctx -> {
                                                                    ServerPlayerEntity target = EntityArgument.getPlayer(ctx, "target");
                                                                    String itemId = StringArgumentType.getString(ctx, "item").toLowerCase();
                                                                    int count = IntegerArgumentType.getInteger(ctx, "count");
                                                                    return CommandGiveHelper.giveItem(target, itemId, count, ctx.getSource());
                                                                })
                                                )

                                        )
                                )
                        )

                        // addexp / setexp / removeexp
                        .then(Commands.literal("addexp")
                                .requires(src -> {
                                    try {
                                        ServerPlayerEntity p = src.getPlayerOrException();
                                        return AdminConfig.isAdmin(p.getName().getString());
                                    } catch (Exception e) {
                                        return true; // consola
                                    }
                                })
                                .executes(ctx -> {
                                    ctx.getSource().sendFailure(
                                            new StringTextComponent(TextFormatting.RED +
                                                    "Uso correcto: /rlv addexp <Player> <Amount>")
                                    );
                                    return 0;
                                })
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                .executes(ctx -> modifyExp(ctx, Mode.ADD))
                                        )
                                )
                        )

                        .then(Commands.literal("setexp")
                                .requires(src -> {
                                    try {
                                        ServerPlayerEntity p = src.getPlayerOrException();
                                        return AdminConfig.isAdmin(p.getName().getString());
                                    } catch (Exception e) {
                                        return true; // consola
                                    }
                                })
                                .executes(ctx -> {
                                    ctx.getSource().sendFailure(
                                            new StringTextComponent(TextFormatting.RED +
                                                    "Uso correcto: /rlv setexp <Player> <Amount>")
                                    );
                                    return 0;
                                })
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                .executes(ctx -> modifyExp(ctx, Mode.SET))
                                        )
                                )
                        )
                        .then(Commands.literal("removeexp")
                                .requires(src -> {
                                    try {
                                        ServerPlayerEntity p = src.getPlayerOrException();
                                        return AdminConfig.isAdmin(p.getName().getString());
                                    } catch (Exception e) {
                                        return true; // consola
                                    }
                                })
                                .executes(ctx -> {
                                    ctx.getSource().sendFailure(
                                            new StringTextComponent(TextFormatting.RED +
                                                    "Uso correcto: /rlv removeexp <Player> <Amount>")
                                    );
                                    return 0;
                                })
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                .executes(ctx -> modifyExp(ctx, Mode.REMOVE))
                                        )
                                )
                        )
                        // addlevel / setlevel / removelevel
                        .then(Commands.literal("addlevel")
                                .requires(src -> {
                                    try {
                                        ServerPlayerEntity p = src.getPlayerOrException();
                                        return AdminConfig.isAdmin(p.getName().getString());
                                    } catch (Exception e) {
                                        return true; // consola
                                    }
                                })
                                .executes(ctx -> {
                                    ctx.getSource().sendFailure(
                                            new StringTextComponent(TextFormatting.RED +
                                                    "Uso correcto: /rlv addlevel <Player> <Amount>")
                                    );
                                    return 0;
                                })
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(ctx -> modifyLevel(ctx, Mode.ADD))
                                        )
                                )
                        )
                        .then(Commands.literal("setlevel")
                                .requires(src -> {
                                    try {
                                        ServerPlayerEntity p = src.getPlayerOrException();
                                        return AdminConfig.isAdmin(p.getName().getString());
                                    } catch (Exception e) {
                                        return true; // consola
                                    }
                                })
                                .executes(ctx -> {
                                    ctx.getSource().sendFailure(
                                            new StringTextComponent(TextFormatting.RED +
                                                    "Uso correcto: /rlv setlevel <Player> <Amount>")
                                    );
                                    return 0;
                                })
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(ctx -> modifyLevel(ctx, Mode.SET))
                                        )
                                )
                        )
                        .then(Commands.literal("removelevel")
                                .requires(src -> {
                                    try {
                                        ServerPlayerEntity p = src.getPlayerOrException();
                                        return AdminConfig.isAdmin(p.getName().getString());
                                    } catch (Exception e) {
                                        return true; // consola
                                    }
                                })
                                .executes(ctx -> {
                                    ctx.getSource().sendFailure(
                                            new StringTextComponent(TextFormatting.RED +
                                                    "Uso correcto: /rlv removelevel <Player> <Amount>")
                                    );
                                    return 0;
                                })
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(ctx -> modifyLevel(ctx, Mode.REMOVE))
                                        )
                                )
                        )
                        // reset
                        .then(Commands.literal("reset")
                                .requires(src -> {
                                    try {
                                        ServerPlayerEntity p = src.getPlayerOrException();
                                        return AdminConfig.isAdmin(p.getName().getString());
                                    } catch (Exception e) {
                                        return true; // consola
                                    }
                                })
                                .executes(ctx -> {
                                    ctx.getSource().sendFailure(
                                            new StringTextComponent(TextFormatting.RED +
                                                    "Uso correcto: /rlv reset <Player>")
                                    );
                                    return 0;
                                })
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(CommandRegistry::resetStats)
                                )
                        )
                        // reload
                        .then(Commands.literal("reload")
                                .requires(src -> {
                                    try {
                                        ServerPlayerEntity p = src.getPlayerOrException();
                                        return AdminConfig.isAdmin(p.getName().getString());
                                    } catch (Exception e) {
                                        return true; // consola
                                    }
                                })
                                .executes(ctx -> {
                                    ConfigLoader.load();
                                    MysteryBoxesConfig.reload();
                                    ExpConfig.reload();
                                    MultiplierState.load();
                                    FlatFilePlayerDataManager.getInstance().reload();
                                    AdminConfig.load();
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
                        .then(Commands.literal("Multiplier")
                                .requires(src -> {
                                    try {
                                        ServerPlayerEntity p = src.getPlayerOrException();
                                        return AdminConfig.isAdmin(p.getName().getString());
                                    } catch (Exception e) {
                                        return true; // consola
                                    }
                                })                              // GLOBAL
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
                                .executes(CommandRegistry::showMultipliers)
                        )
                        .then(Commands.literal("menu")
                                    .executes(ctx -> {
                                    ServerPlayerEntity player = ctx.getSource().getPlayerOrException();
                                    rl.sage.rangerlevels.gui.MainMenu.open(player);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("rewards")
                                    .executes(ctx -> {
                                    ServerPlayerEntity player = ctx.getSource().getPlayerOrException();
                                    rl.sage.rangerlevels.gui.rewards.RewardsMenu.open(player);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("purga")
                                .requires(src -> {
                                    try {
                                        ServerPlayerEntity p = src.getPlayerOrException();
                                        return AdminConfig.isAdmin(p.getName().getString());
                                    } catch (Exception e) {
                                        return true; // consola
                                    }
                                })
                                    .executes(ctx -> {
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
                                .requires(src -> {
                                    try {
                                        ServerPlayerEntity p = src.getPlayerOrException();
                                        return AdminConfig.isAdmin(p.getName().getString());
                                    } catch (Exception e) {
                                        return true; // consola
                                    }
                                })
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
                        .then(Commands.literal("admins")
                                .requires(src -> {
                                    try {
                                        ServerPlayerEntity p = src.getPlayerOrException();
                                        return AdminConfig.isAdmin(p.getName().getString());
                                    } catch (Exception e) {
                                        return true; // consola
                                    }
                                })
                                .then(Commands.literal("add")
                                        .then(Commands.argument("nick", StringArgumentType.word())
                                                .executes(ctx -> {
                                                    String nick = StringArgumentType.getString(ctx, "nick");
                                                    boolean added = AdminConfig.addAdmin(nick);
                                                    ctx.getSource().sendSuccess(
                                                            new StringTextComponent(added
                                                                    ? "§aJugador añadido a admins: " + nick
                                                                    : "§eEse jugador ya es admin."), false);
                                                    return 1;
                                                })
                                        )
                                )
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("nick", StringArgumentType.word())
                                                .executes(ctx -> {
                                                    String nick = StringArgumentType.getString(ctx, "nick");
                                                    boolean removed = AdminConfig.removeAdmin(nick);
                                                    ctx.getSource().sendSuccess(
                                                            new StringTextComponent(removed
                                                                    ? "§cJugador removido de admins: " + nick
                                                                    : "§eEse jugador no era admin."), false);
                                                    return 1;
                                                })
                                        )
                                )
                        )


        );

        // alias /rlv
        dispatcher.register(Commands.literal("rlv").redirect(base));


        dispatcher.register(Commands.literal("rlv")
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayerOrException();
                            rl.sage.rangerlevels.gui.MainMenu.open(player);
                            return 1;
                        })
        );
        dispatcher.register(Commands.literal("pase")
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayerOrException();
                    rl.sage.rangerlevels.gui.MainMenu.open(player);
                    return 1;
                }));



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
    }
    private static int showPassInfo(ServerPlayerEntity player) {
        IPassCapability cap = PassCapabilities.get(player);
        PassType pass = PassManager.getCurrentPass(player);

        // 1) Cabecera: gradiente + color dorado, luego salto de línea
        IFormattableTextComponent header = pass.getGradientDisplayName()
                .withStyle(style -> style.withColor(TextFormatting.GOLD))
                .append((IFormattableTextComponent) new StringTextComponent("\n"));

        // 2) Beneficios: “Beneficios:\n” en amarillo, y luego la descripción en gris + salto
        IFormattableTextComponent benefits = (IFormattableTextComponent) new StringTextComponent("Beneficios:\n")
                .withStyle(style -> style.withColor(TextFormatting.YELLOW))
                .append((IFormattableTextComponent) new StringTextComponent(" - " + pass.getDescription() + "\n")
                        .withStyle(style -> style.withColor(TextFormatting.GRAY)));

        // 3) Si el pase no es FREE, agregamos información de expiración
        IFormattableTextComponent expiryInfo = (IFormattableTextComponent) new StringTextComponent("");
        if (pass != PassType.FREE) {
            long expiresAt = cap.getExpiresAt();
            String expiresStr;
            if (expiresAt == Long.MAX_VALUE) {
                expiresStr = "Nunca"; // pase indefinido
            } else {
                expiresStr = java.time.format.DateTimeFormatter
                        .ofPattern("yyyy-MM-dd HH:mm")
                        .withZone(java.time.ZoneOffset.UTC)
                        .format(java.time.Instant.ofEpochMilli(expiresAt)) + " UTC";
            }
            expiryInfo = (IFormattableTextComponent) new StringTextComponent("Expira: ")
                    .withStyle(style -> style.withColor(TextFormatting.AQUA))
                    .append((IFormattableTextComponent) new StringTextComponent(expiresStr + "\n")
                            .withStyle(style -> style.withColor(TextFormatting.GRAY)));
        }

        // 4) Envío de mensajes al jugador
        player.sendMessage(header, player.getUUID());
        player.sendMessage(benefits, player.getUUID());
        if (pass != PassType.FREE) {
            player.sendMessage(expiryInfo, player.getUUID());
        }

        // 5) Sonido de confirmación
        PlayerSoundUtils.playSoundToPlayer(
                player,
                SoundEvents.NOTE_BLOCK_CHIME,
                SoundCategory.MASTER,
                1.0f,
                0.8f
        );

        return 1;
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
        sendDecoratedPass(player, PassType.SUPER,
                urls.getOrDefault("super", ""), uuid);

        // ╟────────────────────────────────╢
        player.sendMessage(
                new StringTextComponent(" ╟───────────────────────────────╢\n")
                        .withStyle(Style.EMPTY.withColor(TextFormatting.GRAY)),
                uuid
        );

        // ║   • Ultra Pass
        sendDecoratedPass(player, PassType.ULTRA,
                urls.getOrDefault("ultra", ""), uuid);

        // ╟────────────────────────────────╢
        player.sendMessage(
                new StringTextComponent(" ╟───────────────────────────────╢\n")
                        .withStyle(Style.EMPTY.withColor(TextFormatting.GRAY)),
                uuid
        );

        // ║   • Master Pass
        sendDecoratedPass(player, PassType.MASTER,
                urls.getOrDefault("master", ""), uuid);

        // ╚════════════════════════════════╝
        player.sendMessage(
                new StringTextComponent(" ╚═══════════════════════════════╝")
                        .withStyle(Style.EMPTY.withColor(TextFormatting.GRAY)),
                uuid
        );
        PlayerSoundUtils.playSoundToPlayer(
                player,
                SoundEvents.NOTE_BLOCK_CHIME,
                SoundCategory.MASTER,
                1.0f,
                1.0f
        );
        return 1;
    }


    /** Envía una línea de pase con viñeta, hover y click decorados */
    private static void sendDecoratedPass(ServerPlayerEntity player, PassType type, String url, UUID uuid) {
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
        PlayerSoundUtils.playSoundToPlayer(
                player,
                SoundEvents.NOTE_BLOCK_CHIME,
                SoundCategory.MASTER,
                1.0f,
                0.8f
        );
    }


    private static int setPassTier(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        // Obtenemos al jugador objetivo y el nombre del tier
        ServerPlayerEntity target = EntityArgument.getPlayer(ctx, "target");
        String tierName = StringArgumentType.getString(ctx, "tier").toUpperCase();

        // Intentamos parsear el PassType
        PassType chosen;
        try {
            chosen = PassType.valueOf(tierName);
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(
                    new StringTextComponent(TextFormatting.RED + "Tier inválido: " + tierName)
            );
            return 0;
        }

        // 1) Asignamos el nuevo tier en la capability y lo hacemos "indefinido"
        IPassCapability cap = PassCapabilities.get(target);
        cap.setTier(chosen.getTier());
        cap.setExpiresAt(Long.MAX_VALUE); // Pase indefinido
        cap.syncToClient(target); // por si en un futuro hay cliente que necesite saberlo

        // 2) Mensaje de confirmación en el chat del ejecutor
        ctx.getSource().sendSuccess(
                new StringTextComponent(
                        TextFormatting.GREEN + "Tier de pase de " +
                                TextFormatting.AQUA + target.getName().getString() +
                                TextFormatting.GREEN + " establecido a " +
                                TextFormatting.GOLD + chosen.name().toLowerCase()
                ),
                true
        );

        // 3) Envío de Title y Subtitle al jugador objetivo
        // 3a) Título: "Pase Ranger" (en dorado)
        ITextComponent titleText = new StringTextComponent("Pase Ranger")
                .withStyle(TextFormatting.GOLD);

        // 3b) Subtítulo: "Ahora tienes el <PASE>" (mostrando el nombre gradient del PassType)
        ITextComponent subTitleText = new StringTextComponent("Ahora tienes el ")
                .withStyle(TextFormatting.AQUA)
                .append(chosen.getGradientDisplayName());

        // 3c) Creamos los paquetes para TITLE y SUBTITLE
        STitlePacket packetTitle = new STitlePacket(
                STitlePacket.Type.TITLE,
                titleText
        );
        STitlePacket packetSubTitle = new STitlePacket(
                STitlePacket.Type.SUBTITLE,
                subTitleText
        );

        // 3d) Enviamos los paquetes al jugador
        target.connection.send(packetTitle);
        target.connection.send(packetSubTitle);

        PlayerSoundUtils.playSoundToPlayer(
                target,
                SoundEvents.NOTE_BLOCK_CHIME,
                SoundCategory.MASTER,
                1.0f,
                0.8f
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
        PlayerSoundUtils.playSoundToPlayer(
                player,
                SoundEvents.NOTE_BLOCK_CHIME,
                SoundCategory.MASTER,
                1.0f,
                0.8f
        );
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
        PlayerSoundUtils.playSoundToPlayer(
                target,
                SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE,
                SoundCategory.MASTER,
                1.0f,
                0.5f
        );

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

        // ✨ Ahora obtenemos el pase actual a través de PassManager
        PassType pass = PassManager.getCurrentPass(player);

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

        PlayerSoundUtils.playSoundToPlayer(
                player,
                SoundEvents.ITEM_BREAK,
                SoundCategory.MASTER,
                1.0f,
                0.5f
        );
        return 1;
    }

    /**
     * SuggestionProvider que propone todos los nicknames de jugadores actualmente conectados.
     */
    private static SuggestionProvider<CommandSource> playerNameSuggestions() {
        return (context, builder) -> {
            MinecraftServer server = context.getSource().getServer();
            if (server != null) {
                for (ServerPlayerEntity online : server.getPlayerList().getPlayers()) {
                    // Sugerimos solo si el texto parcial coincide
                    builder.suggest(online.getName().getString());
                }
            }
            return builder.buildFuture();
        };
    }
    private static final SuggestionProvider<CommandSource> ITEM_SUGGESTIONS =
            (CommandContext<CommandSource> context, SuggestionsBuilder builder) -> {
                for (String id : CustomItemRegistry.getAllIds()) {
                    builder.suggest(id);
                }
                return builder.buildFuture();
            };
}
