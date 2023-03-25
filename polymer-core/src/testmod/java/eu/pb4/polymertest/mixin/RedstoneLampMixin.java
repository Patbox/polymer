package eu.pb4.polymertest.mixin;

import net.minecraft.block.RedstoneLampBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(RedstoneLampBlock.class)
public class RedstoneLampMixin {
    @ModifyConstant(method = "neighborUpdate", constant = @Constant(intValue = 4))
    private int fastLamps(int i) {
        return 1;
    }
}
