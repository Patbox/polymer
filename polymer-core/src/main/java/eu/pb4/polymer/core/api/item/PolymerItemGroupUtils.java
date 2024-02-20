package eu.pb4.polymer.core.api.item;

import eu.pb4.polymer.common.api.events.SimpleEvent;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.core.api.utils.PolymerRegistry;
import eu.pb4.polymer.core.impl.InternalServerRegistry;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.interfaces.ItemGroupExtra;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.*;


/**
 * An server side item group that can be synchronized with polymer clients
 * It also has it's own server side functionality
 */
public final class PolymerItemGroupUtils {
    public static final PolymerRegistry<ItemGroup> REGISTRY = InternalServerRegistry.ITEM_GROUPS;
    /**
     * Even called on synchronization of ItemGroups
     */
    public static final SimpleEvent<ItemGroupEventListener> LIST_EVENT = new SimpleEvent<>();
    private static final Map<ItemGroupKey, Contents> CONTENT_CACHE = new HashMap<>();

    private PolymerItemGroupUtils() {
    }

    public static Contents getContentsFor(ServerPlayerEntity player, ItemGroup group) {
        return getContentsFor(group, player.getServer().getRegistryManager(), player.getServerWorld().getEnabledFeatures(), CommonImplUtils.permissionCheck(player, "op_items", 2));
    }

    public static Contents getContentsFor(ItemGroup group, RegistryWrapper.WrapperLookup lookup, FeatureSet featureSet, boolean operator) {
        var key = new ItemGroupKey(getId(group), operator);
        var value = CONTENT_CACHE.get(key);
        if (value == null) {
            try {
                 value = ((ItemGroupExtra) group).polymer$getContentsWith(featureSet, operator, lookup);
            } catch (Throwable t) {
                // Some 1.20 mods use client classes in their item groups because vanilla doesn't call them on the server anymore
                // Catch instead of letting the game crash, even though it's their fault...
                PolymerImpl.LOGGER.warn("Failed to load contents for an ItemGroup", t);
                value = new Contents(List.of(), List.of());
            }
            CONTENT_CACHE.put(key, value);
        }
        return value;
    }

    /**
     * Returns list of ItemGroups accessible by player
     */
    public static List<ItemGroup> getItemGroups(ServerPlayerEntity player) {
        var list = new LinkedHashSet<ItemGroup>();

        for (var g : ItemGroups.getGroups()) {
            try {
                if (g.getType() == ItemGroup.Type.CATEGORY && ((ItemGroupExtra) g).polymer$isSyncable()) {
                    list.add(g);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        for (var g : InternalServerRegistry.ITEM_GROUPS) {
            try {
                if (g.getType() == ItemGroup.Type.CATEGORY && ((ItemGroupExtra) g).polymer$isSyncable()) {
                    list.add(g);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        var sync = new PolymerItemGroupUtils.ItemGroupListBuilder() {
            @Override
            public void add(ItemGroup group) {
                list.add(group);
            }

            @Override
            public void remove(ItemGroup group) {
                list.remove(group);
            }
        };

        PolymerItemGroupUtils.LIST_EVENT.invoke((x) -> x.onItemGroupGet(player, sync));

        return new ArrayList<>(list);
    }

    public static boolean isPolymerItemGroup(ItemGroup group) {
        return InternalServerRegistry.ITEM_GROUPS.containsEntry(group);
    }

    public static ItemGroup.Builder builder() {
        return new ItemGroup.Builder(ItemGroup.Row.BOTTOM, -1);
    }

    public static void registerPolymerItemGroup(Identifier identifier, ItemGroup group) {
        InternalServerRegistry.ITEM_GROUPS.set(identifier, group);
        if (Registries.ITEM_GROUP.containsId(identifier)) {
            PolymerImpl.LOGGER.warn("ItemGroup '{}' is already registered as vanilla!", identifier);
        }
    }

    public static Identifier getId(ItemGroup group) {
        var x = REGISTRY.getId(group);

        if (x == null) {
            return Registries.ITEM_GROUP.getId(group);
        }
        return x;
    }

    public static void invalidateItemGroupCache() {
        CONTENT_CACHE.clear();
    }

    @FunctionalInterface
    public interface ItemGroupEventListener {
        void onItemGroupGet(ServerPlayerEntity player, ItemGroupListBuilder builder);
    }

    public interface ItemGroupListBuilder {
        void add(ItemGroup group);

        void remove(ItemGroup group);
    }

    public record Contents(Collection<ItemStack> main, Collection<ItemStack> search) {
    }

    private record ItemGroupKey(Identifier identifier, boolean operator) {}
}
