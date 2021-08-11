package eu.pb4.polymer.block;

import eu.pb4.polymer.other.DoubleBooleanEvent;
import eu.pb4.polymer.other.MineEvent;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkSectionPos;

public class BlockHelper {
    public static final MineEvent SERVER_SIDE_MINING_CHECK = new MineEvent();
    public static final DoubleBooleanEvent<ServerWorld, ChunkSectionPos> SEND_LIGHT_UPDATE_PACKET = new DoubleBooleanEvent<>();
    private static final Object2BooleanMap<BlockState> IS_LIGHT_SOURCE_CACHE = new Object2BooleanOpenHashMap<>();

    @Deprecated
    public static boolean isLightSource(Block block) {
        return isVirtualLightSource(block.getDefaultState());
    }

    public static boolean isVirtualLightSource(BlockState blockState) {
        if (blockState.getBlock() instanceof VirtualBlock virtualBlock) {
            if (BlockHelper.IS_LIGHT_SOURCE_CACHE.containsKey(blockState)) {
                return BlockHelper.IS_LIGHT_SOURCE_CACHE.getBoolean(blockState);
            } else {
                if (blockState.getLuminance() != virtualBlock.getVirtualBlockState(blockState).getLuminance()) {
                    BlockHelper.IS_LIGHT_SOURCE_CACHE.put(blockState, true);
                    return true;
                }

                BlockHelper.IS_LIGHT_SOURCE_CACHE.put(blockState, false);
                return false;
            }
        }
        return false;
    }
}
