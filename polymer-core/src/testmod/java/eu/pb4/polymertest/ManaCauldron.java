package eu.pb4.polymertest;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.joml.Vector3f;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.ArrayList;
import java.util.Collection;


// Bug reported with
// https://github.com/Dev0Louis/Zauber/blob/54fd6f06a9f1c83fdf37641f587da2b27860b98b/src/main/java/dev/louis/zauber/block/ManaCauldron.java
public class ManaCauldron extends Block implements PolymerBlock, BlockWithElementHolder {
    public static final MapCodec<ManaCauldron> CODEC = simpleCodec(ManaCauldron::new);
    public static final IntegerProperty MANA_LEVEL = IntegerProperty.create("mana_level", 0, 2);

    protected ManaCauldron(Properties settings) {
        super(settings);
        registerDefaultState(defaultBlockState().setValue(MANA_LEVEL, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MANA_LEVEL);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        world.setBlockAndUpdate(pos, state.cycle(MANA_LEVEL));
        return InteractionResult.PASS;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, @org.jspecify.annotations.Nullable PacketContext context) {
        return Blocks.CAULDRON.defaultBlockState();
    }

    @Override
    public boolean tickElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return true;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new CustomHolder(initialBlockState);
    }

    public static class CustomHolder extends ElementHolder {
        private static final BlockState EMPTY_STATE = Blocks.AIR.defaultBlockState();
        private static final BlockState HALF_FILLED_STATE = Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState();
        private static final BlockState FULL_STATE = Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState();
        private static final BlockState MANA_BUBBLE_STATE = Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState();
        private final BlockDisplayElement manaFill;
        private final Collection<BlockDisplayElementWithVelocity> manaBubbles = new ArrayList<>();
        private final RandomSource random = RandomSource.create();
        private int age;

        public CustomHolder(BlockState initialBlockState) {
            this.manaFill = this.addElement(new BlockDisplayElement(this.getState(initialBlockState)));
            this.manaFill.setOffset(new Vec3(-0.375, 0, -0.375));
            this.manaFill.setScale(new Vector3f(0.75f, 0.2f * initialBlockState.getValue(MANA_LEVEL) + (float)Math.sin(age / 50f) * 0.05f, 0.75f));
            this.manaFill.setGlowing(false);
        }

        @Override
        public void onTick() {
             this.age++;
            this.manaFill.setOffset(new Vec3(-0.375, 0, -0.375));
            var attachment = this.getAttachment();
            if (attachment == null) throw new IllegalStateException("Attachment is null");
            var blockBoundAttachment = ((BlockBoundAttachment)attachment);
            var blockPos = blockBoundAttachment.getBlockPos();
            var manaLevel = blockBoundAttachment.getBlockState().getValue(MANA_LEVEL);
            var offset = (float)Math.sin(age / 50f) * 0.05f;
            this.manaFill.setScale(new Vector3f(0.75f, 0.2f * manaLevel + offset, 0.75f));
            this.manaFill.setBlockState(this.getState(this.getAttachment().getWorld(), blockPos));

            var world = attachment.getWorld();
            if (manaLevel > 0) {
                if (age % (4 - manaLevel) == 0) {
                    world.sendParticles(ParticleTypes.UNDERWATER, false, true, blockPos.getX() + 0.5, blockPos.getY() + 0.75 + offset, blockPos.getZ() + 0.5, 10, 0.15, 0.15, 0.15, 1);
                }
                if (manaBubbles.size() < manaLevel * 10) {
                    Vec3 velocity = new Vec3((random.nextFloat() - 0.5) * 0.2, 0.05f * manaLevel, (random.nextFloat() - 0.5) * 0.2);
                    var blockDisplayEntity = new BlockDisplayElementWithVelocity(MANA_BUBBLE_STATE, velocity);
                    blockDisplayEntity.setScale(new Vector3f(0.1f));
                    this.addElement(blockDisplayEntity);
                    manaBubbles.add(blockDisplayEntity);
                }
            }

            manaBubbles.removeIf(element -> {
                var remove = element.getOffset().y() > 3 * manaLevel || random.nextFloat() > 0.93f;
                if (remove) {
                    var pos = this.getAttachment().getPos().add(element.getOffset());
                    if (random.nextFloat() > 0.5f) {

                    }
                    world.sendParticles(
                            ParticleTypes.BUBBLE_POP,
                            false, false,
                            pos.x,
                            pos.y,
                            pos.z,
                            1,
                            0,
                            0,
                            0,
                            0
                    );
                    this.removeElement(element);
                }
                return remove;
            });

            //element.setGlowing(true);
            //element.setOffset(element.getOffset().add(element.getVelocity()));
            manaBubbles.forEach(BlockDisplayElementWithVelocity::tick);

        }

        public BlockState getState(ServerLevel world, BlockPos pos) {
            return this.getState(world.getBlockState(pos));
        }

        public BlockState getState(BlockState state) {
            var manaLevel = state.getValue(MANA_LEVEL);
            return switch (manaLevel) {
                case 0 -> EMPTY_STATE;
                case 1 -> HALF_FILLED_STATE;
                case 2 -> FULL_STATE;
                default -> throw new IllegalStateException("Unexpected value: " + manaLevel);
            };
        }
    }

    public static class BlockDisplayElementWithVelocity extends BlockDisplayElement {
        private Vec3 velocity;

        public BlockDisplayElementWithVelocity(BlockState state, Vec3 velocity) {
            super(state);
            this.velocity = velocity;
        }


        @Override
        public void tick() {
            velocity = velocity.multiply(0.9, 1, 0.9);
            this.setOffset(this.getOffset().add(velocity));
            super.tick();
        }

        public Vec3 getVelocity() {
            return velocity;
        }
    }
}