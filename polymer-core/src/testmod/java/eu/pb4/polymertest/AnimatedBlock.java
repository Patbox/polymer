package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4x3f;

public class AnimatedBlock extends Block implements PolymerBlock, BlockWithElementHolder {
    public AnimatedBlock(Settings settings) {
        super(settings);
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return Blocks.BARRIER;
    }

    @Override
    public boolean tickElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return true;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return new CustomHolder();
    }

    public static class CustomHolder extends ElementHolder {
        private ItemDisplayElement centralElement;
        private ItemDisplayElement planetElement;
        private ItemDisplayElement moonElement;
        private int tick = 0;

        public CustomHolder() {
            this.planetElement = this.addElement(new ItemDisplayElement(Items.LIGHT_BLUE_WOOL));
            this.moonElement = this.addElement(new ItemDisplayElement(Items.DECORATED_POT));
            this.centralElement = this.addElement(new ItemDisplayElement(TestMod.TATER_BLOCK_ITEM));
            this.centralElement.setModelTransformation(ModelTransformationMode.FIXED);
            this.moonElement.setModelTransformation(ModelTransformationMode.FIXED);
            this.planetElement.setModelTransformation(ModelTransformationMode.FIXED);
            this.centralElement.setInterpolationDuration(3);
            this.moonElement.setInterpolationDuration(3);
            this.planetElement.setInterpolationDuration(3);

            this.centralElement.setGlowing(true);
            this.centralElement.setGlowColorOverride(0xfdffba);
            this.animate();
        }

        @Override
        protected void onTick() {
            this.animate();
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
