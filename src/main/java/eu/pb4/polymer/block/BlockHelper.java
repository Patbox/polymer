package eu.pb4.polymer.block;

import eu.pb4.polymer.mixin.block.AbstractBlockAccessor;
import eu.pb4.polymer.mixin.block.AbstractBlockSettingAccessor;
import eu.pb4.polymer.other.MineEvent;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

import java.util.function.ToIntFunction;

public class BlockHelper {
    public static final MineEvent SERVER_SIDE_MINING_CHECK = new MineEvent();
    private static final Object2BooleanArrayMap<Block> IS_LIGHT_SOURCE_CACHE = new Object2BooleanArrayMap();

    public static boolean isLightSource(Block block) {
        if (BlockHelper.IS_LIGHT_SOURCE_CACHE.containsKey(block)) {
            return BlockHelper.IS_LIGHT_SOURCE_CACHE.getBoolean(block);
        } else {
            ToIntFunction<BlockState> luminance = ((AbstractBlockSettingAccessor) ((AbstractBlockAccessor) block).polymer_getSettings()).polymer_getLuminance();

            for (BlockState state : block.getStateManager().getStates()) {
                if (luminance.applyAsInt(state) != 0) {
                    BlockHelper.IS_LIGHT_SOURCE_CACHE.put(block, true);
                    return true;
                }
            }

            BlockHelper.IS_LIGHT_SOURCE_CACHE.put(block, false);
            return false;
        }
    }
}
