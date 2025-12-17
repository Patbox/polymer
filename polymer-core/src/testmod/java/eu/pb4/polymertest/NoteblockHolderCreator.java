package eu.pb4.polymertest;

import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public record NoteblockHolderCreator() implements BlockWithElementHolder {
    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState);
    }

    static final class Model extends ElementHolder {
        private final TextDisplayElement[] text = new TextDisplayElement[4];

        public Model(BlockState state) {
            var string = Component.literal("" + state.getValue(NoteBlock.NOTE));
            for (int i = 0; i < 4; i++) {
                var text = new TextDisplayElement(string);
                var dir = Direction.fromYRot(i * 90);
                text.setOffset(dir.getUnitVec3().scale(8.5 / 16f));
                text.setYaw(dir.toYRot());
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
                var string = Component.literal("" + att.getBlockState().getValue(NoteBlock.NOTE));
                for (int i = 0; i < 4; i++) {
                    this.text[i].setText(string);
                }
                this.tick();
            }
        }
    }
}
