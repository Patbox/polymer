package eu.pb4.polymer.core.impl.client;

import eu.pb4.polymer.common.api.events.SimpleEvent;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.common.impl.client.ClientUtils;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.client.*;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import eu.pb4.polymer.core.api.utils.PolymerRegistry;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.client.interfaces.ClientBlockStorageInterface;
import eu.pb4.polymer.core.impl.client.interfaces.ClientItemGroupExtension;
import eu.pb4.polymer.core.impl.interfaces.IndexedNetwork;
import eu.pb4.polymer.core.impl.interfaces.PolymerIdList;
import eu.pb4.polymer.core.impl.other.DelayedAction;
import eu.pb4.polymer.core.impl.other.EventRunners;
import eu.pb4.polymer.core.impl.other.ImplPolymerRegistry;
import eu.pb4.polymer.core.mixin.client.CreativeInventoryScreenAccessor;
import eu.pb4.polymer.core.mixin.other.IdListAccessor;
import eu.pb4.polymer.core.mixin.other.ItemGroupsAccessor;
import eu.pb4.polymer.networking.api.client.PolymerClientNetworking;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

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
    public static final Int2ObjectMap<Identifier> ARMOR_TEXTURES_1 = new Int2ObjectOpenHashMap<>();
    public static final Int2ObjectMap<Identifier> ARMOR_TEXTURES_2 = new Int2ObjectOpenHashMap<>();
    public static final ImplPolymerRegistry<ClientPolymerBlock> BLOCKS = new ImplPolymerRegistry<>("block", "B", ClientPolymerBlock.NONE.identifier(), ClientPolymerBlock.NONE);
    public static final IdList<ClientPolymerBlock.State> BLOCK_STATES = new IdList<>() {
        @Override
        public int size() {
            return ((IdListAccessor) (Object) this).getList().size();
        }
    };
    public static final ImplPolymerRegistry<ClientPolymerItem> ITEMS = new ImplPolymerRegistry<>("item", "I");
    public static final ImplPolymerRegistry<ClientPolymerEntityType> ENTITY_TYPES = new ImplPolymerRegistry<>("entity_type", "E");
    public static final ImplPolymerRegistry<ClientPolymerEntry<VillagerProfession>> VILLAGER_PROFESSIONS = new ImplPolymerRegistry<>("villager_profession", "VP");
    public static final ImplPolymerRegistry<ClientPolymerEntry<BlockEntityType<?>>> BLOCK_ENTITY = new ImplPolymerRegistry<>("block_entity", "BE");
    public static final ImplPolymerRegistry<ClientPolymerEntry<StatusEffect>> STATUS_EFFECT = new ImplPolymerRegistry<>("status_effect", "SE");
    public static final ImplPolymerRegistry<ClientPolymerEntry<Enchantment>> ENCHANTMENT = new ImplPolymerRegistry<>("enchantment", "EN");
    public static final ImplPolymerRegistry<ClientPolymerEntry<Fluid>> FLUID = new ImplPolymerRegistry<>("fluid", "FL");
    public static final ImplPolymerRegistry<ClientPolymerEntry<ScreenHandlerType<?>>> SCREEN_HANDLER = new ImplPolymerRegistry<>("screen_handler", "SH");
    public static final ImplPolymerRegistry<InternalClientItemGroup> ITEM_GROUPS = new ImplPolymerRegistry<>("item_groups", "IG");
    public static final List<ImplPolymerRegistry<?>> REGISTRIES = List.of(ITEMS, BLOCKS, BLOCK_ENTITY, ENTITY_TYPES, STATUS_EFFECT, VILLAGER_PROFESSIONS, ENCHANTMENT, FLUID, SCREEN_HANDLER, ITEM_GROUPS);
    public static final Map<Registry<?>, ImplPolymerRegistry<ClientPolymerEntry<?>>> BY_VANILLA = createRegMap();
    public static final Map<Identifier, ImplPolymerRegistry<ClientPolymerEntry<?>>> BY_VANILLA_ID = createRegMapId(BY_VANILLA);
    private static final Object2ObjectMap<String, DelayedAction> DELAYED_ACTIONS = new Object2ObjectArrayMap<>();
    private static final Map<ClientPolymerItem, VirtualClientItem> VIRTUAL_ITEM_CACHE = new Object2ObjectOpenHashMap<>();
    public static boolean enabled = false;
    public static int syncRequests = 0;
    public static int syncRequestsPostGameJoin = 0;
    public static String serverVersion = "";
    public static boolean hasArmorTextures = false;
    public static String debugRegistryInfo = "";
    public static String debugServerInfo = "";

    private static Map<Registry<?>, ImplPolymerRegistry<ClientPolymerEntry<?>>> createRegMap() {
        var map = new HashMap<Registry<?>, ImplPolymerRegistry<?>>();
        map.put(Registries.BLOCK, BLOCKS);
        map.put(Registries.ENTITY_TYPE, ENTITY_TYPES);
        map.put(Registries.ITEM, ITEMS);
        map.put(Registries.STATUS_EFFECT, STATUS_EFFECT);
        map.put(Registries.VILLAGER_PROFESSION, VILLAGER_PROFESSIONS);
        map.put(Registries.BLOCK_ENTITY_TYPE, BLOCK_ENTITY);
        map.put(Registries.ENCHANTMENT, ENCHANTMENT);
        map.put(Registries.FLUID, FLUID);
        map.put(Registries.SCREEN_HANDLER, SCREEN_HANDLER);
        return (Map<Registry<?>, ImplPolymerRegistry<ClientPolymerEntry<?>>>) (Object) map;
    }

    private static Map<Identifier, ImplPolymerRegistry<ClientPolymerEntry<?>>> createRegMapId(Map<Registry<?>, ImplPolymerRegistry<ClientPolymerEntry<?>>> byVanilla) {
        return byVanilla.entrySet().stream().map(x -> Map.entry(x.getKey().getKey().getValue(), x.getValue())).collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
    }

    public static ClientPolymerBlock.State getBlockAt(BlockPos pos) {
        try {
            if (MinecraftClient.getInstance().world != null) {
                var chunk = MinecraftClient.getInstance().world.getChunkManager().getChunk(
                        ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()),
                        ChunkStatus.FULL,
                        true
                );

                return ((ClientBlockStorageInterface) chunk).polymer$getClientBlock(pos.getX(), pos.getY(), pos.getZ());
            }
        } catch (Throwable e) {}


        return ClientPolymerBlock.NONE_STATE;
    }

    public static void setBlockAt(BlockPos pos, ClientPolymerBlock.State state) {
        if (MinecraftClient.getInstance().world != null) {
            var chunk = MinecraftClient.getInstance().world.getChunkManager().getChunk(
                    ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()),
                    ChunkStatus.FULL,
                    true
            );

            if (chunk != null) {
                ((ClientBlockStorageInterface) chunk).polymer$setClientBlock(pos.getX(), pos.getY(), pos.getZ(), state);
            }
        }
    }

    public static void setVersion(String version) {
        serverVersion = version;
        enabled = !version.isEmpty();
    }

    public static void disable() {
        setVersion("");
        clear();
        DELAYED_ACTIONS.clear();
        CLIENT_PROTOCOL.clear();
        syncRequests = 0;
        syncRequestsPostGameJoin = 0;
        PolymerClientUtils.ON_DISABLE.invoke(Runnable::run);
    }

    @Nullable
    public static BlockState getRealBlockState(int rawPolymerId) {
        var state = InternalClientRegistry.BLOCK_STATES.get(rawPolymerId);
        if (state != null && state.blockState() != null) {
            if (PolymerClientDecoded.checkDecode(state.blockState().getBlock())) {
                return state.blockState();
            } else {
                return PolymerBlockUtils.getPolymerBlockState(state.blockState(), ClientUtils.getPlayer());
            }
        }

        return null;
    }

    private static void setDecoders() {
        IndexedNetwork.set(Block.STATE_IDS, InternalClientRegistry::decodeState);
        IndexedNetwork.set(Registries.ITEM, InternalClientRegistry::decodeItem);

        setSimpleDecoder((Registry<EntityType>) (Object) Registries.ENTITY_TYPE, (PolymerRegistry<ClientPolymerEntry<EntityType>>) (Object) ENTITY_TYPES);
        setSimpleDecoder(Registries.ENCHANTMENT, ENCHANTMENT);
        setSimpleDecoder(Registries.BLOCK, (PolymerRegistry<ClientPolymerEntry<Block>>) (Object) BLOCKS);

        setSimpleDecoder(Registries.VILLAGER_PROFESSION, VILLAGER_PROFESSIONS);
        setSimpleDecoder(Registries.STATUS_EFFECT, STATUS_EFFECT);
        setSimpleDecoder(Registries.BLOCK_ENTITY_TYPE, BLOCK_ENTITY);
        setSimpleDecoder(Registries.FLUID, FLUID);
        setSimpleDecoder(Registries.SCREEN_HANDLER, SCREEN_HANDLER);
    }


    public static Object decodeRegistry(IndexedIterable<?> instance, int i) {
        if (instance instanceof IndexedNetwork<?> indexedNetwork) {
            return indexedNetwork.polymer$getDecoder().apply(i);
        }

        return instance.get(i);
    }

    public static BlockState decodeState(int rawId) {
        BlockState state = InternalClientRegistry.getRealBlockState(rawId);

        if (state == null) {
            state = Block.STATE_IDS.get(rawId);
        }

        if (state == null) {
            if (Block.STATE_IDS.size() != 0) {
                errorDecode(rawId, "BlockState ID");
            }

            return Blocks.AIR.getDefaultState();
        }

        return state;
    }

    public static Item decodeItem(int id) {
        if (InternalClientRegistry.enabled) {
            var item = InternalClientRegistry.ITEMS.get(id);
            if (item != null) {
                if (item.registryEntry() != null) {
                    return item.registryEntry();
                } else if (PolymerImpl.USE_UNSAFE_ITEMS_CLIENT) {
                    return VIRTUAL_ITEM_CACHE.computeIfAbsent(item, VirtualClientItem::of);
                }
            }
        }

        return Registries.ITEM.get(id);
    }


    public static Enchantment decodeEnchantment(int id) {
        if (InternalClientRegistry.enabled) {
            var item = InternalClientRegistry.ENCHANTMENT.get(id);

            if (item != null && item.registryEntry() != null) {
                return item.registryEntry();
            }
        }

        return Enchantment.byRawId(id);
    }

    private static <T> void setSimpleDecoder(final IndexedIterable<T> registry, final PolymerRegistry<ClientPolymerEntry<T>> polymerRegistry) {
        IndexedNetwork.set(registry, (id) -> {
            if (InternalClientRegistry.enabled) {
                var item = polymerRegistry.get(id);

                if (item != null && item.registryEntry() != null) {
                    return item.registryEntry();
                }
            }

            return registry.get(id);
        });
    }

    private static void errorDecode(int rawId, String type) {
        if (PolymerImpl.LOG_INVALID_SERVER_IDS_CLIENT) {
            PolymerImpl.LOGGER.error("Invalid " + type + " (" + rawId + ")! Couldn't match it with any existing value!");
            var stack = Thread.currentThread().getStackTrace();
            if (stack.length > 3) {
                PolymerImpl.LOGGER.error("Caused by: " + stack[3].toString());
            }
        }
    }

    public static void tick() {
        final var each = DELAYED_ACTIONS.object2ObjectEntrySet().iterator();
        while (each.hasNext()) {
            if (each.next().getValue().tryDoing()) {
                each.remove();
            }
        }

        debugServerInfo = "[Polymer] C: " + CommonImpl.VERSION + ", S: " + InternalClientRegistry.serverVersion;

        var regInfo = new StringBuilder();
        regInfo.append("[Polymer] ");
        for (var reg : REGISTRIES) {
            regInfo.append(reg.getShortName());
            regInfo.append(": ");
            regInfo.append(reg.size());
            regInfo.append(", ");
        }

        regInfo.append("BS: " + InternalClientRegistry.BLOCK_STATES.size());

        debugRegistryInfo = regInfo.toString();

        TICK.invoke(Runnable::run);
    }

    public static void clear() {
        for (var reg : REGISTRIES) {
            reg.clear();
        }

        VIRTUAL_ITEM_CACHE.clear();

        BLOCKS.set(ClientPolymerBlock.NONE.identifier(), ClientPolymerBlock.NONE);
        ((PolymerIdList) BLOCK_STATES).polymer$clear();
        BLOCK_STATES.set(ClientPolymerBlock.NONE_STATE, 0);

        clearTabs(i -> true);
        for (var group : Registries.ITEM_GROUP) {
            if (group.getType() == ItemGroup.Type.CATEGORY) {
                ((ClientItemGroupExtension) group).polymer$clearStacks();
            }
        }
        if (ItemGroupsAccessor.getDisplayContext() != null) {
            ItemGroupsAccessor.callUpdateEntries(ItemGroupsAccessor.getDisplayContext());
        }
        PolymerClientUtils.ON_CLEAR.invoke(EventRunners.RUN);
    }

    private static final int TABS_PER_PAGE = 10;

    public static void clearTabs(Predicate<InternalClientItemGroup> removePredicate) {
        try {
            ITEM_GROUPS.removeIf(removePredicate);
            CreativeInventoryScreenAccessor.setSelectedTab(ItemGroups.getDefaultTab());

            if (CompatStatus.FABRIC_ITEM_GROUP) {
                try {
                    /*ItemGroupHelper.sortedGroups = validated.stream().sorted((a, b) -> {
                        if (a.isSpecial() && !b.isSpecial()) return 1;
                        if (!a.isSpecial() && b.isSpecial()) return -1;
                        return 0;
                    }).toList();*/
                } catch (Throwable e) {

                }

                try {
                    var f1 = CreativeInventoryScreen.class.getDeclaredField("fabric_currentPage");
                    f1.setAccessible(true);
                    f1.setInt(null, 0);
                } catch (Throwable e) {
                    if (PolymerImpl.LOG_MORE_ERRORS) {
                        PolymerImpl.LOGGER.error("Failed to change item group page (FABRIC)!", e);
                    }
                }
            }

            if (CompatStatus.QUILT_ITEM_GROUP) {
                try {
                    for (var f1 : CreativeInventoryScreen.class.getDeclaredFields()) {
                        if (f1.getName().contains("quilt$currentPage")) {
                            f1.setAccessible(true);
                            f1.setInt(null, 0);
                            break;
                        }
                    }
                } catch (Throwable e) {
                    if (PolymerImpl.LOG_MORE_ERRORS) {
                        PolymerImpl.LOGGER.error("Failed to change item group page (QUILT)!", e);
                    }
                }

            }

            int count = Registries.ITEM_GROUP.size() - 4;
            for (var x : ITEM_GROUPS) {
                var page = (count / TABS_PER_PAGE);
                int pageIndex = count % TABS_PER_PAGE;
                ItemGroup.Row row = pageIndex < (TABS_PER_PAGE / 2) ? ItemGroup.Row.TOP : ItemGroup.Row.BOTTOM;
                var c = row == ItemGroup.Row.TOP ? pageIndex % TABS_PER_PAGE : (pageIndex - TABS_PER_PAGE / 2) % (TABS_PER_PAGE);
                ((ClientItemGroupExtension) x).polymerCore$setPos(row, c);
                setItemGroupPage(x, page);
                count++;
            }
        } catch (Throwable e) {

        }
    }

    private static void setItemGroupPage(ItemGroup group, int page) {
        ((ClientItemGroupExtension) group).polymerCore$setPage(page);
        if (CompatStatus.FABRIC_ITEM_GROUP) {
            try {
                ((net.fabricmc.fabric.impl.itemgroup.FabricItemGroup) group).setPage(page);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public static void createItemGroup(Identifier id, Text name, ItemStack icon) {
        try {
            var existing = Registries.ITEM_GROUP.get(id);
            if (existing != null) {
                return;
            }
            int count = (Registries.ITEM_GROUP.size() - 4) + ITEM_GROUPS.size();

            var page = (count / TABS_PER_PAGE);
            int pageIndex = count % TABS_PER_PAGE;
            ItemGroup.Row row = pageIndex < (TABS_PER_PAGE / 2) ? ItemGroup.Row.TOP : ItemGroup.Row.BOTTOM;
            var c = row == ItemGroup.Row.TOP ? pageIndex % TABS_PER_PAGE : (pageIndex - TABS_PER_PAGE / 2) % (TABS_PER_PAGE);

            var group = new InternalClientItemGroup(row, c, id, name, icon);
            ITEM_GROUPS.set(id, group);
            setItemGroupPage(group, page);
        } catch(Throwable e) {

        }
    }

    public static ItemGroup getItemGroup(Identifier id) {
        var x = ITEM_GROUPS.get(id);
        if (x != null) {
            return x;
        }
        return Registries.ITEM_GROUP.get(id);
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

    }

    public static String getModName(Identifier id) {
        var container = FabricLoader.getInstance().getModContainer(id.getNamespace());
        return container.isPresent() ? container.get().getMetadata().getName() : (id.getNamespace() + "*");
    }
}
