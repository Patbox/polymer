package eu.pb4.polymer.core.mixin.block;

import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonBaseBlock.class)
public abstract class PistonBaseBlockMixin {
    // Clients simulate piston moves, so note blocks next to moved blocks recompute their instrument and pass
    // the change along the column. Server state didn't change, so resend these columns to undo it.
    @Inject(method = "moveBlocks", at = @At("RETURN"))
    private void polymer$resendNoteBlockColumns(Level level, BlockPos pistonPos, Direction direction, boolean extending,
                                                CallbackInfoReturnable<Boolean> cir, @Local PistonStructureResolver resolver) {
        if (!cir.getReturnValueZ() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        var pushDirection = extending ? direction : direction.getOpposite();
        polymer$resendNoteBlocksAboveAndBelow(serverLevel, pistonPos.relative(direction));
        for (var pos : resolver.getToPush()) {
            polymer$resendNoteBlocksAboveAndBelow(serverLevel, pos);
            polymer$resendNoteBlocksAboveAndBelow(serverLevel, pos.relative(pushDirection));
        }
    }

    @Unique
    private static void polymer$resendNoteBlocksAboveAndBelow(ServerLevel level, BlockPos pos) {
        for (var direction : Direction.Plane.VERTICAL) {
            var cursor = pos.relative(direction).mutable();
            while (PolymerBlockUtils.getPolymerBlockState(level.getBlockState(cursor), null).is(Blocks.NOTE_BLOCK)) {
                level.getChunkSource().blockChanged(cursor);
                cursor.move(direction);
            }
        }
    }
}
