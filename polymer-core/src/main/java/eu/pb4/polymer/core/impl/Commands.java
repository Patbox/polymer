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
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.visitor.NbtTextFormatter;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.LecternScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.stat.StatType;
import net.minecraft.state.property.Property;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import javax.naming.spi.StateFactory;
import java.util.ArrayList;
import java.util.Locale;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

@SuppressWarnings("ResultOfMethodCallIgnored")
@ApiStatus.Internal
public class Commands {
    public static void register(LiteralArgumentBuilder<ServerCommandSource> command, CommandRegistryAccess access) {
        command.then(literal("stats")
                        .requires(CommonImplUtils.permission("command.stats", 0))
                        .executes(Commands::statsGeneral)
                        .then(argument("type", RegistryEntryArgumentType.registryEntry(access, RegistryKeys.STAT_TYPE)).executes(Commands::stats))
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
                .then(literal("pick")
                        .requires(CommonImplUtils.permission("command.pick", 0))
                        .executes((ctx) -> Commands.pickTarget(ctx, false))
                        .then(
                                literal("withnbt").executes((ctx) -> Commands.pickTarget(ctx, true))
                        )

                )
                .then(literal("creative")
                        .requires(CommonImplUtils.permission("command.creative", 0))
                        .then(argument("itemGroup", IdentifierArgumentType.identifier())
                                .suggests((context, builder) -> {
                                    var remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

                                    var groups = PolymerItemGroupUtils.getItemGroups(context.getSource().getPlayerOrThrow());

                                    CommandSource.forEachMatching(groups, remaining, Registries.ITEM_GROUP::getId, group -> builder.suggest(Registries.ITEM_GROUP.getId(group).toString(), group.getDisplayName()));
                                    return builder.buildFuture();
                                })
                                .executes(Commands::creativeTab)
                        )
                        .executes(Commands::creativeTab));
    }

    private static int pickTarget(CommandContext<ServerCommandSource> serverCommandSourceCommandContext, boolean withNbt) throws CommandSyntaxException {
        var player = serverCommandSourceCommandContext.getSource().getPlayerOrThrow();
        var range = player.getEntityInteractionRange();

        var min = player.getCameraPosVec(0);
        var rot = player.getRotationVec(0);
        var max = min.add(rot.x * range, rot.y * range, rot.z * range);

        var box = player.getBoundingBox().stretch(rot.multiply(range)).expand(1.0, 1.0, 1.0);
        var entityHit = ProjectileUtil.raycast(player, min, max, box, entity -> !player.isSpectator() && entity.canHit(), range);

        if (entityHit != null) {
            PolymerImplUtils.pickEntity(player, entityHit.getEntity());
            return 1;
        }

        var hit = player.raycast(player.getBlockInteractionRange(), 0, false);
        if (hit instanceof BlockHitResult result && hit.getType() != HitResult.Type.MISS) {
            PolymerImplUtils.pickBlock(player, result.getBlockPos(), withNbt);
            return 2;
        }

        return 0;
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

        context.getSource().sendFeedback(() -> Text.literal(builder.toString()), false);

        return 0;
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

        var list = new ArrayList<RawFilteredPair<Text>>();

        int line = 0;
        MutableText text = null;

        for (var statType : Registries.STAT_TYPE) {
            if (text == null) {
                text = Text.literal("");
            }
            text.append(Text.empty().append(Registries.STAT_TYPE.getId(statType).toString()).append("\n").styled(x -> x.withUnderline(true).withColor(Formatting.BLUE).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/polymer stats " + Registries.STAT_TYPE.getId(statType)))));
            line++;

            if (line == 13) {
                list.add(RawFilteredPair.of(text));
                text = null;
                line = 0;
            }
        }

        if (text != null) {
            list.add(RawFilteredPair.of(text));
        }

        var stack = new ItemStack(Items.WRITTEN_BOOK);
        stack.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, new WrittenBookContentComponent(
                RawFilteredPair.of("/polymer start"),
                player.getGameProfile().getName(),
                0,
                list,
                false
        ));


        player.openHandledScreen(new NamedScreenHandlerFactory() {
            @Override
            public Text getDisplayName() {
                return Text.empty();
            }

            @Nullable
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                var lectern = new LecternScreenHandler(syncId) {
                    @Override
                    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
                        return false;
                    }

                    @Override
                    public boolean canInsertIntoSlot(Slot slot) {
                        return false;
                    }

                    @Override
                    public boolean onButtonClick(PlayerEntity player, int id) {
                        if (id == 3) {
                            return false;
                        } else {
                            return super.onButtonClick(player, id);
                        }
                    }

                    @Override
                    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
                        // noop
                    }
                };
                lectern.getSlot(0).setStack(stack);
                return lectern;
            }
        });

        return 1;
    }

    private static int stats(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayer();

        var list = new ArrayList<RawFilteredPair<Text>>();

        int line = 0;
        MutableText text = null;

        var type = (StatType<Object>) RegistryEntryArgumentType.getRegistryEntry(context, "type", RegistryKeys.STAT_TYPE).value();

        for (var statObj : type.getRegistry()) {
            if (PolymerUtils.isServerOnly(statObj) && type.hasStat(statObj)) {


                var stat = type.getOrCreateStat(statObj);

                if (text == null) {
                    text = Text.literal("");
                }

                var statVal = player.getStatHandler().getStat(stat);

                Text title;

                if (statObj instanceof PolymerStat stat1) {
                    title = PolymerStat.getName(stat1);
                } else if (statObj instanceof Item item) {
                    title = item.getName();
                } else if (statObj instanceof Block item) {
                    title = item.getName();
                } else if (statObj instanceof EntityType item) {
                    title = item.getName();
                } else {
                    title = Text.translatable(Util.createTranslationKey(type.getRegistry().getKey().getValue().getPath(), type.getRegistry().getId(statObj)));
                }

                text.append(title).append(Text.literal(": ").formatted(Formatting.GRAY)).append(Text.literal(stat.format(statVal) + "\n").formatted(Formatting.DARK_GRAY));
                line++;

                if (line == 13) {
                    list.add(RawFilteredPair.of(text));
                    text = null;
                    line = 0;
                }
            }
        }

        if (text != null) {
            list.add(RawFilteredPair.of(text));
        }

        var stack = new ItemStack(Items.WRITTEN_BOOK);
        stack.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, new WrittenBookContentComponent(
                RawFilteredPair.of("/polymer start"),
                player.getGameProfile().getName(),
                0,
                list,
                false
        ));


        player.openHandledScreen(new NamedScreenHandlerFactory() {
            @Override
            public Text getDisplayName() {
                return Text.empty();
            }

            @Nullable
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                var lectern = new LecternScreenHandler(syncId) {
                    @Override
                    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
                        return false;
                    }

                    @Override
                    public boolean canInsertIntoSlot(Slot slot) {
                        return false;
                    }

                    @Override
                    public boolean onButtonClick(PlayerEntity player, int id) {
                        if (id == 3) {
                            return false;
                        } else {
                            return super.onButtonClick(player, id);
                        }
                    }

                    @Override
                    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
                        // noop
                    }
                };
                lectern.getSlot(0).setStack(stack);
                return lectern;
            }
        });

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
        var player = context.getSource().getPlayer();
        var stack = PolymerItemUtils.getPolymerItemStack(player.getMainHandStack(), player).copy();
        stack.remove(DataComponentTypes.CUSTOM_DATA);

        context.getSource().sendFeedback(() -> (new NbtTextFormatter("", 3)).apply(ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, ServerTranslationUtils.parseFor(player.networkHandler, stack)).result().get()), false);

        return 1;
    }

    private static int getClientItem(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayer();

        var stack = PolymerItemUtils.getPolymerItemStack(player.getMainHandStack(), player);
        stack.remove(DataComponentTypes.CUSTOM_DATA);
        player.giveItemStack(ServerTranslationUtils.parseFor(player.networkHandler, stack));
        context.getSource().sendFeedback(() -> Text.literal("Given client representation to player"), true);

        return 1;
    }
}
