package eu.pb4.polymer.impl.client;

import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.api.client.PolymerClientDecoded;
import eu.pb4.polymer.api.client.PolymerClientUtils;
import eu.pb4.polymer.api.client.registry.ClientPolymerBlock;
import eu.pb4.polymer.api.client.registry.ClientPolymerEntityType;
import eu.pb4.polymer.api.client.registry.ClientPolymerItem;
import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.impl.PolymerImpl;
import eu.pb4.polymer.impl.client.interfaces.ClientBlockStorageInterface;
import eu.pb4.polymer.impl.client.interfaces.ClientItemGroupExtension;
import eu.pb4.polymer.impl.client.interfaces.MutableSearchableContainer;
import eu.pb4.polymer.impl.client.networking.PolymerClientProtocolHandler;
import eu.pb4.polymer.impl.interfaces.NetworkIdList;
import eu.pb4.polymer.impl.other.DelayedAction;
import eu.pb4.polymer.impl.other.EventRunners;
import eu.pb4.polymer.impl.other.ImplPolymerRegistry;
import eu.pb4.polymer.mixin.client.item.CreativeInventoryScreenAccessor;
import eu.pb4.polymer.mixin.other.ItemGroupAccessor;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.search.SearchManager;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class InternalClientRegistry {
    public static boolean stable = false;
    public static boolean itemsMatch = false;
    private static final Object2ObjectMap<String, DelayedAction> DELAYED_ACTIONS = new Object2ObjectArrayMap<>();

    public static boolean enabled = false;
    public static int syncRequests = 0;
    public static String serverVersion = "";
    public static final Object2IntMap<String> CLIENT_PROTOCOL = new Object2IntOpenHashMap<>();

    public static final Int2ObjectMap<Identifier> ARMOR_TEXTURES_1 = new Int2ObjectOpenHashMap<>();
    public static final Int2ObjectMap<Identifier> ARMOR_TEXTURES_2 = new Int2ObjectOpenHashMap<>();

    public static final ImplPolymerRegistry<ClientPolymerBlock> BLOCKS = new ImplPolymerRegistry<>(ClientPolymerBlock.NONE.identifier(), ClientPolymerBlock.NONE);
    public static final IdList<ClientPolymerBlock.State> BLOCK_STATES = new IdList<>();

    public static final ImplPolymerRegistry<ClientPolymerItem> ITEMS = new ImplPolymerRegistry<>();
    public static final ImplPolymerRegistry<InternalClientItemGroup> ITEM_GROUPS = new ImplPolymerRegistry<>();
    public static final HashMap<String, ItemGroup> VANILLA_ITEM_GROUPS = new HashMap<>();

    public static final ImplPolymerRegistry<ClientPolymerEntityType> ENTITY_TYPE = new ImplPolymerRegistry<>();
    public static String debugRegistryInfo = "";
    public static String debugServerInfo = "";


    public static ClientPolymerBlock.State getBlockAt(BlockPos pos) {
        if (MinecraftClient.getInstance().world != null && stable) {
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
        if (MinecraftClient.getInstance().world != null && stable) {
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
        enabled = !version.isEmpty();
    }

    public static void disable() {
        setVersion("");
        clear();
        DELAYED_ACTIONS.clear();
        CLIENT_PROTOCOL.clear();
        syncRequests = 0;
        stable = false;
        itemsMatch = false;
        PolymerClientUtils.ON_DISABLE.invoke(Runnable::run);
    }

    public static BlockState getRealBlockState(int rawPolymerId) {
        var state = InternalClientRegistry.BLOCK_STATES.get(rawPolymerId);
        if (state != null && state.realServerBlockState() != null) {
            if (PolymerClientDecoded.checkDecode(state.realServerBlockState().getBlock())) {
                return state.realServerBlockState();
            } else {
                return PolymerBlockUtils.getPolymerBlockState(state.realServerBlockState(), ClientUtils.getPlayer());
            }
        }

        return Blocks.AIR.getDefaultState();
    }

    public static void tick() {
        final var each = DELAYED_ACTIONS.object2ObjectEntrySet().iterator();
        while (each.hasNext()) {
            if (each.next().getValue().tryDoing()) {
                each.remove();
            }
        }

        PolymerClientProtocolHandler.tick();

        debugServerInfo = "[Polymer] C: " + PolymerImpl.VERSION + ", S: " + InternalClientRegistry.serverVersion + " | PPS: " + PolymerClientProtocolHandler.packetsPerSecond;
        debugRegistryInfo = "[Polymer] I: " + InternalClientRegistry.ITEMS.size() + ", IG: " + InternalClientRegistry.ITEM_GROUPS.size() + ", B: " + InternalClientRegistry.BLOCKS.size() + ", BS: " + InternalClientRegistry.BLOCK_STATES.size() + ", E: " + InternalClientRegistry.ENTITY_TYPE.size();
    }

    public static void clear() {
        stable = false;

        BLOCKS.clear();
        BLOCKS.set(ClientPolymerBlock.NONE.identifier(), ClientPolymerBlock.NONE);
        ITEMS.clear();
        ITEM_GROUPS.clear();
        ((NetworkIdList) BLOCK_STATES).polymer_clear();

        BLOCK_STATES.set(ClientPolymerBlock.NONE_STATE, 0);

        clearTabs(i -> true);
        for (var group : ItemGroup.GROUPS) {
            ((ClientItemGroupExtension) group).polymer_clearStacks();
        }
        stable = true;
        PolymerClientUtils.ON_CLEAR.invoke(EventRunners.RUN);
    }

    public static void clearTabs(Predicate<InternalClientItemGroup> removePredicate) {
        stable = false;

        var array = ItemGroupAccessor.getGROUPS();

        var list = new ArrayList<ItemGroup>();

        int posOffset = 0;

        for (int i = 0; i < array.length; i++) {
            if (array[i] instanceof InternalClientItemGroup group && removePredicate.test(group)) {
                posOffset++;
                ITEM_GROUPS.remove(group);
                ((ItemGroupAccessor) array[i]).setIndex(0);
            } else {
                ((ItemGroupAccessor) array[i]).setIndex(i - posOffset);
                list.add(array[i]);
            }
        }

        if (list.size() <= CreativeInventoryScreenAccessor.getSelectedTab()) {
            CreativeInventoryScreenAccessor.setSelectedTab(ItemGroup.BUILDING_BLOCKS.getIndex());
            try {
                //noinspection JavaReflectionMemberAccess
                Field f1 = CreativeInventoryScreen.class.getDeclaredField("fabric_currentPage");
                f1.setAccessible(true);
                f1.set(null, 0);
            } catch (Exception e) {
                // noop
            }
        }

        ItemGroupAccessor.setGROUPS(list.toArray(new ItemGroup[0]));
        stable = true;
    }

    public static int getProtocol(String identifier) {
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

        ((MutableSearchableContainer) a).polymer_removeIf((s) -> s instanceof ItemStack stack && (PolymerItemUtils.isPolymerServerItem(stack) || PolymerItemUtils.getPolymerIdentifier(stack) != null));
        ((MutableSearchableContainer) b).polymer_removeIf((s) -> s instanceof ItemStack stack && (PolymerItemUtils.isPolymerServerItem(stack) || PolymerItemUtils.getPolymerIdentifier(stack) != null));

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
                for (var stack : stacks) {
                    a.add(stack);
                    b.add(stack);
                }
            }
        }

        a.reload();
        b.reload();
    }
}
