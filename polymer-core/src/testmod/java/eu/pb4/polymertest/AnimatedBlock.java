package eu.pb4.polymertest;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.EntityElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.joml.Matrix4x3f;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

public class AnimatedBlock extends FallingBlock implements PolymerBlock, BlockWithElementHolder {
    public static final BooleanProperty CAN_FALL = BooleanProperty.create("can_fall");
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public AnimatedBlock(Properties settings) {
        super(settings);
        registerDefaultState(this.defaultBlockState().setValue(CAN_FALL, false));
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CAN_FALL, FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return super.getStateForPlacement(ctx).setValue(FACING, ctx.getClickedFace());
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (state.getValue(CAN_FALL)) {
            super.tick(state, world, pos, random);
        }
    }

    @Override
    public int getDustColor(BlockState state, BlockGetter world, BlockPos pos) {
        return 0;
    }

    @Override
    protected MapCodec<? extends FallingBlock> codec() {
        return null;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, @org.jspecify.annotations.Nullable PacketContext context) {
        return Blocks.BARRIER.defaultBlockState();
    }

    @Override
    public boolean tickElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return true;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new CustomHolder(world, initialBlockState);
    }

    public static class CustomHolder extends ElementHolder {
        private final EntityElement<Sheep> entity;
        private final ItemDisplayElement centralElement;
        private final ItemDisplayElement planetElement;
        private final ItemDisplayElement moonElement;
        private int tick = 0;

        public CustomHolder(ServerLevel world, BlockState state) {
            this.planetElement = this.addElement(new ItemDisplayElement(Items.WOOL.lightBlue()));
            this.moonElement = this.addElement(new ItemDisplayElement(Items.DECORATED_POT));
            this.centralElement = this.addElement(new ItemDisplayElement(TestMod.TATER_BLOCK_ITEM));
            this.centralElement.setItemDisplayContext(ItemDisplayContext.FIXED);
            this.moonElement.setItemDisplayContext(ItemDisplayContext.FIXED);
            this.planetElement.setItemDisplayContext(ItemDisplayContext.FIXED);
            this.centralElement.setInterpolationDuration(3);
            this.moonElement.setInterpolationDuration(3);
            this.planetElement.setInterpolationDuration(3);
            this.centralElement.setTeleportDuration(1);
            this.moonElement.setTeleportDuration(1);
            this.planetElement.setTeleportDuration(1);
            this.entity = this.addElement(new EntityElement<>(EntityTypes.SHEEP, world));
            this.updateRotation(state);
            entity.entity().setColor(DyeColor.PINK);
            entity.setOffset(new Vec3(0, 1, 0));
            this.centralElement.setGlowing(true);
            this.centralElement.setGlowColorOverride(0x000000);
            this.animate();
        }

        private void updateRotation(BlockState state) {
            var yaw = 0f;
            var pitch = 0f;
            var dir = state.getValue(FACING);

            if (dir.getAxis() == Direction.Axis.Y) {
                pitch = dir == Direction.DOWN ? 180 : 0;
            } else {
                pitch = 90;
                yaw = dir.toYRot();
            }

            this.planetElement.setRotation(pitch, yaw);
            this.moonElement.setRotation(pitch, yaw);
            this.centralElement.setRotation(pitch, yaw);
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            super.notifyUpdate(updateType);
            if (updateType == BlockAwareAttachment.BLOCK_STATE_UPDATE) {
                updateRotation(BlockAwareAttachment.get(this).getBlockState());
            }
        }

        @Override
        protected void onTick() {
            this.animate();
        }

        @Override
        protected void onAttachmentSet(HolderAttachment attachment, @Nullable HolderAttachment oldAttachment) {
            if (attachment instanceof ChunkAttachment chunkAttachment) {
                var pos = BlockPos.containing(attachment.getPos());
                this.centralElement.setGlowColorOverride(chunkAttachment.getChunk().getNoiseBiome(
                        QuartPos.fromBlock(pos.getX()),
                        QuartPos.fromBlock(pos.getY()),
                        QuartPos.fromBlock(pos.getZ())
                ).value().getWaterColor());
            }
        }

        public void animate() {
            if (this.tick % 3 == 0) {
                this.centralElement.setTransformation(new Matrix4x3f().rotateY(this.tick / 200f).rotateX(Mth.PI / 16).scale(2.2f));
                var planet = new Matrix4x3f().rotateX(-Mth.PI / 16).rotateY(this.tick / 40f).translate(3.6f, 0, 0).rotateY(this.tick / 30f).rotateX(Mth.PI / 12).rotateZ(Mth.PI / 12).scale(1f);
                this.planetElement.setTransformation(planet);
                this.moonElement.setTransformation(planet.rotateY(this.tick / 8f).translate(1.4f, 0, 0).scale(0.42f));

                this.centralElement.startInterpolation();
                this.planetElement.startInterpolation();
                this.moonElement.startInterpolation();
            }
            this.tick++;
        }
    }
}
