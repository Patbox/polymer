package eu.pb4.polymer.common.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import org.jetbrains.annotations.ApiStatus;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.literal;

@SuppressWarnings("ResultOfMethodCallIgnored")
@ApiStatus.Internal
public class CommonCommands {
    public static final List<BiConsumer<LiteralArgumentBuilder<CommandSourceStack>, CommandBuildContext>> COMMANDS_DEV = new ArrayList<>();
    public static final List<BiConsumer<LiteralArgumentBuilder<CommandSourceStack>, CommandBuildContext>> COMMANDS = new ArrayList<>();
    private static final Component[] ABOUT_PLAYER;
    private static final Component[] ABOUT_COLORLESS;

    static {
        var about = new ArrayList<Component>();
        var extraData = Component.empty();
        try {
            extraData.append(Component.literal("[")
                    .append(Component.literal("Contributors")
                            .setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)
                                    .withHoverEvent(new HoverEvent.ShowText(
                                            Component.literal(String.join("\n", CommonImpl.CONTRIBUTORS))
                                    ))
                            ))
                    .append("] ")
            ).append(Component.literal("[")
                    .append(Component.literal("GitHub")
                            .setStyle(Style.EMPTY.withColor(ChatFormatting.BLUE).withUnderlined(true)
                                    .withClickEvent(new ClickEvent.OpenUrl(URI.create(CommonImpl.GITHUB_URL)))
                                    .withHoverEvent(new HoverEvent.ShowText(
                                            Component.literal(CommonImpl.GITHUB_URL)
                                    ))
                            ))
                    .append("]")).setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY));

            about.add(Component.empty()
                    .append(Component.literal("Polymer ").setStyle(Style.EMPTY.withColor(0xb4ff90).withBold(true)))
                    .append(Component.literal(CommonImpl.VERSION).setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE))));

            about.add(Component.literal("» " + CommonImpl.DESCRIPTION).setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));

            about.add(extraData);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        ABOUT_COLORLESS = about.toArray(new Component[0]);

        if (CommonImpl.MINIMAL_ABOUT || CommonImplUtils.ICON.length == 0) {
            ABOUT_PLAYER = ABOUT_COLORLESS;
        } else {
            var output = new ArrayList<Component>();
            about.clear();
            try {
                about.add(Component.literal("Polymer").setStyle(Style.EMPTY.withColor(0xb4ff90).withBold(true).withClickEvent(new ClickEvent.OpenUrl(URI.create(CommonImpl.GITHUB_URL)))));
                about.add(Component.literal("Version: ").setStyle(Style.EMPTY.withColor(0xf7e1a7))
                        .append(Component.literal(CommonImpl.VERSION).setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE))));

                about.add(extraData);
                about.add(Component.empty());

                var desc = new ArrayList<>(List.of(CommonImpl.DESCRIPTION.split(" ")));

                if (desc.size() > 0) {
                    StringBuilder descPart = new StringBuilder();
                    while (!desc.isEmpty()) {
                        (descPart.isEmpty() ? descPart : descPart.append(" ")).append(desc.remove(0));

                        if (descPart.length() > 16) {
                            about.add(Component.literal(descPart.toString()).setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
                            descPart = new StringBuilder();
                        }
                    }

                    if (descPart.length() > 0) {
                        about.add(Component.literal(descPart.toString()).setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
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
                var invalid = Component.literal("/!\\ [ Invalid about mod info ] /!\\").setStyle(Style.EMPTY.withColor(0xFF0000).withItalic(true));

                output.add(invalid);
                about.add(invalid);
            }

            ABOUT_PLAYER = output.toArray(new Component[0]);
        }
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext access) {
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
                ctx.getSource().sendSuccess(() -> Component.literal("Bedrock: " + PolymerCommonUtils.isBedrockPlayer(ctx.getSource().getPlayer().connection)), false);
                return 0;
            }));

            command.then(dev);
        }

        dispatcher.register(command);
    }

    private static int about(CommandContext<CommandSourceStack> context) {
        for (var text : (context.getSource().getEntity() instanceof ServerPlayer ? ABOUT_PLAYER : ABOUT_COLORLESS)) {
            context.getSource().sendSuccess(() -> text, false);
        }

        return 0;
    }
}
