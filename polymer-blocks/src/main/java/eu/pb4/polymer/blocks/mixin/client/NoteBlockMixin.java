package eu.pb4.polymer.blocks.mixin.client;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.NoteBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(NoteBlock.class)
public class NoteBlockMixin {
    @Inject(method = "onUse", at = @At("RETURN"), cancellable = true)
    public void polymer_preventArmSwing(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (PolymerBlockUtils.IS_POLYMER_BLOCK_STATE_PREDICATE.test(state))
            cir.setReturnValue(ActionResult.PASS);
    }
}
