package eu.pb4.polymer.block;

import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.other.DoubleBooleanEvent;
import eu.pb4.polymer.other.MineEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.function.Predicate;

/**
 * Use {@link PolymerBlockUtils} instead
 */
@Deprecated
public final class BlockHelper {
    public static final int NESTED_DEFAULT_DISTANCE = 32;

    public static final Predicate<BlockState> IS_VIRTUAL_BLOCK_STATE_PREDICATE = PolymerBlockUtils.IS_POLYMER_BLOCK_STATE_PREDICATE;

    public static final MineEvent SERVER_SIDE_MINING_CHECK = new MineEvent();

    public static final DoubleBooleanEvent<ServerWorld, ChunkSectionPos> SEND_LIGHT_UPDATE_PACKET = new DoubleBooleanEvent<>();

    static {
        PolymerBlockUtils.SEND_LIGHT_UPDATE_PACKET.register((obj, obj2) -> SEND_LIGHT_UPDATE_PACKET.invoke(obj, obj2));
        PolymerBlockUtils.SERVER_SIDE_MINING_CHECK.register((obj, obj2, obj3) -> SERVER_SIDE_MINING_CHECK.invoke(obj, obj2, obj3));
    }

    public static void registerVirtualBlockEntity(BlockEntityType<?>... types) {
        PolymerBlockUtils.registerBlockEntity(types);
    }

    public static boolean isVirtualBlockEntity(BlockEntityType<?> type) {
        return PolymerBlockUtils.isRegisteredBlockEntity(type);
    }

    public static boolean isVirtualLightSource(BlockState blockState) {
        return PolymerBlockUtils.isLightSource(blockState);
    }

    public static BlockState getBlockStateSafely(VirtualBlock block, BlockState blockState, int maxDistance) {
        BlockState out = block.getVirtualBlockState(blockState);

        int req = 0;
        while (out.getBlock() instanceof VirtualBlock newBlock && newBlock != block && req < maxDistance) {
            out = newBlock.getVirtualBlockState(out);
            req++;
        }
        return out;
    }

    public static BlockState getBlockStateSafely(VirtualBlock block, BlockState blockState) {
        return getBlockStateSafely(block, blockState, NESTED_DEFAULT_DISTANCE);
    }

    public static Block getBlockSafely(VirtualBlock block, World world, BlockPos pos, int maxDistance) {
        int req = 0;
        Block out = block.getVirtualBlock(pos, world);

        while (out instanceof VirtualBlock newBlock && newBlock != block && req < maxDistance) {
            out = newBlock.getVirtualBlock(pos, world);
            req++;
        }
        return out;
    }

    public static Block getBlockSafely(VirtualBlock block, World world, BlockPos pos) {
        return getBlockSafely(block, world, pos, NESTED_DEFAULT_DISTANCE);
    }

    @Deprecated
    public static void registerVirtualBlockEntity(Identifier identifier) {
        PolymerBlockUtils.registerBlockEntity(Registry.BLOCK_ENTITY_TYPE.get(identifier));
    }

    @Deprecated
    public static boolean isVirtualBlockEntity(Identifier identifier) {
        return PolymerBlockUtils.isRegisteredBlockEntity(Registry.BLOCK_ENTITY_TYPE.get(identifier));
    }

    @Deprecated
    public static boolean isVirtualBlockEntity(String identifier) {
        return isVirtualBlockEntity(Identifier.tryParse(identifier));
    }
}
