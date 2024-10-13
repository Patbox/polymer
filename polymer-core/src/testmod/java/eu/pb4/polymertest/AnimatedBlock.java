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
import net.minecraft.block.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.Items;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.source.BiomeCoords;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4x3f;
import xyz.nucleoid.packettweaker.PacketContext;

public class AnimatedBlock extends FallingBlock implements PolymerBlock, BlockWithElementHolder {
    public static final BooleanProperty CAN_FALL = BooleanProperty.of("can_fall");
    public static final EnumProperty<Direction> FACING = Properties.FACING;

    public AnimatedBlock(Settings settings) {
        super(settings);
        setDefaultState(this.getDefaultState().with(CAN_FALL, false));
    }


    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(CAN_FALL, FACING);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return super.getPlacementState(ctx).with(FACING, ctx.getSide());
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(CAN_FALL)) {
            super.scheduledTick(state, world, pos, random);
        }
    }

    @Override
    protected MapCodec<? extends FallingBlock> getCodec() {
        return null;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return Blocks.BARRIER.getDefaultState();
    }

    @Override
    public boolean tickElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return true;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return new CustomHolder(world, initialBlockState);
    }

    public static class CustomHolder extends ElementHolder {
        private final EntityElement<SheepEntity> entity;
        private final ItemDisplayElement centralElement;
        private final ItemDisplayElement planetElement;
        private final ItemDisplayElement moonElement;
        private int tick = 0;

        public CustomHolder(ServerWorld world, BlockState state) {
            this.planetElement = this.addElement(new ItemDisplayElement(Items.LIGHT_BLUE_WOOL));
            this.moonElement = this.addElement(new ItemDisplayElement(Items.DECORATED_POT));
            this.centralElement = this.addElement(new ItemDisplayElement(TestMod.TATER_BLOCK_ITEM));
            this.centralElement.setModelTransformation(ModelTransformationMode.FIXED);
            this.moonElement.setModelTransformation(ModelTransformationMode.FIXED);
            this.planetElement.setModelTransformation(ModelTransformationMode.FIXED);
            this.centralElement.setInterpolationDuration(3);
            this.moonElement.setInterpolationDuration(3);
            this.planetElement.setInterpolationDuration(3);
            this.centralElement.setTeleportDuration(1);
            this.moonElement.setTeleportDuration(1);
            this.planetElement.setTeleportDuration(1);
            this.entity = this.addElement(new EntityElement<>(EntityType.SHEEP, world));
            this.updateRotation(state);
            entity.entity().setColor(DyeColor.PINK);
            entity.setOffset(new Vec3d(0, 1, 0));
            this.centralElement.setGlowing(true);
            this.centralElement.setGlowColorOverride(0x000000);
            this.animate();
        }

        private void updateRotation(BlockState state) {
            var yaw = 0f;
            var pitch = 0f;
            var dir = state.get(FACING);

            if (dir.getAxis() == Direction.Axis.Y) {
                pitch = dir == Direction.DOWN ? 180 : 0;
            } else {
                pitch = 90;
                yaw = dir.asRotation();
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
                var pos = BlockPos.ofFloored(attachment.getPos());
                this.centralElement.setGlowColorOverride(chunkAttachment.getChunk().getBiomeForNoiseGen(
                        BiomeCoords.fromBlock(pos.getX()),
                        BiomeCoords.fromBlock(pos.getY()),
                        BiomeCoords.fromBlock(pos.getZ())
                ).value().getWaterColor());
            }
        }

        public void animate() {
            if (this.tick % 3 == 0) {
                this.centralElement.setTransformation(new Matrix4x3f().rotateY(this.tick / 200f).rotateX(MathHelper.PI / 16).scale(2.2f));
                var planet = new Matrix4x3f().rotateX(-MathHelper.PI / 16).rotateY(this.tick / 40f).translate(3.6f, 0, 0).rotateY(this.tick / 30f).rotateX(MathHelper.PI / 12).rotateZ(MathHelper.PI / 12).scale(1f);
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
