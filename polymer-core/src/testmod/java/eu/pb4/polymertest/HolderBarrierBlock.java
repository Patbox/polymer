package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class HolderBarrierBlock extends Block implements PolymerBlock, BlockWithElementHolder {
    private final Block block;

    public HolderBarrierBlock(Properties settings, Block block) {
        super(settings);
        this.block = block;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, @Nullable PacketContext context) {
        return Blocks.BARRIER.defaultBlockState();
    }

    @Override
    public boolean forceLightInsideBlock(BlockState blockState) {
        return true;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new CustomHolder(block);
    }

    public static class CustomHolder extends ElementHolder {

        public CustomHolder(Block block) {
            var element = new ItemDisplayElement(block.asItem());
            element.setItemDisplayContext(ItemDisplayContext.NONE);
            element.setInvisible(true);
            this.addElement(element);
        }

    }
}
