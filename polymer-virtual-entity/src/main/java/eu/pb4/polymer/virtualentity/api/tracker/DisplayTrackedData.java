package eu.pb4.polymer.virtualentity.api.tracker;

import eu.pb4.polymer.virtualentity.mixin.accessors.BlockDisplayEntityAccessor;
import eu.pb4.polymer.virtualentity.mixin.accessors.DisplayEntityAccessor;
import eu.pb4.polymer.virtualentity.mixin.accessors.ItemDisplayEntityAccessor;
import eu.pb4.polymer.virtualentity.mixin.accessors.TextDisplayEntityAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class DisplayTrackedData {
    public final static TrackedData<Vector3f> TRANSLATION = DisplayEntityAccessor.getTRANSLATION();
    public final static TrackedData<Vector3f> SCALE = DisplayEntityAccessor.getSCALE();
    public final static TrackedData<Quaternionf> LEFT_ROTATION = DisplayEntityAccessor.getLEFT_ROTATION();
    public final static TrackedData<Quaternionf> RIGHT_ROTATION = DisplayEntityAccessor.getRIGHT_ROTATION();
    public final static TrackedData<Long> INTERPOLATION_START = DisplayEntityAccessor.getINTERPOLATION_START();
    public final static TrackedData<Integer> INTERPOLATION_DURATION = DisplayEntityAccessor.getINTERPOLATION_DURATION();
    public final static TrackedData<Integer> BRIGHTNESS = DisplayEntityAccessor.getBRIGHTNESS();
    public final static TrackedData<Float> VIEW_RANGE = DisplayEntityAccessor.getVIEW_RANGE();
    public final static TrackedData<Float> SHADOW_RADIUS = DisplayEntityAccessor.getSHADOW_RADIUS();
    public final static TrackedData<Float> SHADOW_STRENGTH = DisplayEntityAccessor.getSHADOW_STRENGTH();
    public final static TrackedData<Float> WIDTH = DisplayEntityAccessor.getWIDTH();
    public final static TrackedData<Float> HEIGHT = DisplayEntityAccessor.getHEIGHT();
    public final static TrackedData<Integer> GLOW_COLOR_OVERRIDE = DisplayEntityAccessor.getGLOW_COLOR_OVERRIDE();
    public final static TrackedData<Byte> BILLBOARD = DisplayEntityAccessor.getBILLBOARD();

    private DisplayTrackedData() {
    }

    public static final class Item {
        public final static TrackedData<ItemStack> ITEM = ItemDisplayEntityAccessor.getITEM();
        public final static TrackedData<Byte> ITEM_DISPLAY = ItemDisplayEntityAccessor.getITEM_DISPLAY();

        private Item() {
        }
    }

    public static final class Block {
        public final static TrackedData<BlockState> BLOCK_STATE = BlockDisplayEntityAccessor.getBLOCK_STATE();

        private Block() {
        }
    }

    public static final class Text {
        public static final byte SHADOW_FLAG = TextDisplayEntityAccessor.getSHADOW_FLAG();
        public static final byte SEE_THROUGH_FLAG = TextDisplayEntityAccessor.getSEE_THROUGH_FLAG();
        public static final byte DEFAULT_BACKGROUND_FLAG = TextDisplayEntityAccessor.getDEFAULT_BACKGROUND_FLAG();
        public static final byte LEFT_ALIGNMENT_FLAG = TextDisplayEntityAccessor.getLEFT_ALIGNMENT_FLAG();
        public static final byte RIGHT_ALIGNMENT_FLAG = TextDisplayEntityAccessor.getRIGHT_ALIGNMENT_FLAG();

        public static final TrackedData<net.minecraft.text.Text> TEXT = TextDisplayEntityAccessor.getTEXT();
        public static final TrackedData<Integer> LINE_WIDTH = TextDisplayEntityAccessor.getLINE_WIDTH();
        public static final TrackedData<Integer> BACKGROUND = TextDisplayEntityAccessor.getBACKGROUND();
        public static final TrackedData<Byte> TEXT_OPACITY = TextDisplayEntityAccessor.getTEXT_OPACITY();
        public static final TrackedData<Byte> TEXT_DISPLAY_FLAGS = TextDisplayEntityAccessor.getTEXT_DISPLAY_FLAGS();

        private Text() {
        }
    }
}
