package eu.pb4.polymer.impl.client;

import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.api.client.PolymerClientDecoded;
import eu.pb4.polymer.api.client.PolymerClientUtils;
import eu.pb4.polymer.api.client.registry.ClientPolymerBlock;
import eu.pb4.polymer.api.client.registry.ClientPolymerEntityType;
import eu.pb4.polymer.api.client.registry.ClientPolymerEntry;
import eu.pb4.polymer.api.client.registry.ClientPolymerItem;
import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.api.utils.events.SimpleEvent;
import eu.pb4.polymer.impl.PolymerImpl;
import eu.pb4.polymer.impl.client.interfaces.ClientBlockStorageInterface;
import eu.pb4.polymer.impl.client.interfaces.ClientItemGroupExtension;
import eu.pb4.polymer.impl.client.interfaces.MutableSearchableContainer;
import eu.pb4.polymer.impl.client.networking.PolymerClientProtocolHandler;
import eu.pb4.polymer.impl.compat.CompatStatus;
import eu.pb4.polymer.impl.interfaces.PolymerIdList;
import eu.pb4.polymer.impl.other.DelayedAction;
import eu.pb4.polymer.impl.other.EventRunners;
import eu.pb4.polymer.impl.other.ImplPolymerRegistry;
import eu.pb4.polymer.mixin.client.item.CreativeInventoryScreenAccessor;
import eu.pb4.polymer.mixin.other.ItemGroupAccessor;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.search.SearchManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class InternalClientRegistry {
    public static final SimpleEvent<Runnable> TICK = new SimpleEvent<>();
    private static final Object2ObjectMap<String, DelayedAction> DELAYED_ACTIONS = new Object2ObjectArrayMap<>();

    public static boolean enabled = false;
    public static int syncRequests = 0;
    public static String serverVersion = "";
    public static boolean isClientOutdated = false;
    @Deprecated
    public static boolean legacyBlockState = false;
    public static final Object2IntMap<String> CLIENT_PROTOCOL = new Object2IntOpenHashMap<>();

    public static boolean hasArmorTextures = false;
    public static final Int2ObjectMap<Identifier> ARMOR_TEXTURES_1 = new Int2ObjectOpenHashMap<>();
    public static final Int2ObjectMap<Identifier> ARMOR_TEXTURES_2 = new Int2ObjectOpenHashMap<>();

    public static final ImplPolymerRegistry<ClientPolymerBlock> BLOCKS = new ImplPolymerRegistry<>("block", "B",  ClientPolymerBlock.NONE.identifier(), ClientPolymerBlock.NONE);
    public static final IdList<ClientPolymerBlock.State> BLOCK_STATES = new IdList<>();

    public static final ImplPolymerRegistry<ClientPolymerItem> ITEMS = new ImplPolymerRegistry<>("item", "I");
    public static final ImplPolymerRegistry<InternalClientItemGroup> ITEM_GROUPS = new ImplPolymerRegistry<>("item_group", "IG");

    public static final ImplPolymerRegistry<ClientPolymerEntityType> ENTITY_TYPES = new ImplPolymerRegistry<>("entity_type", "E");
    public static final ImplPolymerRegistry<ClientPolymerEntry<VillagerProfession>> VILLAGER_PROFESSIONS = new ImplPolymerRegistry<>("villager_profession", "VP");
    public static final ImplPolymerRegistry<ClientPolymerEntry<BlockEntityType<?>>> BLOCK_ENTITY = new ImplPolymerRegistry<>("block_entity", "BE");
    public static final ImplPolymerRegistry<ClientPolymerEntry<StatusEffect>> STATUS_EFFECT = new ImplPolymerRegistry<>("status_effect", "SE");

    public static final HashMap<String, ItemGroup> VANILLA_ITEM_GROUPS = new HashMap<>();
    public static final List<ImplPolymerRegistry<?>> REGISTRIES = List.of(ITEMS, ITEM_GROUPS, BLOCKS, BLOCK_ENTITY, ENTITY_TYPES, STATUS_EFFECT, VILLAGER_PROFESSIONS);
    public static final Map<Registry<?>, ImplPolymerRegistry<ClientPolymerEntry<?>>> BY_VANILLA = createRegMap();

    private static Map<Registry<?>, ImplPolymerRegistry<ClientPolymerEntry<?>>> createRegMap() {
        var map = new HashMap<Registry<?>, ImplPolymerRegistry<?>>();
        map.put(Registry.BLOCK, BLOCKS);
        map.put(Registry.ENTITY_TYPE, ENTITY_TYPES);
        map.put(Registry.ITEM, ITEMS);
        map.put(Registry.STATUS_EFFECT, STATUS_EFFECT);
        map.put(Registry.VILLAGER_PROFESSION, VILLAGER_PROFESSIONS);
        return (Map<Registry<?>, ImplPolymerRegistry<ClientPolymerEntry<?>>>) (Object) map;
    }

    public static String debugRegistryInfo = "";
    public static String debugServerInfo = "";
    @Deprecated
    public static int blockOffset = -1;

    public static ClientPolymerBlock.State getBlockAt(BlockPos pos) {
        if (MinecraftClient.getInstance().world != null) {
            var chunk = MinecraftClient.getInstance().world.getChunkManager().getChunk(
                    ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()),
                    ChunkStatus.FULL,
                    true
            );

            return ((ClientBlockStorageInterface) chunk).polymer_getClientPolymerBlock(pos.getX(), pos.getY(), pos.getZ());
        }

        return ClientPolymerBlock.NONE_STATE;
    }

    public static void setBlockAt(BlockPos pos, ClientPolymerBlock.State state) {
        if (MinecraftClient.getInstance().world != null) {
            var chunk = MinecraftClient.getInstance().world.getChunkManager().getChunk(
                    ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()),
                    ChunkStatus.FULL,
                    true
            );

            ((ClientBlockStorageInterface) chunk).polymer_setClientPolymerBlock(pos.getX(), pos.getY(), pos.getZ(), state);
        }
    }

    public static void setVersion(String version) {
        serverVersion = version;
        isClientOutdated = version.isEmpty() ? false : PolymerImpl.isOlderThan(version);
        enabled = !version.isEmpty();
    }

    public static void disable() {
        setVersion("");
        clear();
        DELAYED_ACTIONS.clear();
        CLIENT_PROTOCOL.clear();
        syncRequests = 0;
        legacyBlockState = false;
        blockOffset = -1;
        rebuildSearch();
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

    public static BlockState decodeState(int rawId) {
        if (Block.STATE_IDS.size() == 0) {
            // States broke by something, it seems to happen after game crash...
            // Should limit log spam caused by it
            return Blocks.AIR.getDefaultState();
        }

        BlockState state;

        if (legacyBlockState) {
            if (rawId >= PolymerClientUtils.getBlockStateOffset()) {
                state = InternalClientRegistry.getRealBlockState(rawId - PolymerClientUtils.getBlockStateOffset() + 1);
            } else {
                state = Block.STATE_IDS.get(rawId);
            }
        } else {
            state = InternalClientRegistry.getRealBlockState(rawId);

            if (state == null) {
                state = Block.STATE_IDS.get(rawId);
            }
        }
        if (state == null) {
            errorDecode(rawId, "BlockState ID");
            return Blocks.AIR.getDefaultState();
        }

        return state;
    }

    public static Item decodeItem(int id) {
        if (InternalClientRegistry.enabled) {
            var item = InternalClientRegistry.ITEMS.get(id);

            if (item != null && item.registryEntry() != null) {
                return item.registryEntry();
            }
        }

        return Item.byRawId(id);
    }

    public static BlockEntityType<?> decodeBlockEntityType(int id) {
        if (InternalClientRegistry.enabled) {
            var item = InternalClientRegistry.BLOCK_ENTITY.get(id);

            if (item != null && item.registryEntry() != null) {
                return item.registryEntry();
            }
        }

        return Registry.BLOCK_ENTITY_TYPE.get(id);
    }

    public static StatusEffect decodeStatusEffect(int id) {
        if (InternalClientRegistry.enabled) {
            var item = InternalClientRegistry.STATUS_EFFECT.get(id);

            if (item != null && item.registryEntry() != null) {
                return item.registryEntry();
            }
        }

        return Registry.STATUS_EFFECT.get(id);
    }

    public static EntityType<?> decodeEntity(int id) {
        if (InternalClientRegistry.enabled) {
            var item = InternalClientRegistry.ENTITY_TYPES.get(id);

            if (item != null && item.registryEntry() != null) {
                return item.registryEntry();
            }
        }

        return Registry.ENTITY_TYPE.get(id);
    }

    public static VillagerProfession decodeVillagerProfession(int id) {
        if (InternalClientRegistry.enabled) {
            var item = InternalClientRegistry.VILLAGER_PROFESSIONS.get(id);

            if (item != null && item.registryEntry() != null) {
                return item.registryEntry();
            }
        }

        return Registry.VILLAGER_PROFESSION.get(id);
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

        PolymerClientProtocolHandler.tick();

        debugServerInfo = "[Polymer] C: " + PolymerImpl.VERSION + (isClientOutdated ? " (Outdated)" : "") + ", S: " + InternalClientRegistry.serverVersion + (legacyBlockState ? "| Legacy BSO: " + blockOffset : "") + " | PPS: " + PolymerClientProtocolHandler.packetsPerSecond;

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

        BLOCKS.set(ClientPolymerBlock.NONE.identifier(), ClientPolymerBlock.NONE);
        ((PolymerIdList) BLOCK_STATES).polymer_clear();
        BLOCK_STATES.set(ClientPolymerBlock.NONE_STATE, 0);

        clearTabs(i -> true);
        for (var group : ItemGroup.GROUPS) {
            ((ClientItemGroupExtension) group).polymer_clearStacks();
        }
        PolymerClientUtils.ON_CLEAR.invoke(EventRunners.RUN);
    }

    public static void clearTabs(Predicate<InternalClientItemGroup> removePredicate) {
        var array = ItemGroupAccessor.getGROUPS();

        var list = new ArrayList<ItemGroup>();

        int posOffset = 0;

        for (int i = 0; i < array.length; i++) {
            if (array[i] instanceof InternalClientItemGroup group && removePredicate.test(group)) {
                posOffset++;
                ITEM_GROUPS.remove(group);
                ((ItemGroupAccessor) array[i]).setIndex(0);
            } else if (array[i] != null) {
                ((ItemGroupAccessor) array[i]).setIndex(i - posOffset);
                list.add(array[i]);
            }
        }

        if (list.size() <= CreativeInventoryScreenAccessor.getSelectedTab()) {
            CreativeInventoryScreenAccessor.setSelectedTab(ItemGroup.BUILDING_BLOCKS.getIndex());

            if (CompatStatus.FABRIC_ITEM_GROUP) {
                try {
                    Field f1 = CreativeInventoryScreen.class.getDeclaredField("fabric_currentPage");
                    f1.setAccessible(true);
                    f1.setInt(null, 0);
                } catch (Throwable e) {
                    // noop
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
                    // noop
                }
            }
        }

        ItemGroupAccessor.setGROUPS(list.toArray(new ItemGroup[0]));
    }

    public static int getClientProtocolVer(String identifier) {
        return CLIENT_PROTOCOL.getOrDefault(identifier, -1);
    }

    public static void delayAction(String id, int time, Runnable action) {
        if (enabled) {
            DELAYED_ACTIONS.put(id, new DelayedAction(id, time, action));
        }
    }

    public static void rebuildSearch() {
        var a = MinecraftClient.getInstance().getSearchableContainer(SearchManager.ITEM_TOOLTIP);
        var b = MinecraftClient.getInstance().getSearchableContainer(SearchManager.ITEM_TAG);

        ((MutableSearchableContainer) a).polymer_removeIf(InternalClientRegistry::isPolymerItemStack);
        ((MutableSearchableContainer) b).polymer_removeIf(InternalClientRegistry::isPolymerItemStack);

        for (var group : ItemGroup.GROUPS) {
            if (group == ItemGroup.SEARCH) {
                continue;
            }

            Collection<ItemStack> stacks;

            if (group instanceof InternalClientItemGroup clientItemGroup) {
                stacks = clientItemGroup.getStacks();
            } else {
                stacks = ((ClientItemGroupExtension) group).polymer_getStacks();
            }

            if (stacks != null) {
                for (var stack : stacks.toArray(new ItemStack[0])) {
                    a.add(stack);
                    b.add(stack);
                }
            }
        }

        a.reload();
        b.reload();
    }

    private static boolean isPolymerItemStack(Object o) {
        return o instanceof ItemStack stack && (PolymerItemUtils.isPolymerServerItem(stack) || PolymerItemUtils.getPolymerIdentifier(stack) != null);
    }
}
