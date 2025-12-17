package eu.pb4.polymer.virtualentity.mixin.accessors;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Display;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Display.BlockDisplay.class)
public interface BlockDisplayAccessor {
    @Accessor
    static EntityDataAccessor<BlockState> getDATA_BLOCK_STATE_ID() {
        throw new UnsupportedOperationException();
    }
}
