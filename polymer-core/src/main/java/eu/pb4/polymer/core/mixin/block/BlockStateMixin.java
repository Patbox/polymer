package eu.pb4.polymer.core.mixin.block;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.impl.interfaces.BlockStateExtra;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockState.class)
public abstract class BlockStateMixin implements BlockStateExtra {
    @Shadow protected abstract BlockState asBlockState();

    @Unique
    private boolean polymer$calculatedIsLight;
    @Unique
    private boolean polymer$isLight;

    @Override
    public boolean polymer$isPolymerLightSource() {
        if (this.polymer$calculatedIsLight) {
            return this.polymer$isLight;
        }

        if (this.asBlockState().getBlock() instanceof PolymerBlock polymerBlock) {
            this.polymer$isLight = this.asBlockState().getLuminance() != polymerBlock.getPolymerBlockState(this.asBlockState()).getLuminance();
        }


        this.polymer$calculatedIsLight = true;

        return false;
    }
}
