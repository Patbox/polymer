package eu.pb4.polymer.virtualentity.api.elements;

import eu.pb4.polymer.virtualentity.api.data.DisplayEntityData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;

public class TextDisplayElement extends DisplayElement {
    public TextDisplayElement(Component text) {
        this.setText(text);
    }

    public TextDisplayElement() {
    }

    public Component getText() {
        return this.syncedData.get(DisplayEntityData.Text.TEXT);
    }

    public void setText(Component text) {
        this.syncedData.set(DisplayEntityData.Text.TEXT, text);
    }

    public int getLineWidth() {
        return this.syncedData.get(DisplayEntityData.Text.LINE_WIDTH);
    }

    public void setLineWidth(int lineWidth) {
        this.syncedData.set(DisplayEntityData.Text.LINE_WIDTH, lineWidth);
    }

    public byte getTextOpacity() {
        return this.syncedData.get(DisplayEntityData.Text.TEXT_OPACITY);
    }

    public void setTextOpacity(byte textOpacity) {
        this.syncedData.set(DisplayEntityData.Text.TEXT_OPACITY, textOpacity);
    }

    public int getBackground() {
        return this.syncedData.get(DisplayEntityData.Text.BACKGROUND);
    }

    public void setBackground(int background) {
        this.syncedData.set(DisplayEntityData.Text.BACKGROUND, background);
    }

    public byte getDisplayFlags() {
        return this.syncedData.get(DisplayEntityData.Text.TEXT_DISPLAY_FLAGS);
    }

    public void setDisplayFlags(byte flags) {
        this.syncedData.set(DisplayEntityData.Text.TEXT_DISPLAY_FLAGS, flags);
    }

    public boolean getDisplayFlag(byte flag) {
        return (getDisplayFlags() & flag) != 0;
    }

    public void setDisplayFlag(byte flag, boolean value) {
        this.syncedData.set(DisplayEntityData.Text.TEXT_DISPLAY_FLAGS, flag(getDisplayFlags(), flag, value));
    }

    public void setTextAlignment(Display.TextDisplay.Align alignment) {
        setDisplayFlags(switch (alignment) {
            case CENTER -> flag(getDisplayFlags(), DisplayEntityData.Text.LEFT_ALIGNMENT_FLAG | DisplayEntityData.Text.RIGHT_ALIGNMENT_FLAG, false);
            case LEFT -> flag(flag(getDisplayFlags(), DisplayEntityData.Text.LEFT_ALIGNMENT_FLAG, true), DisplayEntityData.Text.RIGHT_ALIGNMENT_FLAG, false);
            case RIGHT -> flag(flag(getDisplayFlags(), DisplayEntityData.Text.LEFT_ALIGNMENT_FLAG, false), DisplayEntityData.Text.RIGHT_ALIGNMENT_FLAG, true);
        });
    }

    public Display.TextDisplay.Align getTextAlignment() {
        return Display.TextDisplay.getAlign(this.getDisplayFlags());
    }

    public void setShadow(boolean value) {
        setDisplayFlag(DisplayEntityData.Text.SHADOW_FLAG, value);
    }

    public boolean getShadow() {
        return getDisplayFlag(DisplayEntityData.Text.SHADOW_FLAG);
    }

    public void setSeeThrough(boolean value) {
        setDisplayFlag(DisplayEntityData.Text.SEE_THROUGH_FLAG, value);
    }

    public boolean getSeeThrough() {
        return getDisplayFlag(DisplayEntityData.Text.SEE_THROUGH_FLAG);
    }

    public void setDefaultBackground(boolean value) {
        setDisplayFlag(DisplayEntityData.Text.DEFAULT_BACKGROUND_FLAG, value);
    }

    public boolean getDefaultBackground() {
        return getDisplayFlag(DisplayEntityData.Text.DEFAULT_BACKGROUND_FLAG);
    }

    private static byte flag(int base, int flag, boolean value) {
        return (byte) (value ? base | flag : base & ~flag);
    }

    @Override
    protected final EntityType<? extends Display> getEntityType() {
        return EntityType.TEXT_DISPLAY;
    }
}
