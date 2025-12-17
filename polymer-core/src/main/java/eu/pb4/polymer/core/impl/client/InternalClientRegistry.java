package eu.pb4.polymer.core.impl.client;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.common.api.events.SimpleEvent;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.client.*;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import eu.pb4.polymer.core.api.utils.PolymerRegistry;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.client.debug.LookingAtPolymerBlockDebugHudEntry;
import eu.pb4.polymer.core.impl.client.debug.LookingAtPolymerEntityDebugHudEntry;
import eu.pb4.polymer.core.impl.client.debug.PolymerInfoDebugHudEntry;
import eu.pb4.polymer.core.impl.client.interfaces.ClientBlockStorageInterface;
import eu.pb4.polymer.core.impl.client.interfaces.ClientCreativeModeTabExtension;
import eu.pb4.polymer.core.impl.interfaces.IndexedNetwork;
import eu.pb4.polymer.core.impl.interfaces.PolymerIdMapper;
import eu.pb4.polymer.core.impl.other.DelayedAction;
import eu.pb4.polymer.core.impl.other.EventRunners;
import eu.pb4.polymer.core.impl.other.FixedIdList;
import eu.pb4.polymer.core.impl.other.ImplPolymerRegistry;
import eu.pb4.polymer.core.mixin.client.CreativeModeInventoryScreenAccessor;
import eu.pb4.polymer.core.mixin.other.CreativeModeTabsAccessor;
import eu.pb4.polymer.networking.api.client.PolymerClientNetworking;

import it.unimi.dsi.fastutil.objects.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.IdMap;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.Strategy;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class InternalClientRegistry {
    public static final SimpleEvent<Runnable> TICK = new SimpleEvent<>();
    public static final Object2IntMap<String> CLIENT_PROTOCOL = new Object2IntOpenHashMap<>();
    public static final ImplPolymerRegistry<ClientPolymerBlock> BLOCKS = new ImplPolymerRegistry<>("block", "B", ClientPolymerBlock.NONE.identifier(), ClientPolymerBlock.NONE);
    public static final FixedIdList<ClientPolymerBlock.State> BLOCK_STATES = new FixedIdList<>();
    public static Strategy<ClientPolymerBlock.State> blockStatesPaletteProvider = Strategy.createForBlockStates(BLOCK_STATES);
    public static final ImplPolymerRegistry<ClientPolymerItem> ITEMS = new ImplPolymerRegistry<>("item", "I");
    public static final ImplPolymerRegistry<ClientPolymerEntityType> ENTITY_TYPES = new ImplPolymerRegistry<>("entity_type", "E");
    public static final ImplPolymerRegistry<ClientPolymerEntry<VillagerProfession>> VILLAGER_PROFESSIONS = new ImplPolymerRegistry<>("villager_profession", "VP");
    public static final ImplPolymerRegistry<ClientPolymerEntry<BlockEntityType<?>>> BLOCK_ENTITY = new ImplPolymerRegistry<>("block_entity", "BE");
    public static final ImplPolymerRegistry<ClientPolymerEntry<MobEffect>> STATUS_EFFECT = new ImplPolymerRegistry<>("status_effect", "SE");
    public static final ImplPolymerRegistry<ClientPolymerEntry<Fluid>> FLUID = new ImplPolymerRegistry<>("fluid", "FL");
    public static final ImplPolymerRegistry<ClientPolymerEntry<MenuType<?>>> SCREEN_HANDLER = new ImplPolymerRegistry<>("screen_handler", "SH");
    public static final ImplPolymerRegistry<ClientPolymerEntry<DataComponentType<?>>> DATA_COMPONENT_TYPE = new ImplPolymerRegistry<>("data_component_type", "DC");
    public static final ImplPolymerRegistry<ClientPolymerEntry<DataComponentType<?>>> ENCHANTMENT_COMPONENT_TYPE = new ImplPolymerRegistry<>("enchantment_component_type", "EC");
    public static final ImplPolymerRegistry<InternalClientItemGroup> ITEM_GROUPS = new ImplPolymerRegistry<>("item_groups", "IG");
    public static final List<ImplPolymerRegistry<?>> REGISTRIES = List.of(ITEMS, BLOCKS, BLOCK_ENTITY, ENTITY_TYPES, STATUS_EFFECT, VILLAGER_PROFESSIONS, FLUID, SCREEN_HANDLER, ITEM_GROUPS, DATA_COMPONENT_TYPE, ENCHANTMENT_COMPONENT_TYPE);
    public static final Map<Registry<?>, ImplPolymerRegistry<ClientPolymerEntry<?>>> BY_VANILLA = createRegMap();
    public static final Map<Identifier, ImplPolymerRegistry<ClientPolymerEntry<?>>> BY_VANILLA_ID = createRegMapId(BY_VANILLA);
    private static final Object2ObjectMap<String, DelayedAction> DELAYED_ACTIONS = new Object2ObjectArrayMap<>();
    private static final Map<ClientPolymerItem, VirtualClientItem> VIRTUAL_ITEM_CACHE = new Object2ObjectOpenHashMap<>();
    public static boolean enabled = false;
    public static int syncRequests = 0;
    public static int syncRequestsPostGameJoin = 0;
    public static String serverVersion = "";
    public static String debugRegistryInfo = "";
    public static String debugServerInfo = "";
    public static boolean serverHasPolymer;
    public static boolean limitedF3;

    private static Map<Registry<?>, ImplPolymerRegistry<ClientPolymerEntry<?>>> createRegMap() {
        var map = new HashMap<Registry<?>, ImplPolymerRegistry<?>>();
        map.put(BuiltInRegistries.BLOCK, BLOCKS);
        map.put(BuiltInRegistries.ENTITY_TYPE, ENTITY_TYPES);
        map.put(BuiltInRegistries.ITEM, ITEMS);
        map.put(BuiltInRegistries.MOB_EFFECT, STATUS_EFFECT);
        map.put(BuiltInRegistries.VILLAGER_PROFESSION, VILLAGER_PROFESSIONS);
        map.put(BuiltInRegistries.BLOCK_ENTITY_TYPE, BLOCK_ENTITY);
        map.put(BuiltInRegistries.FLUID, FLUID);
        map.put(BuiltInRegistries.DATA_COMPONENT_TYPE, DATA_COMPONENT_TYPE);
        map.put(BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, ENCHANTMENT_COMPONENT_TYPE);
        return (Map<Registry<?>, ImplPolymerRegistry<ClientPolymerEntry<?>>>) (Object) map;
    }

    private static Map<Identifier, ImplPolymerRegistry<ClientPolymerEntry<?>>> createRegMapId(Map<Registry<?>, ImplPolymerRegistry<ClientPolymerEntry<?>>> byVanilla) {
        return byVanilla.entrySet().stream().map(x -> Map.entry(x.getKey().key().identifier(), x.getValue())).collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
    }

    public static ClientPolymerBlock.State getBlockAt(BlockPos pos) {
        try {
            if (Minecraft.getInstance().level != null) {
                var chunk = Minecraft.getInstance().level.getChunkSource().getChunk(
                        SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()),
                        ChunkStatus.FULL,
                        true
                );

                return ((ClientBlockStorageInterface) chunk).polymer$getClientBlock(pos.getX(), pos.getY(), pos.getZ());
            }
        } catch (Throwable e) {}


        return ClientPolymerBlock.NONE_STATE;
    }

    public static void setBlockAt(BlockPos pos, ClientPolymerBlock.State state) {
        if (Minecraft.getInstance().level != null) {
            var chunk = Minecraft.getInstance().level.getChunkSource().getChunk(
                    SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()),
                    ChunkStatus.FULL,
                    true
            );

            if (chunk != null) {
                ((ClientBlockStorageInterface) chunk).polymer$setClientBlock(pos.getX(), pos.getY(), pos.getZ(), state);
            }
        }
    }

    public static void setVersion(String version, @Nullable IntTag protocolVersion) {
        serverVersion = version;
        serverHasPolymer = !version.isEmpty();
        enabled = serverHasPolymer && protocolVersion != null && protocolVersion.intValue() == SharedConstants.getProtocolVersion();
    }

    public static void disable() {
        setVersion("", null);
        clear();
        DELAYED_ACTIONS.clear();
        CLIENT_PROTOCOL.clear();
        syncRequests = 0;
        syncRequestsPostGameJoin = 0;
        PolymerClientUtils.ON_DISABLE.invoke(Runnable::run);
    }

    @Nullable
    public static BlockState getRealBlockState(int rawPolymerId) {
        var state = InternalClientRegistry.BLOCK_STATES.byId(rawPolymerId);
        if (state != null && state.blockState() != null) {
            if (PolymerClientDecoded.checkDecode(state.blockState().getBlock())) {
                return state.blockState();
            } else {
                return PolymerBlockUtils.getPolymerBlockState(state.blockState(), PacketContext.create());
            }
        }

        return null;
    }

    private static void setDecoders() {
        IndexedNetwork.set(Block.BLOCK_STATE_REGISTRY, InternalClientRegistry::getRealBlockState);
        IndexedNetwork.set(BuiltInRegistries.ITEM, InternalClientRegistry::decodeItem);

        setSimpleDecoder((Registry<EntityType>) (Object) BuiltInRegistries.ENTITY_TYPE, (PolymerRegistry<ClientPolymerEntry<EntityType>>) (Object) ENTITY_TYPES);
        setSimpleDecoder(BuiltInRegistries.BLOCK, (PolymerRegistry<ClientPolymerEntry<Block>>) (Object) BLOCKS);

        setSimpleDecoder(BuiltInRegistries.VILLAGER_PROFESSION, VILLAGER_PROFESSIONS);
        setSimpleDecoder(BuiltInRegistries.MOB_EFFECT, STATUS_EFFECT);
        setSimpleDecoder(BuiltInRegistries.BLOCK_ENTITY_TYPE, BLOCK_ENTITY);
        setSimpleDecoder(BuiltInRegistries.FLUID, FLUID);
        setSimpleDecoder(BuiltInRegistries.MENU, SCREEN_HANDLER);
        setSimpleDecoder(BuiltInRegistries.DATA_COMPONENT_TYPE, DATA_COMPONENT_TYPE);
        setSimpleDecoder(BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, ENCHANTMENT_COMPONENT_TYPE);
    }


    public static Object decodeRegistry(IdMap<?> instance, int i) {
        if (serverHasPolymer) {
            return PolymerCommonUtils.executeWithNetworkingLogic(() -> instance.byIdOrThrow(i));
        }

        return instance.byIdOrThrow(i);
    }

    private static Item decodeItem(int id) {
        if (InternalClientRegistry.enabled) {
            var item = InternalClientRegistry.ITEMS.byId(id);
            if (item != null) {
                if (item.registryEntry() != null) {
                    return item.registryEntry();
                } else if (PolymerImpl.USE_UNSAFE_ITEMS_CLIENT) {
                    return VIRTUAL_ITEM_CACHE.computeIfAbsent(item, VirtualClientItem::of);
                }
            }
        }

        return null;
    }

    private static <T> void setSimpleDecoder(final IdMap<T> registry, final PolymerRegistry<ClientPolymerEntry<T>> polymerRegistry) {
        IndexedNetwork.set(registry, (id) -> {
            if (InternalClientRegistry.enabled) {
                var item = polymerRegistry.byId(id);

                if (item != null && item.registryEntry() != null) {
                    return item.registryEntry();
                }
            }

            return null;
        });
    }

    public static void tick() {
        if (!enabled) {
            debugServerInfo = "[Polymer] C: " + CommonImpl.VERSION + ", S: " + InternalClientRegistry.serverVersion;
            debugRegistryInfo = "[Polymer] §cMismatched protocol versions!";
            return;
        }

        DELAYED_ACTIONS.object2ObjectEntrySet().removeIf(stringDelayedActionEntry -> stringDelayedActionEntry.getValue().tryDoing());
        TICK.invoke(Runnable::run);

        debugServerInfo = "[Polymer] C: " + CommonImpl.VERSION + ", S: " + InternalClientRegistry.serverVersion;
        if (limitedF3) {
            debugRegistryInfo = "";
            return;
        }
        var regInfo = new StringBuilder();
        regInfo.append("[Polymer] ");
        for (var reg : REGISTRIES) {
            regInfo.append(reg.getShortName());
            regInfo.append(": ");
            regInfo.append(reg.size());
            regInfo.append(", ");
        }

        regInfo.append("BS: ").append(InternalClientRegistry.BLOCK_STATES.mapSize());

        debugRegistryInfo = regInfo.toString();
    }

    public static void clear() {
        for (var reg : REGISTRIES) {
            reg.clear();
        }

        VIRTUAL_ITEM_CACHE.clear();

        BLOCKS.set(ClientPolymerBlock.NONE.identifier(), ClientPolymerBlock.NONE);
        ((PolymerIdMapper) BLOCK_STATES).polymer$clear();
        BLOCK_STATES.addMapping(ClientPolymerBlock.NONE_STATE, 0);
        updateBlockStatesPaletteProvider();

        Minecraft.getInstance().execute(() -> {
            clearTabs(i -> true);

            for (var group : BuiltInRegistries.CREATIVE_MODE_TAB) {
                if (group.getType() == CreativeModeTab.Type.CATEGORY) {
                    try {
                        ((ClientCreativeModeTabExtension) group).polymer$clearStacks();
                    } catch (Throwable e) {
                        PolymerImpl.LOGGER.warn("Can't clear stacks of ItemGroup!", e);
                    }
                }
            }
            try {
                if (CreativeModeTabsAccessor.getCACHED_PARAMETERS() != null) {
                    CreativeModeTabsAccessor.callBuildAllTabContents(CreativeModeTabsAccessor.getCACHED_PARAMETERS());
                }
            } catch (Throwable e) {
                PolymerImpl.LOGGER.warn("Can't update entries of ItemGroups!", e);
            }
        });
        PolymerClientUtils.ON_CLEAR.invoke(EventRunners.RUN);
    }

    private static final int TABS_PER_PAGE = 10;

    public static void clearTabs(Predicate<InternalClientItemGroup> removePredicate) {
        try {
            ITEM_GROUPS.removeIf(removePredicate);
            CreativeModeInventoryScreenAccessor.setSelectedTab(CreativeModeTabs.getDefaultTab());

            if (CompatStatus.FABRIC_ITEM_GROUP || CompatStatus.QUILT_ITEM_GROUP) {
                try {
                    for (var f1 : CreativeModeInventoryScreen.class.getDeclaredFields()) {
                        if (f1.getName().contains("currentPage")) {
                            f1.setAccessible(true);
                            f1.setInt(null, 0);
                            break;
                        }
                    }
                } catch (Throwable e) {
                    if (PolymerImpl.LOG_MORE_ERRORS) {
                        PolymerImpl.LOGGER.error("Failed to change item group page (FABRIC / QUILT)!", e);
                    }
                }
            }

            int count = BuiltInRegistries.CREATIVE_MODE_TAB.size() - 4;
            for (var x : ITEM_GROUPS) {
                var page = (count / TABS_PER_PAGE);
                int pageIndex = count % TABS_PER_PAGE;
                CreativeModeTab.Row row = pageIndex < (TABS_PER_PAGE / 2) ? CreativeModeTab.Row.TOP : CreativeModeTab.Row.BOTTOM;
                var c = row == CreativeModeTab.Row.TOP ? pageIndex % TABS_PER_PAGE : (pageIndex - TABS_PER_PAGE / 2) % (TABS_PER_PAGE);
                ((ClientCreativeModeTabExtension) x).polymerCore$setPos(row, c);
                setItemGroupPage(x, page);
                count++;
            }
        } catch (Throwable e) {

        }
    }

    private static void setItemGroupPage(CreativeModeTab group, int page) {
        ((ClientCreativeModeTabExtension) group).polymerCore$setPage(page);
        if (CompatStatus.FABRIC_ITEM_GROUP) {
            try {
                ((net.fabricmc.fabric.impl.itemgroup.FabricItemGroupImpl) group).fabric_setPage(page);
            } catch (Throwable e) {
                PolymerImpl.LOGGER.warn("Couldn't set page of ItemGroup (FABRIC)", e);
            }
        }
    }

    public static void createItemGroup(Identifier id, Component name, ItemStack icon) {
        try {
            var existing = BuiltInRegistries.CREATIVE_MODE_TAB.getValue(id);
            if (existing != null) {
                return;
            }
            int count = (BuiltInRegistries.CREATIVE_MODE_TAB.size() - 4) + ITEM_GROUPS.size();

            var page = (count / TABS_PER_PAGE);
            int pageIndex = count % TABS_PER_PAGE;
            CreativeModeTab.Row row = pageIndex < (TABS_PER_PAGE / 2) ? CreativeModeTab.Row.TOP : CreativeModeTab.Row.BOTTOM;
            var c = row == CreativeModeTab.Row.TOP ? pageIndex % TABS_PER_PAGE : (pageIndex - TABS_PER_PAGE / 2) % (TABS_PER_PAGE);

            var group = new InternalClientItemGroup(row, c, id, name, icon);
            ITEM_GROUPS.set(id, group);

            setItemGroupPage(group, page);
        } catch(Throwable e) {

        }
    }

    public static CreativeModeTab getItemGroup(Identifier id) {
        var x = ITEM_GROUPS.get(id);
        if (x != null) {
            return x;
        }
        return BuiltInRegistries.CREATIVE_MODE_TAB.getValue(id);
    }

    public static int getClientProtocolVer(Identifier identifier) {
        return PolymerClientNetworking.getSupportedVersion(identifier);
    }

    public static void delayAction(String id, int time, Runnable action) {
        if (enabled) {
            DELAYED_ACTIONS.put(id, new DelayedAction(id, time, action));
        }
    }

    static {
        setDecoders();
    }

    public static void register() {
        DebugScreenEntries.register(Identifier.fromNamespaceAndPath("polymer", "looking_at_server_block"), new LookingAtPolymerBlockDebugHudEntry());
        DebugScreenEntries.register(Identifier.fromNamespaceAndPath("polymer", "looking_at_server_entity"), new LookingAtPolymerEntityDebugHudEntry());
        DebugScreenEntries.register(Identifier.fromNamespaceAndPath("polymer", "server_info"), new PolymerInfoDebugHudEntry());
    }

    public static void updateBlockStatesPaletteProvider() {
        blockStatesPaletteProvider = Strategy.createForBlockStates(InternalClientRegistry.BLOCK_STATES);
    }
}
