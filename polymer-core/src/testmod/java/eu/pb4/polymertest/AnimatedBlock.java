package eu.pb4.polymertest;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.BlockWithMovingElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.EntityElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4x3f;
import xyz.nucleoid.packettweaker.PacketContext;

public class AnimatedBlock extends FallingBlock implements PolymerBlock, BlockWithMovingElementHolder {
    public static final BooleanProperty CAN_FALL = BooleanProperty.of("can_fall");

    public AnimatedBlock(Settings settings) {
        super(settings);
    }


    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(CAN_FALL);
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
        return new CustomHolder(world);
    }

    public static class CustomHolder extends ElementHolder {
        private final EntityElement<SheepEntity> entity;
        private final ItemDisplayElement centralElement;
        private final ItemDisplayElement planetElement;
        private final ItemDisplayElement moonElement;
        private int tick = 0;

        public CustomHolder(ServerWorld world) {
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
            entity.entity().setColor(DyeColor.PINK);
            entity.setOffset(new Vec3d(0, 1, 0));
            this.centralElement.setGlowing(true);
            this.centralElement.setGlowColorOverride(0x000000);
            this.animate();
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
