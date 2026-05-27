package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

public class FakeEndGatewayBlock extends Block implements PolymerBlock, EntityBlock {
    public FakeEndGatewayBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, @Nullable PacketContext context) {
        return Blocks.END_GATEWAY.defaultBlockState();
    }

    @Override
    public void onPolymerBlockSend(BlockState blockState, BlockPos.MutableBlockPos pos, ServerPlayer player) {
        var nbt = new CompoundTag();
        nbt.putString("id", "minecraft:end_gateway");
        nbt.putInt("x", pos.getX());
        nbt.putInt("y", pos.getY());
        nbt.putInt("z", pos.getZ());
        nbt.putLong("Age", Long.MIN_VALUE);
        player.connection.send(PolymerBlockUtils.createBlockEntityPacket(pos, BlockEntityTypes.END_GATEWAY, nbt));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FakeEndGatewayBlockEntity(pos, state);
    }
}
