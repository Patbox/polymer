package eu.pb4.polymer.core.api.item;

import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.core.api.utils.PolymerRegistry;
import eu.pb4.polymer.core.impl.InternalServerRegistry;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.interfaces.CreativeModeTabExtra;
import eu.pb4.polymer.core.mixin.CreativeModeTabAccessor;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

import java.util.*;


/**
 * A server side item group that can be synchronized with polymer clients
 * It also has its own server side functionality
 */
public final class PolymerCreativeModeTabUtils {
    public static final PolymerRegistry<CreativeModeTab> REGISTRY = InternalServerRegistry.ITEM_GROUPS;
    /**
     * Even called on synchronization of ItemGroups
     */
    public static final Event<CreativeModeTabEventListener> LIST_EVENT = EventFactory.createArrayBacked(CreativeModeTabEventListener.class, arr ->
            (player, builder) -> {
                for (var a : arr) {
                    a.onCreativeModeTabGet(player, builder);
                }
            });
    private static final Map<CreativeModeTabKey, Contents> CONTENT_CACHE = new HashMap<>();

    private PolymerCreativeModeTabUtils() {
    }

    public static Contents getContentsFor(ServerPlayer player, CreativeModeTab group) {
        return getContentsFor(group, player.level().getServer().registryAccess(), player.level().enabledFeatures(), CommonImplUtils.permissionCheck(player, "op_items", 2));
    }

    public static Contents getContentsFor(CreativeModeTab group, HolderLookup.Provider lookup, FeatureFlagSet featureSet, boolean operator) {
        var key = new CreativeModeTabKey(getId(group), operator);
        var value = CONTENT_CACHE.get(key);
        if (value == null) {
            try {
                value = ((CreativeModeTabExtra) group).polymer$getContentsWith(getId(group), featureSet, operator, lookup);
            } catch (Throwable t) {
                // Some mods use client classes in their item groups because vanilla doesn't call them on the server anymore
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
    public static List<CreativeModeTab> getCreativeModeTabs(ServerPlayer player) {
        var list = new LinkedHashSet<CreativeModeTab>();

        for (var g : CreativeModeTabs.allTabs()) {
            try {
                if (g.getType() == CreativeModeTab.Type.CATEGORY && ((CreativeModeTabExtra) g).polymer$isSyncable()) {
                    list.add(g);
                }
            } catch (Throwable e) {
                PolymerImpl.LOGGER.warn("Something broke!", e);
            }
        }

        for (var g : InternalServerRegistry.ITEM_GROUPS) {
            try {
                if (g.getType() == CreativeModeTab.Type.CATEGORY && ((CreativeModeTabExtra) g).polymer$isSyncable()) {
                    list.add(g);
                }
            } catch (Throwable e) {
                PolymerImpl.LOGGER.warn("Something broke!", e);
            }
        }

        var sync = new CreativeModeTabListBuilder() {
            @Override
            public void add(CreativeModeTab group) {
                list.add(group);
            }

            @Override
            public void remove(CreativeModeTab group) {
                list.remove(group);
            }
        };

        PolymerCreativeModeTabUtils.LIST_EVENT.invoker().onCreativeModeTabGet(player, sync);

        return new ArrayList<>(list);
    }

    public static boolean isPolymerCreativeModeTab(CreativeModeTab group) {
        return InternalServerRegistry.ITEM_GROUPS.containsEntry(group);
    }

    public static CreativeModeTab.Builder builder() {
        return new CreativeModeTab.Builder(CreativeModeTab.Row.BOTTOM, -1);
    }

    public static void registerPolymerCreativeModeTab(Identifier identifier, CreativeModeTab group) {
        if (BuiltInRegistries.CREATIVE_MODE_TAB.containsKey(identifier)) {
            PolymerImpl.LOGGER.warn("Creative Mode Tab '{}' is already registered in vanilla registry!", identifier);
        } else if (contains(identifier)) {
            PolymerImpl.LOGGER.warn("Creative Mode Tab  '{}' is already registered under the same id!", identifier);
        } else if (isPolymerCreativeModeTab(group)) {
            PolymerImpl.LOGGER.warn("Creative Mode Tab  '{}' is already registered as '{}'! ", identifier, REGISTRY.getId(group));
        } else {
            InternalServerRegistry.ITEM_GROUPS.set(identifier, group);
        }
    }

    public static Boolean contains(Identifier identifier) {
        return InternalServerRegistry.ITEM_GROUPS.contains(identifier);
    }

    public static void registerPolymerCreativeModeTab(ResourceKey<CreativeModeTab> identifier, CreativeModeTab group) {
        registerPolymerCreativeModeTab(identifier.identifier(), group);
    }

    public static Identifier getId(CreativeModeTab group) {
        var x = REGISTRY.getEntryId(group);

        if (x == null) {
            return BuiltInRegistries.CREATIVE_MODE_TAB.getKey(group);
        }
        return x;
    }

    public static ResourceKey<CreativeModeTab> getKey(CreativeModeTab group) {
        var x = REGISTRY.getEntryId(group);

        if (x == null) {
            return BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(group).orElseThrow();
        }
        return ResourceKey.create(Registries.CREATIVE_MODE_TAB, x);
    }

    public static void invalidateCache() {
        CONTENT_CACHE.clear();

        // Vanilla bugfix, causes crash on client/singleplayer
        for (var x : REGISTRY) {
            ((CreativeModeTabAccessor) x).setIconItemStack(null);
        }
    }

    @FunctionalInterface
    public interface CreativeModeTabEventListener {
        void onCreativeModeTabGet(ServerPlayer player, CreativeModeTabListBuilder builder);
    }

    public interface CreativeModeTabListBuilder {
        void add(CreativeModeTab group);

        void remove(CreativeModeTab group);
    }

    public record Contents(Collection<ItemStack> main, Collection<ItemStack> search) {
    }

    private record CreativeModeTabKey(Identifier identifier, boolean operator) {
    }
}
