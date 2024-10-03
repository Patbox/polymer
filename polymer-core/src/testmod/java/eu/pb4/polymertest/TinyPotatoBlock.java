package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.block.PolymerHeadBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.packettweaker.PacketContext;

public class TinyPotatoBlock extends Block implements PolymerHeadBlock {
    public TinyPotatoBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return Blocks.PLAYER_HEAD.getDefaultState();
    }

    @Override
    public String getPolymerSkinValue(BlockState state, BlockPos pos, PacketContext context) {
        return "ewogICJ0aW1lc3RhbXAiIDogMTYxNzk3NjcxOTAzNSwKICAicHJvZmlsZUlkIiA6ICJlZDUzZGQ4MTRmOWQ0YTNjYjRlYjY1MWRjYmE3N2U2NiIsCiAgInByb2ZpbGVOYW1lIiA6ICI0MTQxNDE0MWgiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTczNTE0YTIzMjQ1ZjE1ZGJhZDVmYjRlNjIyMTYzMDIwODY0Y2NlNGMxNWQ1NmRlM2FkYjkwZmE1YTcxMzdmZCIKICAgIH0KICB9Cn0";
    }
}
