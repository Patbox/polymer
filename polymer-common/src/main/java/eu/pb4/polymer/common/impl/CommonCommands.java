package eu.pb4.polymer.common.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static net.minecraft.server.command.CommandManager.literal;

@SuppressWarnings("ResultOfMethodCallIgnored")
@ApiStatus.Internal
public class CommonCommands {
    public static final List<BiConsumer<LiteralArgumentBuilder<ServerCommandSource>, CommandRegistryAccess>> COMMANDS_DEV = new ArrayList<>();
    public static final List<BiConsumer<LiteralArgumentBuilder<ServerCommandSource>, CommandRegistryAccess>> COMMANDS = new ArrayList<>();
    private static final Text[] ABOUT_PLAYER;
    private static final Text[] ABOUT_COLORLESS;

    static {
        var about = new ArrayList<Text>();
        var extraData = Text.empty();
        try {
            extraData.append(Text.literal("[")
                    .append(Text.literal("Contributors")
                            .setStyle(Style.EMPTY.withColor(Formatting.AQUA)
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Text.literal(String.join("\n", CommonImpl.CONTRIBUTORS))
                                    ))
                            ))
                    .append("] ")
            ).append(Text.literal("[")
                    .append(Text.literal("GitHub")
                            .setStyle(Style.EMPTY.withColor(Formatting.BLUE).withUnderline(true)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, CommonImpl.GITHUB_URL))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Text.literal(CommonImpl.GITHUB_URL)
                                    ))
                            ))
                    .append("]")).setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY));

            about.add(Text.empty()
                    .append(Text.literal("Polymer ").setStyle(Style.EMPTY.withColor(0xb4ff90).withBold(true)))
                    .append(Text.literal(CommonImpl.VERSION).setStyle(Style.EMPTY.withColor(Formatting.WHITE))));

            about.add(Text.literal("» " + CommonImpl.DESCRIPTION).setStyle(Style.EMPTY.withColor(Formatting.GRAY)));

            about.add(extraData);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        ABOUT_COLORLESS = about.toArray(new Text[0]);

        if (CommonImpl.MINIMAL_ABOUT || CommonImplUtils.ICON.length == 0) {
            ABOUT_PLAYER = ABOUT_COLORLESS;
        } else {
            var output = new ArrayList<Text>();
            about.clear();
            try {
                about.add(Text.literal("Polymer").setStyle(Style.EMPTY.withColor(0xb4ff90).withBold(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, CommonImpl.GITHUB_URL))));
                about.add(Text.literal("Version: ").setStyle(Style.EMPTY.withColor(0xf7e1a7))
                        .append(Text.literal(CommonImpl.VERSION).setStyle(Style.EMPTY.withColor(Formatting.WHITE))));

                about.add(extraData);
                about.add(Text.empty());

                var desc = new ArrayList<>(List.of(CommonImpl.DESCRIPTION.split(" ")));

                if (desc.size() > 0) {
                    StringBuilder descPart = new StringBuilder();
                    while (!desc.isEmpty()) {
                        (descPart.isEmpty() ? descPart : descPart.append(" ")).append(desc.remove(0));

                        if (descPart.length() > 16) {
                            about.add(Text.literal(descPart.toString()).setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
                            descPart = new StringBuilder();
                        }
                    }

                    if (descPart.length() > 0) {
                        about.add(Text.literal(descPart.toString()).setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
                    }
                }

                if (CommonImplUtils.ICON.length > about.size() + 2) {
                    int a = 0;
                    for (int i = 0; i < CommonImplUtils.ICON.length; i++) {
                        if (i == (CommonImplUtils.ICON.length - about.size() - 1) / 2 + a && a < about.size()) {
                            output.add(CommonImplUtils.ICON[i].copy().append("  ").append(about.get(a++)));
                        } else {
                            output.add(CommonImplUtils.ICON[i]);
                        }
                    }
                } else {
                    Collections.addAll(output, CommonImplUtils.ICON);
                    output.addAll(about);
                }
            } catch (Exception e) {
                e.printStackTrace();
                var invalid = Text.literal("/!\\ [ Invalid about mod info ] /!\\").setStyle(Style.EMPTY.withColor(0xFF0000).withItalic(true));

                output.add(invalid);
                about.add(invalid);
            }

            ABOUT_PLAYER = output.toArray(new Text[0]);
        }
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess access) {
        var command = literal("polymer")
                .requires(CommonImplUtils.permission("command.core", CommonImpl.CORE_COMMAND_MINIMAL_OP))
                .executes(CommonCommands::about);

        for (var consumer : COMMANDS) {
            consumer.accept(command, access);
        }

        if (CommonImpl.DEVELOPER_MODE) {
            var dev = literal("dev")
                    .requires(CommonImplUtils.permission("command.dev", 3));

            for (var consumer : COMMANDS_DEV) {
                consumer.accept(dev, access);
            }

            dev.then(literal("is_bedrock").executes((ctx) -> {
                ctx.getSource().sendFeedback(() -> Text.literal("Bedrock: " + PolymerCommonUtils.isBedrockPlayer(ctx.getSource().getPlayer())), false);
                return 0;
            }));

            command.then(dev);
        }

        dispatcher.register(command);
    }

    private static int about(CommandContext<ServerCommandSource> context) {
        for (var text : (context.getSource().getEntity() instanceof ServerPlayerEntity ? ABOUT_PLAYER : ABOUT_COLORLESS)) {
            context.getSource().sendFeedback(() -> text, false);
        }

        return 0;
    }
}
