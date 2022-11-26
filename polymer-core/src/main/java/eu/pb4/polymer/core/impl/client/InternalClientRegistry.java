package eu.pb4.polymer.core.impl.client;

import eu.pb4.polymer.common.api.events.SimpleEvent;
import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.client.*;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.client.interfaces.ClientBlockStorageInterface;
import eu.pb4.polymer.core.impl.client.interfaces.ClientItemGroupExtension;
import eu.pb4.polymer.core.impl.client.networking.PolymerClientProtocolHandler;
import eu.pb4.polymer.core.impl.interfaces.IndexedNetwork;
import eu.pb4.polymer.core.impl.interfaces.PolymerIdList;
import eu.pb4.polymer.core.impl.other.DelayedAction;
import eu.pb4.polymer.core.impl.other.EventRunners;
import eu.pb4.polymer.core.impl.other.ImplPolymerRegistry;
import eu.pb4.polymer.core.mixin.other.ItemGroupsAccessor;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.impl.itemgroup.ItemGroupHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
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

import java.util.ArrayList;
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
    public static final IdList<ClientPolymerBlock.State> BLOCK_STATES = new IdList<>();
    public static final ImplPolymerRegistry<ClientPolymerItem> ITEMS = new ImplPolymerRegistry<>("item", "I");
    public static final ImplPolymerRegistry<InternalClientItemGroup> ITEM_GROUPS = new ImplPolymerRegistry<>("item_group", "IG");
    public static final ImplPolymerRegistry<ClientPolymerEntityType> ENTITY_TYPES = new ImplPolymerRegistry<>("entity_type", "E");
    public static final ImplPolymerRegistry<ClientPolymerEntry<VillagerProfession>> VILLAGER_PROFESSIONS = new ImplPolymerRegistry<>("villager_profession", "VP");
    public static final ImplPolymerRegistry<ClientPolymerEntry<BlockEntityType<?>>> BLOCK_ENTITY = new ImplPolymerRegistry<>("block_entity", "BE");
    public static final ImplPolymerRegistry<ClientPolymerEntry<StatusEffect>> STATUS_EFFECT = new ImplPolymerRegistry<>("status_effect", "SE");
    public static final ImplPolymerRegistry<ClientPolymerEntry<Enchantment>> ENCHANTMENT = new ImplPolymerRegistry<>("enchantment", "EN");
    public static final HashMap<Identifier, ItemGroup> VANILLA_ITEM_GROUPS = new HashMap<>();
    public static final List<ImplPolymerRegistry<?>> REGISTRIES = List.of(ITEMS, ITEM_GROUPS, BLOCKS, BLOCK_ENTITY, ENTITY_TYPES, STATUS_EFFECT, VILLAGER_PROFESSIONS, ENCHANTMENT);
    public static final Map<Registry<?>, ImplPolymerRegistry<ClientPolymerEntry<?>>> BY_VANILLA = createRegMap();
    public static final Map<Identifier, ImplPolymerRegistry<ClientPolymerEntry<?>>> BY_VANILLA_ID = createRegMapId(BY_VANILLA);
    private static final Object2ObjectMap<String, DelayedAction> DELAYED_ACTIONS = new Object2ObjectArrayMap<>();
    private static final Map<ClientPolymerItem, VirtualClientItem> VIRTUAL_ITEM_CACHE = new Object2ObjectOpenHashMap<>();
    public static boolean enabled = false;
    public static int syncRequests = 0;
    public static String serverVersion = "";
    public static boolean isClientOutdated = false;
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
        return (Map<Registry<?>, ImplPolymerRegistry<ClientPolymerEntry<?>>>) (Object) map;
    }

    private static Map<Identifier, ImplPolymerRegistry<ClientPolymerEntry<?>>> createRegMapId(Map<Registry<?>, ImplPolymerRegistry<ClientPolymerEntry<?>>> byVanilla) {
        return byVanilla.entrySet().stream().map(x -> Map.entry(x.getKey().getKey().getValue(), x.getValue())).collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
    }

    public static ClientPolymerBlock.State getBlockAt(BlockPos pos) {
        if (MinecraftClient.getInstance().world != null) {
            var chunk = MinecraftClient.getInstance().world.getChunkManager().getChunk(
                    ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()),
                    ChunkStatus.FULL,
                    true
            );

            return ((ClientBlockStorageInterface) chunk).polymer$getClientBlock(pos.getX(), pos.getY(), pos.getZ());
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

            if (chunk != null) {
                ((ClientBlockStorageInterface) chunk).polymer$setClientBlock(pos.getX(), pos.getY(), pos.getZ(), state);
            }
        }
    }

    public static void setVersion(String version) {
        serverVersion = version;
        isClientOutdated = !version.isEmpty() && PolymerImpl.isOlderThan(version);
        enabled = !version.isEmpty();
    }

    public static void disable() {
        setVersion("");
        clear();
        DELAYED_ACTIONS.clear();
        CLIENT_PROTOCOL.clear();
        syncRequests = 0;
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
        IndexedNetwork.set(Registries.ENTITY_TYPE, InternalClientRegistry::decodeEntity);
        IndexedNetwork.set(Registries.ENCHANTMENT, InternalClientRegistry::decodeEnchantment);
        IndexedNetwork.set(Registries.ITEM, InternalClientRegistry::decodeItem);
        IndexedNetwork.set(Registries.BLOCK, InternalClientRegistry::decodeBlock);
        IndexedNetwork.set(Registries.VILLAGER_PROFESSION, InternalClientRegistry::decodeVillagerProfession);
        IndexedNetwork.set(Registries.STATUS_EFFECT, InternalClientRegistry::decodeStatusEffect);
        IndexedNetwork.set(Registries.BLOCK_ENTITY_TYPE, InternalClientRegistry::decodeBlockEntityType);
    }


    public static Object decodeRegistry(IndexedIterable instance, int i) {
        if (instance instanceof IndexedNetwork<?> indexedNetwork) {
            return indexedNetwork.polymer$getDecoder().apply(i);
        }

        return instance.get(i);
    }

    public static BlockState decodeState(int rawId) {
        if (Block.STATE_IDS.size() == 0) {
            // States broke by something, it seems to happen after game crash...
            // Should limit log spam caused by it
            return Blocks.AIR.getDefaultState();
        }

        BlockState state = InternalClientRegistry.getRealBlockState(rawId);

        if (state == null) {
            state = Block.STATE_IDS.get(rawId);
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

    public static Block decodeBlock(int id) {
        if (InternalClientRegistry.enabled) {
            var item = InternalClientRegistry.BLOCKS.get(id);
            if (item != null && item.registryEntry() != null) {
                return item.registryEntry();
            }
        }

        return Registries.BLOCK.get(id);
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

    public static BlockEntityType<?> decodeBlockEntityType(int id) {
        if (InternalClientRegistry.enabled) {
            var item = InternalClientRegistry.BLOCK_ENTITY.get(id);

            if (item != null && item.registryEntry() != null) {
                return item.registryEntry();
            }
        }

        return Registries.BLOCK_ENTITY_TYPE.get(id);
    }

    public static StatusEffect decodeStatusEffect(int id) {
        if (InternalClientRegistry.enabled) {
            var item = InternalClientRegistry.STATUS_EFFECT.get(id);

            if (item != null && item.registryEntry() != null) {
                return item.registryEntry();
            }
        }

        return Registries.STATUS_EFFECT.get(id);
    }

    public static EntityType<?> decodeEntity(int id) {
        if (InternalClientRegistry.enabled) {
            var item = InternalClientRegistry.ENTITY_TYPES.get(id);

            if (item != null && item.registryEntry() != null) {
                return item.registryEntry();
            }
        }

        return Registries.ENTITY_TYPE.get(id);
    }

    public static VillagerProfession decodeVillagerProfession(int id) {
        if (InternalClientRegistry.enabled) {
            var item = InternalClientRegistry.VILLAGER_PROFESSIONS.get(id);

            if (item != null && item.registryEntry() != null) {
                return item.registryEntry();
            }
        }

        return Registries.VILLAGER_PROFESSION.get(id);
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

        debugServerInfo = "[Polymer] C: " + PolymerImpl.VERSION + (isClientOutdated ? " (Outdated)" : "") + ", S: " + InternalClientRegistry.serverVersion + " | PPS: " + PolymerClientProtocolHandler.packetsPerSecond;

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
        for (var group : ItemGroups.getGroups()) {
            if (group.getType() == ItemGroup.Type.CATEGORY) {
                ((ClientItemGroupExtension) group).polymer$clearStacks();
            }
        }
        if (ItemGroups.enabledFeatures != null) {
            ItemGroupsAccessor.callUpdateEntries(ItemGroups.enabledFeatures, ItemGroups.operatorEnabled);
        }
        PolymerClientUtils.ON_CLEAR.invoke(EventRunners.RUN);
    }

    public static void clearTabs(Predicate<InternalClientItemGroup> removePredicate) {
        try {
            var itemGroups = new ArrayList<>(ItemGroups.getGroups());
            itemGroups.removeIf((i) -> i instanceof InternalClientItemGroup group && removePredicate.test(group));

            List<ItemGroup> validated = ItemGroupsAccessor.callCollect(itemGroups.toArray(ItemGroup[]::new));
            ItemGroupsAccessor.setGROUPS(validated);

            if (CompatStatus.FABRIC_ITEM_GROUP) {
                try {
                    ItemGroupHelper.sortedGroups = validated.stream().sorted((a, b) -> {
                        if (a.isSpecial() && !b.isSpecial()) return 1;
                        if (!a.isSpecial() && b.isSpecial()) return -1;
                        return 0;
                    }).toList();
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

        } catch (Throwable e) {

        }
    }

    public static void createItemGroup(Identifier id, Text name, ItemStack icon) {
        try {
            var group = new InternalClientItemGroup(null, -1, id, name, icon);

            if (CompatStatus.FABRIC_ITEM_GROUP) {
                try {
                    ItemGroupHelper.appendItemGroup(group);
                } catch (Throwable e) {

                }
            }
        } catch(Throwable e) {

        }
    }

    public static int getClientProtocolVer(String identifier) {
        return CLIENT_PROTOCOL.getOrDefault(identifier, -1);
    }

    public static void delayAction(String id, int time, Runnable action) {
        if (enabled) {
            DELAYED_ACTIONS.put(id, new DelayedAction(id, time, action));
        }
    }

    private static boolean isPolymerItemStack(Object o) {
        return o instanceof ItemStack stack && (PolymerItemUtils.isPolymerServerItem(stack) || PolymerItemUtils.getPolymerIdentifier(stack) != null);
    }

    static {
        setDecoders();
    }

    public static ItemGroup getItemGroup(Identifier id) {
        for (var group : ItemGroups.getGroups()) {
            if (group instanceof InternalClientItemGroup clientItemGroup && clientItemGroup.getIdentifier().equals(id)) {
                return group;
            } else if (PolymerImplUtils.toItemGroupId(group).equals(id)) {
                return group;
            }
        }

        return null;
    }

    public static boolean hideFromItemGroup(ItemStack stack) {
        return PolymerItemUtils.isPolymerServerItem(stack) || PolymerItemUtils.getServerIdentifier(stack) != null;
    }
}
