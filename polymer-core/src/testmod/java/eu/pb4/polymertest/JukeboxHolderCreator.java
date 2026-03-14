package eu.pb4.polymertest;

import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.joml.Vector3f;

public record JukeboxHolderCreator() implements BlockWithElementHolder {

    @Override
    public boolean tickElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return true;
    }

    @Override
    public Vec3 getElementHolderOffset(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Vec3(0, 8.5 / 16f, 0);
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
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
                this.disk.setItem(be.getTheItem().isEmpty() ? ItemStack.EMPTY : be.getTheItem());
                if (be.getSongPlayer().isPlaying()) {
                    rotation += 1.5f;
                    this.disk.setYaw(rotation);
                }
            }
        }
    }
}
