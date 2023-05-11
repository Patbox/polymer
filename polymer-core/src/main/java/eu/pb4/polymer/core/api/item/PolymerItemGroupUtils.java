package eu.pb4.polymer.core.api.item;

import eu.pb4.polymer.common.api.events.SimpleEvent;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.core.api.utils.PolymerRegistry;
import eu.pb4.polymer.core.impl.InternalServerRegistry;
import eu.pb4.polymer.core.impl.interfaces.ItemGroupExtra;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * An server side item group that can be synchronized with polymer clients
 * It also has it's own server side functionality
 */
public final class PolymerItemGroupUtils {
    private PolymerItemGroupUtils() {}
    /**
     * Even called on synchronization of ItemGroups
     */
    public static final SimpleEvent<ItemGroupEventListener> LIST_EVENT = new SimpleEvent<>();

    public static Contents getContentsFor(ServerPlayerEntity player, ItemGroup group) {
        return getContentsFor(group, player.getServer().getRegistryManager(), player.getServerWorld().getEnabledFeatures(), CommonImplUtils.permissionCheck(player, "op_items", 2));
    }

    public static Contents getContentsFor(ItemGroup group, RegistryWrapper.WrapperLookup lookup, FeatureSet featureSet, boolean operator) {
        return ((ItemGroupExtra) group).polymer$getContentsWith(featureSet, operator, lookup);
    }

    /**
     * Returns list of ItemGroups accessible by player
     */
    public static List<ItemGroup> getItemGroups(ServerPlayerEntity player) {
        var list = new LinkedHashSet<ItemGroup>();

        for (var g : ItemGroups.getGroups()) {
            if (g.getType() == ItemGroup.Type.CATEGORY && ((ItemGroupExtra) g).polymer$isSyncable()) {
                list.add(g);
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
        return InternalServerRegistry.ITEM_GROUPS.contains(group);
    }

    public static void registerPolymerItemGroup(ItemGroup group) {
        InternalServerRegistry.ITEM_GROUPS.add(group);
    }

    @FunctionalInterface
    public interface ItemGroupEventListener {
        void onItemGroupGet(ServerPlayerEntity player, ItemGroupListBuilder builder);
    }

    public interface ItemGroupListBuilder {
        void add(ItemGroup group);
        void remove(ItemGroup group);
    }

    public record Contents(Collection<ItemStack> main, Collection<ItemStack> search) {}
}
