package eu.pb4.polymer.impl.client;

import eu.pb4.polymer.api.client.PolymerClientUtils;
import eu.pb4.polymer.api.client.registry.ClientPolymerBlock;
import eu.pb4.polymer.api.client.registry.ClientPolymerEntityType;
import eu.pb4.polymer.api.client.registry.ClientPolymerItem;
import eu.pb4.polymer.impl.client.interfaces.ClientBlockStorageInterface;
import eu.pb4.polymer.impl.client.interfaces.ClientItemGroupExtension;
import eu.pb4.polymer.impl.interfaces.NetworkIdList;
import eu.pb4.polymer.impl.other.DelayedAction;
import eu.pb4.polymer.impl.other.EventRunners;
import eu.pb4.polymer.impl.other.ImplPolymerRegistry;
import eu.pb4.polymer.mixin.client.item.CreativeInventoryScreenAccessor;
import eu.pb4.polymer.mixin.other.ItemGroupAccessor;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class InternalClientRegistry {
    private static final Map<String, DelayedAction> DELAYED_ACTIONS = new Object2ObjectOpenHashMap<>();

    public static boolean ENABLED = false;
    public static int SYNC_REQUESTS = 0;
    public static String SERVER_VERSION = "";
    public static final Object2IntMap<String> CLIENT_PROTOCOL = new Object2IntOpenHashMap<>();

    public static final Int2ObjectMap<Identifier> ARMOR_TEXTURES_1 = new Int2ObjectOpenHashMap<>();
    public static final Int2ObjectMap<Identifier> ARMOR_TEXTURES_2 = new Int2ObjectOpenHashMap<>();

    public static final ImplPolymerRegistry<ClientPolymerBlock> BLOCKS = new ImplPolymerRegistry<>(ClientPolymerBlock.NONE.identifier(), ClientPolymerBlock.NONE);
    public static final IdList<ClientPolymerBlock.State> BLOCK_STATES = new IdList<>();

    public static final ImplPolymerRegistry<ClientPolymerItem> ITEMS = new ImplPolymerRegistry<>();
    public static final ImplPolymerRegistry<InternalClientItemGroup> ITEM_GROUPS = new ImplPolymerRegistry<>();
    public static final HashMap<String, ItemGroup> VANILLA_ITEM_GROUPS = new HashMap<>();

    public static final ImplPolymerRegistry<ClientPolymerEntityType> ENTITY_TYPE = new ImplPolymerRegistry<>();


    public static ClientPolymerBlock.State getBlockAt(BlockPos pos) {
        if (MinecraftClient.getInstance().world != null) {
            var chunk = MinecraftClient.getInstance().world.getChunk(pos);

            if (chunk instanceof ClientBlockStorageInterface storage) {
                return storage.polymer_getClientPolymerBlock(pos.getX(), pos.getY(), pos.getZ());
            }
        }

        return ClientPolymerBlock.NONE_STATE;
    }

    public static void setBlockAt(BlockPos pos, ClientPolymerBlock.State state) {
        if (MinecraftClient.getInstance().world != null) {
            var chunk = MinecraftClient.getInstance().world.getChunk(pos);

            if (chunk instanceof ClientBlockStorageInterface storage) {
                storage.polymer_setClientPolymerBlock(pos.getX(), pos.getY(), pos.getZ(), state);
            }
        }
    }

    public static void setVersion(String version) {
        SERVER_VERSION = version;
        ENABLED = !version.isEmpty();
    }

    public static void disable() {
        DELAYED_ACTIONS.clear();
        clear();
        setVersion("");
        CLIENT_PROTOCOL.clear();
        SYNC_REQUESTS = 0;
    }

    public static void tick() {
        DELAYED_ACTIONS.entrySet().removeIf(e -> e.getValue().tryDoing());
    }

    public static void clear() {
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
    }

    public static int getProtocol(String identifier) {
        return CLIENT_PROTOCOL.getOrDefault(identifier, -1);
    }

    public static void delayAction(String id, int time, Runnable action) {
        if (ENABLED) {
            DELAYED_ACTIONS.put(id, new DelayedAction(id, time, action));
        }
    }
}
