package eu.pb4.polymer.core.impl;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.core.api.block.BlockMapper;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.api.other.PolymerStat;
import eu.pb4.polymer.core.api.utils.PolymerSyncUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.networking.PolymerServerProtocol;
import eu.pb4.polymer.core.impl.ui.CreativeTabListUi;
import eu.pb4.polymer.core.impl.ui.CreativeTabUi;
import eu.pb4.polymer.core.impl.ui.PotionUi;
import eu.pb4.polymer.core.mixin.block.PalettedContainerAccessor;
import eu.pb4.polymer.networking.impl.ExtConnection;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TextComponentTagVisitor;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.dialog.ActionButton;
import net.minecraft.server.dialog.CommonButtonData;
import net.minecraft.server.dialog.CommonDialogData;
import net.minecraft.server.dialog.DialogAction;
import net.minecraft.server.dialog.MultiActionDialog;
import net.minecraft.server.dialog.NoticeDialog;
import net.minecraft.server.dialog.action.StaticAction;
import net.minecraft.server.dialog.body.DialogBody;
import net.minecraft.server.dialog.body.ItemBody;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.stats.StatType;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.ApiStatus;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@SuppressWarnings("ResultOfMethodCallIgnored")
@ApiStatus.Internal
public class Commands {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> command, CommandBuildContext access) {
        command.then(literal("stats")
                        .requires(CommonImplUtils.permission("command.stats", 0))
                        .executes(Commands::statsGeneral)
                        .then(argument("type", ResourceArgument.resource(access, Registries.STAT_TYPE)).executes(Commands::stats))
                )
                .then(literal("effects")
                        .requires(CommonImplUtils.permission("command.effects", 0))
                        .executes(Commands::effects)
                )
                .then(literal("client-item")
                        .requires(CommonImplUtils.permission("command.client-item", 3))
                        .executes(Commands::displayClientItem)
                        .then(literal("get").executes(Commands::getClientItem))
                )
                .then(literal("export-registry")
                        .requires(CommonImplUtils.permission("command.export-registry", 3))
                        .executes(Commands::dumpRegistries)
                )
                .then(literal("target-block")
                        .requires(CommonImplUtils.permission("command.target-block", 3))
                        .executes(Commands::targetBlock)
                )
                .then(literal("target-item")
                        .requires(CommonImplUtils.permission("command.target-item", 3))
                        .executes(Commands::targetItem)
                )
                .then(literal("creative")
                        .requires(CommonImplUtils.permission("command.creative", 0))
                        .then(argument("itemGroup", IdentifierArgument.id())
                                .suggests((context, builder) -> {
                                    var remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

                                    var groups = PolymerItemGroupUtils.getItemGroups(context.getSource().getPlayerOrException());

                                    SharedSuggestionProvider.filterResources(groups, remaining, PolymerItemGroupUtils::getId, group -> builder.suggest(PolymerItemGroupUtils.getId(group).toString(), group.getDisplayName()));
                                    return builder.buildFuture();
                                })
                                .executes(Commands::creativeTab)
                        )
                        .executes(Commands::creativeTab));
    }

    public static void registerDev(LiteralArgumentBuilder<CommandSourceStack> dev) {
        dev
                .then(literal("reload-world")
                        .executes((ctx) -> {
                            PolymerUtils.reloadWorld(ctx.getSource().getPlayer());
                            return 0;
                        })
                )
                .then(literal("get-mapper")
                        .executes((ctx) -> {
                            ctx.getSource().sendSuccess(() -> Component.literal(BlockMapper.getFrom(ctx.getSource().getPlayer()).getMapperName()), false);
                            return 0;
                        })
                )
                .then(literal("reset-mapper")
                        .executes((ctx) -> {
                            BlockMapper.resetMapper(ctx.getSource().getPlayer());
                            return 0;
                        })
                )
                .then(literal("run-sync")
                        .executes((ctx) -> {
                            PolymerSyncUtils.synchronizePolymerRegistries(ctx.getSource().getPlayer().connection);
                            return 0;
                        }))
                .then(literal("protocol-info")
                        .executes((ctx) -> {
                            ctx.getSource().sendSuccess(() -> Component.literal("Protocol supported by your client:"), false);
                            for (var entry : ExtConnection.of(ctx.getSource().getPlayer().connection).polymerNet$getSupportMap().object2IntEntrySet()) {
                                ctx.getSource().sendSuccess(() -> Component.literal("- " + entry.getKey() + " = " + entry.getIntValue()), false);
                            }
                            return 0;
                        })
                )
                .then(literal("validate_states")
                        .executes((ctx) -> {
                            PolymerServerProtocol.sendDebugValidateStatesPackets(ctx.getSource().getPlayer().connection);
                            return 0;
                        })
                )
                .then(literal("set-pack-status")
                        .then(argument("status", BoolArgumentType.bool())
                                .then(argument("uuid", UuidArgument.uuid())
                                        .executes((ctx) -> {
                                            var status = ctx.getArgument("status", Boolean.class);
                                            PolymerCommonUtils.setHasResourcePack(ctx.getSource().getPlayerOrException(), UuidArgument.getUuid(ctx, "uuid"), status);
                                            ctx.getSource().sendSuccess(() -> Component.literal("New resource pack status: " + status), false);
                                            return 0;
                                        }))
                        )
                )
                .then(literal("get-pack-status")
                        .executes((ctx) -> {
                            var status = PolymerUtils.hasResourcePack(ctx.getSource().getPlayer(), PolymerResourcePackUtils.getMainUuid());
                            ctx.getSource().sendSuccess(() -> Component.literal("Resource pack status: " + status), false);
                            return 0;
                        })
                )
                .then(literal("chunk_section_info")
                        .executes((ctx) -> {
                            var chunk = ctx.getSource().getLevel().getChunk(ctx.getSource().getPlayer().blockPosition());
                            var s = chunk.getSection(ctx.getSource().getLevel().getSectionIndex(ctx.getSource().getPlayer().getBlockY()));

                            var a = ((PalettedContainerAccessor<BlockState>) s.getStates()).getData();

                            ctx.getSource().sendSuccess(() -> Component.literal("Chunk: " + chunk.getPos() + " Palette: " + a.palette() + " | " + " Storage: " + a.storage() + " | Bits: " + a.storage().getBits()), false);
                            return 0;
                        })
                );
    }

    private static int targetBlock(CommandContext<CommandSourceStack> context) {
        var raycast = (BlockHitResult) context.getSource().getPlayer().pick(10, 0, true);

        var builder = new StringBuilder();
        var state = context.getSource().getLevel().getBlockState(raycast.getBlockPos());

        builder.append(BuiltInRegistries.BLOCK.getKey(state.getBlock()));

        if (!state.getBlock().getStateDefinition().getProperties().isEmpty()) {
            builder.append("[");
            var iterator = state.getBlock().getStateDefinition().getProperties().iterator();

            while (iterator.hasNext()) {
                var property = iterator.next();
                builder.append(property.getName());
                builder.append("=");
                builder.append(((Property) property).getName(state.getValue(property)));

                if (iterator.hasNext()) {
                    builder.append(",");
                }
            }
            builder.append("]");
        }

        context.getSource().sendSuccess(() -> Component.literal(builder.toString())
                .setStyle(Style.EMPTY
                        .withHoverEvent(new HoverEvent.ShowText(Component.translatable("chat.copy.click")))
                        .withClickEvent(new ClickEvent.CopyToClipboard(builder.toString()))), false);

        return 0;
    }

    private static int targetItem(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var itemStack = context.getSource().getPlayerOrException().getMainHandItem();
        var id = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
        context.getSource().sendSuccess(() -> Component.literal(id.toString())
                .setStyle(Style.EMPTY
                        .withHoverEvent(new HoverEvent.ShowText(Component.translatable("chat.copy.click")))
                        .withClickEvent(new ClickEvent.CopyToClipboard(id.toString()))), false);        return 0;
    }

    private static int dumpRegistries(CommandContext<CommandSourceStack> context) {
        var path = PolymerImplUtils.dumpRegistry();
        if (path != null) {
            context.getSource().sendSuccess(() -> Component.literal("Exported registry state as " + path), false);
        } else {
            context.getSource().sendFailure(Component.literal("Couldn't export registry!"));
        }
        return 0;
    }

    private static int effects(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        new PotionUi(context.getSource().getPlayer());
        return 1;
    }

    private static int statsGeneral(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayer();

        var list = new ArrayList<ActionButton>();

        for (var statType : BuiltInRegistries.STAT_TYPE) {
            list.add(new ActionButton(new CommonButtonData(Component.literal(BuiltInRegistries.STAT_TYPE.getKey(statType).toString()), 150),
                    Optional.of(new StaticAction(new ClickEvent.RunCommand("polymer stats " + BuiltInRegistries.STAT_TYPE.getKey(statType))))));
        }

        player.openDialog(Holder.direct(new MultiActionDialog(new CommonDialogData(
                Component.translatable("gui.stats"),
                Optional.empty(),
                true, true,
                DialogAction.CLOSE,
                List.of(),
                List.of()
        ), list, Optional.of(new ActionButton(new CommonButtonData(CommonComponents.GUI_DONE, 150), Optional.empty())), 1)));

        return 1;
    }

    private static int stats(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayer();

        var list = new ArrayList<DialogBody>();

        var type = (StatType<Object>) ResourceArgument.getResource(context, "type", Registries.STAT_TYPE).value();
        for (var statObj : type.getRegistry()) {
            if (PolymerUtils.isServerOnly(type.getRegistry(), statObj) && type.contains(statObj)) {
                var stat = type.get(statObj);

                var statVal = player.getStats().getValue(stat);

                ItemStack stack = ItemStack.EMPTY;

                Component title;

                if (statObj instanceof Identifier stat1) {
                    title = PolymerStat.getName(stat1);
                } else if (statObj instanceof Item item) {
                    title = item.getName();
                    stack = item.getDefaultInstance();
                } else if (statObj instanceof Block item) {
                    title = item.getName();
                    stack = item.asItem().getDefaultInstance();
                } else if (statObj instanceof EntityType item) {
                    title = item.getDescription();
                } else {
                    title = Component.translatable(Util.makeDescriptionId(type.getRegistry().key().identifier().getPath(), type.getRegistry().getKey(statObj)));
                }

                var text = Component.empty().append(title).append(Component.literal(": ").withStyle(ChatFormatting.GRAY)).append(Component.literal(stat.format(statVal)).withStyle(ChatFormatting.WHITE));

                if (stack.isEmpty()) {
                    list.add(new PlainMessage(text, 200));
                } else {
                    list.add(new ItemBody(stack, Optional.of(new PlainMessage(text, 200)), true, true, 16, 16));
                }
            }
        }

        player.openDialog(Holder.direct(new NoticeDialog(new CommonDialogData(
                Component.translatable("gui.stats"),
                Optional.empty(),
                true, true,
                DialogAction.CLOSE,
                list,
                List.of()
        ), new ActionButton(new CommonButtonData(CommonComponents.GUI_DONE, 150), Optional.empty()))));

        return 1;
    }

    private static int creativeTab(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getPlayer().isCreative()) {
            try {
                var id = context.getArgument("itemGroup", Identifier.class);

                var itemGroup = BuiltInRegistries.CREATIVE_MODE_TAB.getValue(id);
                if (itemGroup != null) {
                    new CreativeTabUi(context.getSource().getPlayer(), itemGroup);
                    return 2;
                }
            } catch (Exception e) {
                //
            }

            new CreativeTabListUi(context.getSource().getPlayer());
            return 1;
        } else {
            return 0;
        }
    }

    private static int displayClientItem(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrException();
        var stack = PolymerItemUtils.getPolymerItemStack(player.getMainHandItem(), PacketContext.create(player)).copy();
        stack.remove(DataComponents.CUSTOM_DATA);

        context.getSource().sendSuccess(() -> (new TextComponentTagVisitor("")).visit(
                ItemStack.OPTIONAL_CODEC.encodeStart(context.getSource().registryAccess().createSerializationContext(NbtOps.INSTANCE), stack).getOrThrow()
        ), false);

        return 1;
    }

    private static int getClientItem(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrException();

        var stack = PolymerItemUtils.getPolymerItemStack(player.getMainHandItem(), PacketContext.create(player));
        stack.remove(DataComponents.CUSTOM_DATA);
        player.addItem(stack);
        context.getSource().sendSuccess(() -> Component.literal("Given client representation to player"), true);

        return 1;
    }
}
