package eu.pb4.polymer.impl.client;

import com.google.common.base.Predicates;
import eu.pb4.polymer.api.client.registry.ClientPolymerBlock;
import eu.pb4.polymer.api.client.registry.ClientPolymerItem;
import eu.pb4.polymer.impl.PolymerMod;
import eu.pb4.polymer.impl.client.world.ClientBlockStorageInterface;
import eu.pb4.polymer.impl.interfaces.ClientItemGroupExtension;
import eu.pb4.polymer.impl.interfaces.NetworkIdList;
import eu.pb4.polymer.impl.other.ImplPolymerRegistry;
import eu.pb4.polymer.mixin.client.CreativeInventoryScreenAccessor;
import eu.pb4.polymer.mixin.other.ItemGroupAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.IdListPalette;
import net.minecraft.world.chunk.Palette;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.function.Predicate;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class InternalClientRegistry {
    public static final ImplPolymerRegistry<ClientPolymerBlock> BLOCKS = new ImplPolymerRegistry<>();
    public static final IdList<ClientPolymerBlock.State> BLOCK_STATES = new IdList<>();
    public static final Palette<ClientPolymerBlock.State> BLOCK_STATE_PALETTE = new IdListPalette<>(BLOCK_STATES, null);

    public static final ImplPolymerRegistry<ClientPolymerItem> ITEMS = new ImplPolymerRegistry<>();
    public static final ImplPolymerRegistry<ClientItemGroup> ITEM_GROUPS = new ImplPolymerRegistry<>();

    @Nullable
    public static ClientPolymerBlock.State getBlockAt(BlockPos pos) {
        var chunk = MinecraftClient.getInstance().world.getChunk(pos);

        if (chunk instanceof ClientBlockStorageInterface storage) {
            return storage.polymer_getClientPolymerBlock(pos.getX(), pos.getY(), pos.getZ());
        }

        return null;
    }

    public static void clear() {
        BLOCKS.clear();
        ITEMS.clear();
        ITEM_GROUPS.clear();
        ((NetworkIdList) BLOCK_STATES).polymer_clear();

        clearTabs(Predicates.alwaysTrue());
        for (var group : ItemGroup.GROUPS) {
            ((ClientItemGroupExtension) group).polymer_clearStacks();
        }
    }

    public static void clearTabs(Predicate<ClientItemGroup> removePredicate) {
        var array = ItemGroupAccessor.getGROUPS();

        var list = new ArrayList<ItemGroup>();

        int posOffset = 0;

        for (int i = 0; i < array.length; i++) {
            if (array[i] instanceof ClientItemGroup group && removePredicate.test(group)) {
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
                Field f1 = CreativeInventoryScreen.class.getDeclaredField("fabric_currentPage");
                f1.setAccessible(true);
                f1.set(null, 0);
            } catch (Exception e) {
                // noop
            }
        }

        ItemGroupAccessor.setGROUPS(list.toArray(new ItemGroup[0]));
    }
}
