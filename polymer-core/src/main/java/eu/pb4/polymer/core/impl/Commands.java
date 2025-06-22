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
import eu.pb4.polymer.core.impl.compat.ServerTranslationUtils;
import eu.pb4.polymer.core.impl.networking.PolymerServerProtocol;
import eu.pb4.polymer.core.impl.ui.CreativeTabListUi;
import eu.pb4.polymer.core.impl.ui.CreativeTabUi;
import eu.pb4.polymer.core.impl.ui.PotionUi;
import eu.pb4.polymer.core.mixin.block.PalettedContainerAccessor;
import eu.pb4.polymer.networking.impl.ExtClientConnection;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.dialog.AfterAction;
import net.minecraft.dialog.DialogActionButtonData;
import net.minecraft.dialog.DialogButtonData;
import net.minecraft.dialog.DialogCommonData;
import net.minecraft.dialog.action.SimpleDialogAction;
import net.minecraft.dialog.body.DialogBody;
import net.minecraft.dialog.body.ItemDialogBody;
import net.minecraft.dialog.body.PlainMessageDialogBody;
import net.minecraft.dialog.type.MultiActionDialog;
import net.minecraft.dialog.type.NoticeDialog;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.visitor.NbtTextFormatter;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.stat.StatType;
import net.minecraft.state.property.Property;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import javax.naming.spi.StateFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

@SuppressWarnings("ResultOfMethodCallIgnored")
@ApiStatus.Internal
public class Commands {
    public static void register(LiteralArgumentBuilder<ServerCommandSource> command, CommandRegistryAccess access) {
        command.then(literal("stats")
                        .requires(CommonImplUtils.permission("command.stats", 0))
                        .executes(Commands::statsGeneral)
                        .then(argument("type", RegistryEntryReferenceArgumentType.registryEntry(access, RegistryKeys.STAT_TYPE)).executes(Commands::stats))
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
                        .then(argument("itemGroup", IdentifierArgumentType.identifier())
                                .suggests((context, builder) -> {
                                    var remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

                                    var groups = PolymerItemGroupUtils.getItemGroups(context.getSource().getPlayerOrThrow());

                                    CommandSource.forEachMatching(groups, remaining, PolymerItemGroupUtils::getId, group -> builder.suggest(PolymerItemGroupUtils.getId(group).toString(), group.getDisplayName()));
                                    return builder.buildFuture();
                                })
                                .executes(Commands::creativeTab)
                        )
                        .executes(Commands::creativeTab));
    }

    public static void registerDev(LiteralArgumentBuilder<ServerCommandSource> dev) {
        dev
                .then(literal("reload-world")
                        .executes((ctx) -> {
                            PolymerUtils.reloadWorld(ctx.getSource().getPlayer());
                            return 0;
                        })
                )
                .then(literal("get-mapper")
                        .executes((ctx) -> {
                            ctx.getSource().sendFeedback(() -> Text.literal(BlockMapper.getFrom(ctx.getSource().getPlayer()).getMapperName()), false);
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
                            PolymerSyncUtils.synchronizePolymerRegistries(ctx.getSource().getPlayer().networkHandler);
                            return 0;
                        }))
                .then(literal("protocol-info")
                        .executes((ctx) -> {
                            ctx.getSource().sendFeedback(() -> Text.literal("Protocol supported by your client:"), false);
                            for (var entry : ExtClientConnection.of(ctx.getSource().getPlayer().networkHandler).polymerNet$getSupportMap().object2IntEntrySet()) {
                                ctx.getSource().sendFeedback(() -> Text.literal("- " + entry.getKey() + " = " + entry.getIntValue()), false);
                            }
                            return 0;
                        })
                )
                .then(literal("validate_states")
                        .executes((ctx) -> {
                            PolymerServerProtocol.sendDebugValidateStatesPackets(ctx.getSource().getPlayer().networkHandler);
                            return 0;
                        })
                )
                .then(literal("set-pack-status")
                        .then(argument("status", BoolArgumentType.bool())
                                .then(argument("uuid", UuidArgumentType.uuid())
                                        .executes((ctx) -> {
                                            var status = ctx.getArgument("status", Boolean.class);
                                            PolymerCommonUtils.setHasResourcePack(ctx.getSource().getPlayerOrThrow(), UuidArgumentType.getUuid(ctx, "uuid"), status);
                                            ctx.getSource().sendFeedback(() -> Text.literal("New resource pack status: " + status), false);
                                            return 0;
                                        }))
                        )
                )
                .then(literal("get-pack-status")
                        .executes((ctx) -> {
                            var status = PolymerUtils.hasResourcePack(ctx.getSource().getPlayer(), PolymerResourcePackUtils.getMainUuid());
                            ctx.getSource().sendFeedback(() -> Text.literal("Resource pack status: " + status), false);
                            return 0;
                        })
                )
                .then(literal("chunk_section_info")
                        .executes((ctx) -> {
                            var chunk = ctx.getSource().getWorld().getChunk(ctx.getSource().getPlayer().getBlockPos());
                            var s = chunk.getSection(ctx.getSource().getWorld().getSectionIndex(ctx.getSource().getPlayer().getBlockY()));

                            var a = ((PalettedContainerAccessor<BlockState>) s.getBlockStateContainer()).getData();

                            ctx.getSource().sendFeedback(() -> Text.literal("Chunk: " + chunk.getPos() + " Palette: " + a.palette() + " | " + " Storage: " + a.storage() + " | Bits: " + a.storage().getElementBits()), false);
                            return 0;
                        })
                );
    }

    private static int targetBlock(CommandContext<ServerCommandSource> context) {
        var raycast = (BlockHitResult) context.getSource().getPlayer().raycast(10, 0, true);

        var builder = new StringBuilder();
        var state = context.getSource().getWorld().getBlockState(raycast.getBlockPos());

        builder.append(Registries.BLOCK.getId(state.getBlock()));

        if (!state.getBlock().getStateManager().getProperties().isEmpty()) {
            builder.append("[");
            var iterator = state.getBlock().getStateManager().getProperties().iterator();

            while (iterator.hasNext()) {
                var property = iterator.next();
                builder.append(property.getName());
                builder.append("=");
                builder.append(((Property) property).name(state.get(property)));

                if (iterator.hasNext()) {
                    builder.append(",");
                }
            }
            builder.append("]");
        }

        context.getSource().sendFeedback(() -> Text.literal(builder.toString())
                .setStyle(Style.EMPTY
                        .withHoverEvent(new HoverEvent.ShowText(Text.translatable("chat.copy.click")))
                        .withClickEvent(new ClickEvent.CopyToClipboard(builder.toString()))), false);

        return 0;
    }

    private static int targetItem(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var itemStack = context.getSource().getPlayerOrThrow().getMainHandStack();
        var id = Registries.ITEM.getId(itemStack.getItem());
        context.getSource().sendFeedback(() -> Text.literal(id.toString())
                .setStyle(Style.EMPTY
                        .withHoverEvent(new HoverEvent.ShowText(Text.translatable("chat.copy.click")))
                        .withClickEvent(new ClickEvent.CopyToClipboard(id.toString()))), false);        return 0;
    }

    private static int dumpRegistries(CommandContext<ServerCommandSource> context) {
        var path = PolymerImplUtils.dumpRegistry();
        if (path != null) {
            context.getSource().sendFeedback(() -> Text.literal("Exported registry state as " + path), false);
        } else {
            context.getSource().sendError(Text.literal("Couldn't export registry!"));
        }
        return 0;
    }

    private static int effects(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        new PotionUi(context.getSource().getPlayer());
        return 1;
    }

    private static int statsGeneral(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayer();

        var list = new ArrayList<DialogActionButtonData>();

        for (var statType : Registries.STAT_TYPE) {
            list.add(new DialogActionButtonData(new DialogButtonData(Text.literal(Registries.STAT_TYPE.getId(statType).toString()), 150),
                    Optional.of(new SimpleDialogAction(new ClickEvent.RunCommand("polymer stats " + Registries.STAT_TYPE.getId(statType))))));
        }

        player.openDialog(RegistryEntry.of(new MultiActionDialog(new DialogCommonData(
                Text.translatable("gui.stats"),
                Optional.empty(),
                true, true,
                AfterAction.CLOSE,
                List.of(),
                List.of()
        ), list, Optional.of(new DialogActionButtonData(new DialogButtonData(ScreenTexts.DONE, 150), Optional.empty())), 1)));

        return 1;
    }

    private static int stats(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayer();

        var list = new ArrayList<DialogBody>();

        var type = (StatType<Object>) RegistryEntryReferenceArgumentType.getRegistryEntry(context, "type", RegistryKeys.STAT_TYPE).value();
        for (var statObj : type.getRegistry()) {
            if (PolymerUtils.isServerOnly(type.getRegistry(), statObj) && type.hasStat(statObj)) {
                var stat = type.getOrCreateStat(statObj);

                var statVal = player.getStatHandler().getStat(stat);

                ItemStack stack = ItemStack.EMPTY;

                Text title;

                if (statObj instanceof Identifier stat1) {
                    title = PolymerStat.getName(stat1);
                } else if (statObj instanceof Item item) {
                    title = item.getName();
                    stack = item.getDefaultStack();
                } else if (statObj instanceof Block item) {
                    title = item.getName();
                    stack = item.asItem().getDefaultStack();
                } else if (statObj instanceof EntityType item) {
                    title = item.getName();
                } else {
                    title = Text.translatable(Util.createTranslationKey(type.getRegistry().getKey().getValue().getPath(), type.getRegistry().getId(statObj)));
                }

                var text = Text.empty().append(title).append(Text.literal(": ").formatted(Formatting.GRAY)).append(Text.literal(stat.format(statVal)).formatted(Formatting.WHITE));

                if (stack.isEmpty()) {
                    list.add(new PlainMessageDialogBody(text, 200));
                } else {
                    list.add(new ItemDialogBody(stack, Optional.of(new PlainMessageDialogBody(text, 200)), true, true, 16, 16));
                }
            }
        }

        player.openDialog(RegistryEntry.of(new NoticeDialog(new DialogCommonData(
                Text.translatable("gui.stats"),
                Optional.empty(),
                true, true,
                AfterAction.CLOSE,
                list,
                List.of()
        ), new DialogActionButtonData(new DialogButtonData(ScreenTexts.DONE, 150), Optional.empty()))));

        return 1;
    }

    private static int creativeTab(CommandContext<ServerCommandSource> context) {
        if (context.getSource().getPlayer().isCreative()) {
            try {
                var id = context.getArgument("itemGroup", Identifier.class);

                var itemGroup = Registries.ITEM_GROUP.get(id);
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

    private static int displayClientItem(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrThrow();
        var stack = PolymerItemUtils.getPolymerItemStack(player.getMainHandStack(), PacketContext.create(player)).copy();
        stack.remove(DataComponentTypes.CUSTOM_DATA);

        context.getSource().sendFeedback(() -> (new NbtTextFormatter("")).apply(
                ItemStack.OPTIONAL_CODEC.encodeStart(context.getSource().getRegistryManager().getOps(NbtOps.INSTANCE), stack).getOrThrow()
        ), false);

        return 1;
    }

    private static int getClientItem(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrThrow();

        var stack = PolymerItemUtils.getPolymerItemStack(player.getMainHandStack(), PacketContext.create(player));
        stack.remove(DataComponentTypes.CUSTOM_DATA);
        player.giveItemStack(stack);
        context.getSource().sendFeedback(() -> Text.literal("Given client representation to player"), true);

        return 1;
    }
}
