package eu.pb4.polymer.virtualentity.mixin.block;

import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.impl.BlockExt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BlockMixin implements BlockExt {
    @Unique
    private BlockWithElementHolder blockWithElementHolder;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void setupInitialCreator(BlockBehaviour.Properties settings, CallbackInfo ci) {
        this.blockWithElementHolder = this instanceof BlockWithElementHolder holder ? holder : null;
    }

    @Override
    public boolean polymerVE$setElementHolderCreator(BlockWithElementHolder holder) {
        this.blockWithElementHolder = holder;
        return true;
    }
    @Override
    @Nullable
    public BlockWithElementHolder polymerVE$getElementHolderCreator() {
        return this.blockWithElementHolder;
    }
}
