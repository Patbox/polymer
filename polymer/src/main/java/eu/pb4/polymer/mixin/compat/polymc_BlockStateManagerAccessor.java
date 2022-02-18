package eu.pb4.polymer.mixin.compat;

import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.api.block.BlockStateManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

@Pseudo
@Mixin(BlockStateManager.class)
public interface polymc_BlockStateManagerAccessor {
    @Invoker
    BlockState callRequestBlockState(Block block, Predicate<BlockState> filter, BiConsumer<Block, PolyRegistry> onFirstRegister) throws BlockStateManager.StateLimitReachedException;
}
