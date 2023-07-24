package eu.pb4.polymer.core.impl.other.world;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;

public class SimpleVirtualWorld extends VirtualWorld {
    public SimpleVirtualWorld(ServerWorld world) {
        super(world);
    }

    @Override
    protected BlockState getContextState(BlockState x) {
        return PolymerBlockUtils.getPolymerBlockState(x);
    }
}
