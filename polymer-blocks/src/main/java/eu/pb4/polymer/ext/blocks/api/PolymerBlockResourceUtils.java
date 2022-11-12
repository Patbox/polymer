package eu.pb4.polymer.ext.blocks.api;

import eu.pb4.polymer.api.block.BlockMapper;
import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import eu.pb4.polymer.ext.blocks.impl.BlockExtBlockMapper;
import eu.pb4.polymer.ext.blocks.impl.PolymerBlocksInternal;
import eu.pb4.polymer.impl.compat.CompatStatus;
import net.minecraft.block.BlockState;
import org.jetbrains.annotations.Nullable;

public final class PolymerBlockResourceUtils {
    private PolymerBlockResourceUtils() {}

    final static BlockResourceCreator CREATOR = new BlockResourceCreator(PolymerRPUtils.getInstance(), BlockExtBlockMapper.INSTANCE, () -> {
        BlockMapper.DEFAULT_MAPPER_EVENT.register((player, mapper) -> BlockExtBlockMapper.INSTANCE);
    });

    @Nullable
    public static BlockState requestBlock(BlockModelType type, PolymerBlockModel model) {
        return CREATOR.requestBlock(type, model);
    }

    @Nullable
    public static BlockState requestBlock(BlockModelType type, PolymerBlockModel... model) {
        return CREATOR.requestBlock(type, model);
    }

    public static int getBlocksLeft(BlockModelType type) {
        return CREATOR.getBlocksLeft(type);
    }

    static {
        if (CompatStatus.POLYMC) {
            PolymerBlocksInternal.modelMap = CREATOR.models;
        }
    }
}
