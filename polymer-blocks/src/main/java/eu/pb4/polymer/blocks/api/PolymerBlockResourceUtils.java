package eu.pb4.polymer.blocks.api;

import eu.pb4.polymer.blocks.impl.BlockExtBlockMapper;
import eu.pb4.polymer.blocks.impl.PolymerBlocksInternal;
import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.core.api.block.BlockMapper;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class PolymerBlockResourceUtils {
    private PolymerBlockResourceUtils() {}

    final static BlockResourceCreator CREATOR = new BlockResourceCreator(PolymerResourcePackUtils.getInstance(), BlockExtBlockMapper.INSTANCE, () -> {
        BlockMapper.DEFAULT_MAPPER_EVENT.register((player, mapper) -> BlockExtBlockMapper.INSTANCE);
    });

    @Nullable
    public static synchronized BlockState requestBlock(BlockModelType type, PolymerBlockModel model) {
        if (!CREATOR.hasRequestedEmpty(type) && CREATOR.getBlocksLeft(type) <= 1) {
            return null;
        }
        return CREATOR.requestBlock(type, model);
    }

    public static synchronized BlockState requestEmpty(BlockModelType type) {
        var empty = CREATOR.requestEmpty(type);
        if (empty == null) {
            throw new IllegalStateException("Empty state should never be null! BlockModelType: " + type);
        }
        return empty;
    }

    @Nullable
    public static synchronized BlockState requestBlock(BlockModelType type, PolymerBlockModel... model) {
        if (!CREATOR.hasRequestedEmpty(type) && CREATOR.getBlocksLeft(type) <= 1) {
            return null;
        }
        return CREATOR.requestBlock(type, model);
    }

    @Nullable
    public static synchronized BlockState requestBlock(BlockModelType type, MultiPolymerBlockModel model) {
        if (!CREATOR.hasRequestedEmpty(type) && CREATOR.getBlocksLeft(type) <= 1) {
            return null;
        }
        return CREATOR.requestBlock(type, model);
    }

    public static synchronized int getBlocksLeft(BlockModelType type) {
        if (CREATOR.hasRequestedEmpty(type)) {
            return CREATOR.getBlocksLeft(type);
        }

        return Math.max(CREATOR.getBlocksLeft(type) - 1, 0);
    }

    static {
        if (CompatStatus.POLYMC) {
            PolymerBlocksInternal.modelMap = CREATOR.models;
        }
    }
}
