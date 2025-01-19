package eu.pb4.polymertest;

import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public record JukeboxHolderCreator() implements BlockWithElementHolder {

    @Override
    public boolean tickElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return true;
    }

    @Override
    public Vec3d getElementHolderOffset(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return new Vec3d(0, 8.5 / 16f, 0);
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return new Model();
    }

    static final class Model extends ElementHolder {
        private final ItemDisplayElement disk = new ItemDisplayElement();
        private float rotation = 0;


        public Model() {
            disk.setPitch(90);
            disk.setScale(new Vector3f(1, 1.6f, 1));
            disk.setTeleportDuration(2);
            this.addElement(disk);
        }

        @Override
        protected void onTick() {
            var attachment = BlockAwareAttachment.get(this);
            if (attachment == null) {
                return;
            }

            if (attachment.getWorld().getBlockEntity(attachment.getBlockPos()) instanceof JukeboxBlockEntity be) {
                this.disk.setItem(be.getStack().isEmpty() ? ItemStack.EMPTY : be.getStack());
                if (be.getManager().isPlaying()) {
                    rotation += 1.5f;
                    this.disk.setYaw(rotation);
                }
            }
        }
    }
}
