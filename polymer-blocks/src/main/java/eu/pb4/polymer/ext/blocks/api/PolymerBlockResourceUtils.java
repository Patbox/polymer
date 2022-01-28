package eu.pb4.polymer.ext.blocks.api;

import eu.pb4.polymer.ext.blocks.impl.BlockResourceImpl;
import net.minecraft.block.BlockState;
import org.jetbrains.annotations.Nullable;

public final class PolymerBlockResourceUtils {
    private PolymerBlockResourceUtils() {}

    @Nullable
    public static BlockState requestBlock(Type type, PolymerBlockModel model) {
        return BlockResourceImpl.requestBlock(type, model);
    }

    public static int getBlocksLeft(Type type) {
        return BlockResourceImpl.getBlocksLeft(type);
    }

    public enum Type {
        FULL_BLOCK,
        TRANSPARENT_BLOCK
    }
}
