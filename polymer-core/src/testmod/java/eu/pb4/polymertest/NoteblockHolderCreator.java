package eu.pb4.polymertest;

import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import net.minecraft.block.BlockState;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public record NoteblockHolderCreator() implements BlockWithElementHolder {
    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState);
    }

    static final class Model extends ElementHolder {
        private final TextDisplayElement[] text = new TextDisplayElement[4];

        public Model(BlockState state) {
            var string = Text.literal("" + state.get(NoteBlock.NOTE));
            for (int i = 0; i < 4; i++) {
                var text = new TextDisplayElement(string);
                var dir = Direction.fromHorizontalDegrees(i * 90);
                text.setOffset(dir.getDoubleVector().multiply(8.5 / 16f));
                text.setYaw(dir.getPositiveHorizontalDegrees());
                this.text[i] = text;
                this.addElement(text);
            }
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            super.notifyUpdate(updateType);
            if (updateType == BlockAwareAttachment.BLOCK_STATE_UPDATE) {
                var att = BlockAwareAttachment.get(this);
                assert att != null;
                var string = Text.literal("" + att.getBlockState().get(NoteBlock.NOTE));
                for (int i = 0; i < 4; i++) {
                    this.text[i].setText(string);
                }
                this.tick();
            }
        }
    }
}
