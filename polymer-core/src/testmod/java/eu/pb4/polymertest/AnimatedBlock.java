package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.block.PolymerBlock;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4x3f;

public class AnimatedBlock extends BlockWithEntity implements PolymerBlock {
    public AnimatedBlock(Settings settings) {
        super(settings);
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return Blocks.BARRIER;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, TestMod.ANIMATED_BE, this::tick);
    }

    private <E extends BlockEntity> void tick(World world, BlockPos pos, BlockState state, E e) {
        var self = (BEntity) e;

        if (self.holder == null) {
            self.setup();
        }

        self.animate();
    }


    public static class BEntity extends BlockEntity {
        public HolderAttachment holderAttachment;
        public ElementHolder holder;
        private ItemDisplayElement centralElement;
        private ItemDisplayElement planetElement;
        private ItemDisplayElement moonElement;
        private int tick = 0;

        public BEntity(BlockPos pos, BlockState state) {
            super(TestMod.ANIMATED_BE, pos, state);
        }

        public void setup() {
            this.holder = new ElementHolder();
            this.planetElement = this.holder.addElement(new ItemDisplayElement(Items.LIGHT_BLUE_WOOL));
            this.moonElement = this.holder.addElement(new ItemDisplayElement(Items.DECORATED_POT));
            this.centralElement = this.holder.addElement(new ItemDisplayElement(TestMod.TATER_BLOCK_ITEM));
            this.centralElement.setModelTransformation(ModelTransformationMode.FIXED);
            this.moonElement.setModelTransformation(ModelTransformationMode.FIXED);
            this.planetElement.setModelTransformation(ModelTransformationMode.FIXED);
            this.centralElement.setInterpolationDuration(3);
            this.moonElement.setInterpolationDuration(3);
            this.planetElement.setInterpolationDuration(3);

            this.centralElement.setGlowing(true);
            this.centralElement.setGlowColorOverride(0xfdffba);
            this.animate();


            this.holderAttachment = new ChunkAttachment(this.holder, (WorldChunk) this.world.getChunk(this.pos), Vec3d.ofCenter(this.pos), false);
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
                this.holder.tick();
            }
            this.tick++;
        }

        @Override
        public void markRemoved() {
            super.markRemoved();
            if (this.holder != null) {
                holder.destroy();
                holder = null;
            }
        }
    }
}
