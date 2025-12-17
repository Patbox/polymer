package eu.pb4.polymer.virtualentity.api.tracker;

import eu.pb4.polymer.virtualentity.mixin.accessors.BlockDisplayAccessor;
import eu.pb4.polymer.virtualentity.mixin.accessors.DisplayAccessor;
import eu.pb4.polymer.virtualentity.mixin.accessors.ItemDisplayAccessor;
import eu.pb4.polymer.virtualentity.mixin.accessors.TextDisplayAccessor;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

public final class DisplayTrackedData {
    public final static EntityDataAccessor<Vector3fc> TRANSLATION = DisplayAccessor.getDATA_TRANSLATION_ID();
    public final static EntityDataAccessor<Vector3fc> SCALE = DisplayAccessor.getDATA_SCALE_ID();
    public final static EntityDataAccessor<Quaternionfc> LEFT_ROTATION = DisplayAccessor.getDATA_LEFT_ROTATION_ID();
    public final static EntityDataAccessor<Quaternionfc> RIGHT_ROTATION = DisplayAccessor.getDATA_RIGHT_ROTATION_ID();
    public final static EntityDataAccessor<Integer> INTERPOLATION_DURATION = DisplayAccessor.getDATA_TRANSFORMATION_INTERPOLATION_DURATION_ID();
    public final static EntityDataAccessor<Integer> TELEPORTATION_DURATION = DisplayAccessor.getDATA_POS_ROT_INTERPOLATION_DURATION_ID();
    public final static EntityDataAccessor<Integer> START_INTERPOLATION = DisplayAccessor.getDATA_TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS_ID();
    public final static EntityDataAccessor<Integer> BRIGHTNESS = DisplayAccessor.getDATA_BRIGHTNESS_OVERRIDE_ID();
    public final static EntityDataAccessor<Float> VIEW_RANGE = DisplayAccessor.getDATA_VIEW_RANGE_ID();
    public final static EntityDataAccessor<Float> SHADOW_RADIUS = DisplayAccessor.getDATA_SHADOW_RADIUS_ID();
    public final static EntityDataAccessor<Float> SHADOW_STRENGTH = DisplayAccessor.getDATA_SHADOW_STRENGTH_ID();
    public final static EntityDataAccessor<Float> WIDTH = DisplayAccessor.getDATA_WIDTH_ID();
    public final static EntityDataAccessor<Float> HEIGHT = DisplayAccessor.getDATA_HEIGHT_ID();
    public final static EntityDataAccessor<Integer> GLOW_COLOR_OVERRIDE = DisplayAccessor.getDATA_GLOW_COLOR_OVERRIDE_ID();
    public final static EntityDataAccessor<Byte> BILLBOARD = DisplayAccessor.getDATA_BILLBOARD_RENDER_CONSTRAINTS_ID();

    private DisplayTrackedData() {
    }

    public static final class Item {
        public final static EntityDataAccessor<ItemStack> ITEM = ItemDisplayAccessor.getDATA_ITEM_STACK_ID();
        public final static EntityDataAccessor<Byte> ITEM_DISPLAY = ItemDisplayAccessor.getDATA_ITEM_DISPLAY_ID();

        private Item() {
        }
    }

    public static final class Block {
        public final static EntityDataAccessor<BlockState> BLOCK_STATE = BlockDisplayAccessor.getDATA_BLOCK_STATE_ID();

        private Block() {
        }
    }

    public static final class Text {
        public static final byte SHADOW_FLAG = TextDisplayAccessor.getFLAG_SHADOW();
        public static final byte SEE_THROUGH_FLAG = TextDisplayAccessor.getFLAG_SEE_THROUGH();
        public static final byte DEFAULT_BACKGROUND_FLAG = TextDisplayAccessor.getFLAG_USE_DEFAULT_BACKGROUND();
        public static final byte LEFT_ALIGNMENT_FLAG = TextDisplayAccessor.getFLAG_ALIGN_LEFT();
        public static final byte RIGHT_ALIGNMENT_FLAG = TextDisplayAccessor.getFLAG_ALIGN_RIGHT();

        public static final EntityDataAccessor<net.minecraft.network.chat.Component> TEXT = TextDisplayAccessor.getDATA_TEXT_ID();
        public static final EntityDataAccessor<Integer> LINE_WIDTH = TextDisplayAccessor.getDATA_LINE_WIDTH_ID();
        public static final EntityDataAccessor<Integer> BACKGROUND = TextDisplayAccessor.getDATA_BACKGROUND_COLOR_ID();
        public static final EntityDataAccessor<Byte> TEXT_OPACITY = TextDisplayAccessor.getDATA_TEXT_OPACITY_ID();
        public static final EntityDataAccessor<Byte> TEXT_DISPLAY_FLAGS = TextDisplayAccessor.getDATA_STYLE_FLAGS_ID();

        private Text() {
        }
    }
}
