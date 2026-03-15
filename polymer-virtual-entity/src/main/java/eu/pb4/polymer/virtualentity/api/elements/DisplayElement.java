package eu.pb4.polymer.virtualentity.api.elements;

import com.mojang.math.MatrixUtil;
import com.mojang.math.Transformation;
import eu.pb4.polymer.virtualentity.api.data.DisplayEntityData;
import net.minecraft.util.Brightness;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import org.jspecify.annotations.Nullable;
import org.joml.*;


@SuppressWarnings("ConstantConditions")
public abstract class DisplayElement extends GenericEntityElement {
    @Override
    protected abstract EntityType<? extends Display> getEntityType();

    public void setTransformation(Transformation transformation) {
        this.dataTracker.set(DisplayEntityData.TRANSLATION, transformation.translation());
        this.dataTracker.set(DisplayEntityData.LEFT_ROTATION, transformation.leftRotation());
        this.dataTracker.set(DisplayEntityData.SCALE, transformation.scale());
        this.dataTracker.set(DisplayEntityData.RIGHT_ROTATION, transformation.rightRotation());
    }

    public void setTransformation(Matrix4fc matrix) {
        float f = 1.0F / matrix.m33();
        var triple = MatrixUtil.svdDecompose(new Matrix3f(matrix).scale(f));
        this.dataTracker.set(DisplayEntityData.TRANSLATION, matrix.getTranslation(new Vector3f()));
        this.dataTracker.set(DisplayEntityData.LEFT_ROTATION, new Quaternionf(triple.getLeft()));
        this.dataTracker.set(DisplayEntityData.SCALE, new Vector3f(triple.getMiddle()));
        this.dataTracker.set(DisplayEntityData.RIGHT_ROTATION, new Quaternionf(triple.getRight()));
    }

    public void setTransformation(Matrix4x3fc matrix) {
        var triple = MatrixUtil.svdDecompose(new Matrix3f().set(matrix));
        this.dataTracker.set(DisplayEntityData.TRANSLATION, matrix.getTranslation(new Vector3f()));
        this.dataTracker.set(DisplayEntityData.LEFT_ROTATION, new Quaternionf(triple.getLeft()));
        this.dataTracker.set(DisplayEntityData.SCALE, new Vector3f(triple.getMiddle()));
        this.dataTracker.set(DisplayEntityData.RIGHT_ROTATION, new Quaternionf(triple.getRight()));
    }

    public boolean isTransformationDirty() {
        return this.dataTracker.isDirty(DisplayEntityData.TRANSLATION)
                || this.dataTracker.isDirty(DisplayEntityData.LEFT_ROTATION)
                || this.dataTracker.isDirty(DisplayEntityData.SCALE)
                || this.dataTracker.isDirty(DisplayEntityData.RIGHT_ROTATION);
    }

    public void setTranslation(Vector3fc vector3f) {
        this.dataTracker.set(DisplayEntityData.TRANSLATION, new Vector3f(vector3f));
    }

    public Vector3fc getTranslation() {
        return this.dataTracker.get(DisplayEntityData.TRANSLATION);
    }

    public void setScale(Vector3fc vector3f) {
        this.dataTracker.set(DisplayEntityData.SCALE, new Vector3f(vector3f));
    }

    public Vector3fc getScale() {
        return this.dataTracker.get(DisplayEntityData.SCALE);
    }

    public void setLeftRotation(Quaternionfc quaternion) {
        this.dataTracker.set(DisplayEntityData.LEFT_ROTATION, new Quaternionf(quaternion));
    }

    public Quaternionfc getLeftRotation() {
        return this.dataTracker.get(DisplayEntityData.LEFT_ROTATION);
    }

    public void setRightRotation(Quaternionfc quaternion) {
        this.dataTracker.set(DisplayEntityData.RIGHT_ROTATION, new Quaternionf(quaternion));
    }

    public Quaternionfc getRightRotation() {
        return this.dataTracker.get(DisplayEntityData.RIGHT_ROTATION);
    }

    public int getInterpolationDuration() {
        return this.dataTracker.get(DisplayEntityData.INTERPOLATION_DURATION);
    }

    public void setInterpolationDuration(int interpolationDuration) {
        this.dataTracker.set(DisplayEntityData.INTERPOLATION_DURATION, interpolationDuration);
    }

    public int getTeleportDuration() {
        return this.dataTracker.get(DisplayEntityData.TELEPORTATION_DURATION);
    }

    public void setTeleportDuration(int interpolationDuration) {
        this.dataTracker.set(DisplayEntityData.TELEPORTATION_DURATION, interpolationDuration);
    }

    public int getStartInterpolation() {
        return this.dataTracker.get(DisplayEntityData.START_INTERPOLATION);
    }

    public void startInterpolation() {
        this.dataTracker.setDirty(DisplayEntityData.START_INTERPOLATION, true);
    }

    public void setStartInterpolation(int startInterpolation) {
        this.dataTracker.set(DisplayEntityData.START_INTERPOLATION, startInterpolation, true);
    }

    public void startInterpolationIfDirty() {
        if (this.isTransformationDirty()) {
            this.startInterpolation();
        }
    }

    public Display.BillboardConstraints getBillboardMode() {
        return Display.BillboardConstraints.BY_ID.apply(this.dataTracker.get(DisplayEntityData.BILLBOARD));
    }

    public void setBillboardMode(Display.BillboardConstraints billboardMode) {
        this.dataTracker.set(DisplayEntityData.BILLBOARD, (byte) billboardMode.ordinal());
    }

    @Nullable
    public Brightness getBrightness() {
        int i = this.dataTracker.get(DisplayEntityData.BRIGHTNESS);
        return i != -1 ? Brightness.unpack(i) : null;
    }

    public void setBrightness(@Nullable Brightness brightness) {
        this.dataTracker.set(DisplayEntityData.BRIGHTNESS, brightness != null ? brightness.pack() : -1);
    }

    public float getViewRange() {
        return this.dataTracker.get(DisplayEntityData.VIEW_RANGE);
    }

    public void setViewRange(float viewRange) {
        this.dataTracker.set(DisplayEntityData.VIEW_RANGE, viewRange);
    }

    public float getShadowRadius() {
        return this.dataTracker.get(DisplayEntityData.SHADOW_RADIUS);
    }

    public void setShadowRadius(float shadowRadius) {
        this.dataTracker.set(DisplayEntityData.SHADOW_RADIUS, shadowRadius);
    }

    public float getShadowStrength() {
        return this.dataTracker.get(DisplayEntityData.SHADOW_STRENGTH);
    }

    public void setShadowStrength(float shadowStrength) {
        this.dataTracker.set(DisplayEntityData.SHADOW_STRENGTH, shadowStrength);
    }

    public float getDisplayWidth() {
        return this.dataTracker.get(DisplayEntityData.WIDTH);
    }

    public float getDisplayHeight() {
        return this.dataTracker.get(DisplayEntityData.HEIGHT);
    }

    public void setDisplayWidth(float width) {
        this.dataTracker.set(DisplayEntityData.WIDTH, width);
    }

    public void setDisplayHeight(float height) {
        this.dataTracker.set(DisplayEntityData.HEIGHT, height);
    }

    public void setDisplaySize(float width, float height) {
        this.setDisplayWidth(width);
        this.setDisplayHeight(height);
    }

    public void setDisplaySize(EntityDimensions dimensions) {
        this.setDisplayWidth(dimensions.width());
        this.setDisplayHeight(dimensions.height());
    }

    public int getGlowColorOverride() {
        return this.dataTracker.get(DisplayEntityData.GLOW_COLOR_OVERRIDE);
    }

    public void setGlowColorOverride(int glowColorOverride) {
        this.dataTracker.set(DisplayEntityData.GLOW_COLOR_OVERRIDE, glowColorOverride);
    }
}
