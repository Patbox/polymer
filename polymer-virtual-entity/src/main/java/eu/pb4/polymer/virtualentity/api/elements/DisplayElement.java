package eu.pb4.polymer.virtualentity.api.elements;

import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.Brightness;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.MatrixUtil;
import org.jetbrains.annotations.Nullable;
import org.joml.*;


@SuppressWarnings("ConstantConditions")
public abstract class DisplayElement extends GenericEntityElement {
    @Override
    protected abstract EntityType<? extends DisplayEntity> getEntityType();

    public void setTransformation(AffineTransformation transformation) {
        this.dataTracker.set(DisplayTrackedData.TRANSLATION, transformation.getTranslation());
        this.dataTracker.set(DisplayTrackedData.LEFT_ROTATION, transformation.getLeftRotation());
        this.dataTracker.set(DisplayTrackedData.SCALE, transformation.getScale());
        this.dataTracker.set(DisplayTrackedData.RIGHT_ROTATION, transformation.getRightRotation());
    }

    public void setTransformation(Matrix4f matrix) {
        float f = 1.0F / matrix.m33();
        var triple = MatrixUtil.svdDecompose((new Matrix3f(matrix)).scale(f));
        this.dataTracker.set(DisplayTrackedData.TRANSLATION, matrix.getTranslation(new Vector3f()));
        this.dataTracker.set(DisplayTrackedData.LEFT_ROTATION, new Quaternionf(triple.getLeft()));
        this.dataTracker.set(DisplayTrackedData.SCALE, new Vector3f(triple.getMiddle()));
        this.dataTracker.set(DisplayTrackedData.RIGHT_ROTATION, new Quaternionf(triple.getRight()));
    }

    public void setTransformation(Matrix4x3f matrix) {
        var triple = MatrixUtil.svdDecompose((new Matrix3f()).set(matrix));
        this.dataTracker.set(DisplayTrackedData.TRANSLATION, matrix.getTranslation(new Vector3f()));
        this.dataTracker.set(DisplayTrackedData.LEFT_ROTATION, new Quaternionf(triple.getLeft()));
        this.dataTracker.set(DisplayTrackedData.SCALE, new Vector3f(triple.getMiddle()));
        this.dataTracker.set(DisplayTrackedData.RIGHT_ROTATION, new Quaternionf(triple.getRight()));
    }

    public void setTranslation(Vector3fc vector3f) {
        this.dataTracker.set(DisplayTrackedData.TRANSLATION, new Vector3f(vector3f));
    }

    public Vector3fc getTranslation() {
        return this.dataTracker.get(DisplayTrackedData.TRANSLATION);
    }

    public void setScale(Vector3fc vector3f) {
        this.dataTracker.set(DisplayTrackedData.SCALE, new Vector3f(vector3f));
    }

    public Vector3fc getScale() {
        return this.dataTracker.get(DisplayTrackedData.SCALE);
    }

    public void setLeftRotation(Quaternionfc quaternion) {
        this.dataTracker.set(DisplayTrackedData.LEFT_ROTATION, new Quaternionf(quaternion));
    }

    public Quaternionfc getLeftRotation() {
        return this.dataTracker.get(DisplayTrackedData.LEFT_ROTATION);
    }

    public void setRightRotation(Quaternionfc quaternion) {
        this.dataTracker.set(DisplayTrackedData.RIGHT_ROTATION, new Quaternionf(quaternion));
    }

    public Quaternionfc getRightRotation() {
        return this.dataTracker.get(DisplayTrackedData.RIGHT_ROTATION);
    }

    public int getInterpolationDuration() {
        return this.dataTracker.get(DisplayTrackedData.INTERPOLATION_DURATION);
    }

    public void setInterpolationDuration(int interpolationDuration) {
        this.dataTracker.set(DisplayTrackedData.INTERPOLATION_DURATION, interpolationDuration);
    }

    public int getStartInterpolation() {
        return this.dataTracker.get(DisplayTrackedData.START_INTERPOLATION);
    }

    public void startInterpolation() {
        this.dataTracker.setDirty(DisplayTrackedData.START_INTERPOLATION, true);
    }

    public void setStartInterpolation(int startInterpolation) {
        this.dataTracker.set(DisplayTrackedData.START_INTERPOLATION, startInterpolation, true);
    }

    public DisplayEntity.BillboardMode getBillboardMode() {
        return DisplayEntity.BillboardMode.FROM_INDEX.apply(this.dataTracker.get(DisplayTrackedData.BILLBOARD));
    }

    public void setBillboardMode(DisplayEntity.BillboardMode billboardMode) {
        this.dataTracker.set(DisplayTrackedData.BILLBOARD, (byte) billboardMode.ordinal());
    }

    @Nullable
    public Brightness getBrightness() {
        int i = this.dataTracker.get(DisplayTrackedData.BRIGHTNESS);
        return i != -1 ? Brightness.unpack(i) : null;
    }

    public void setBrightness(@Nullable Brightness brightness) {
        this.dataTracker.set(DisplayTrackedData.BRIGHTNESS, brightness != null ? brightness.pack() : -1);
    }

    public float getViewRange() {
        return this.dataTracker.get(DisplayTrackedData.VIEW_RANGE);
    }

    public void setViewRange(float viewRange) {
        this.dataTracker.set(DisplayTrackedData.VIEW_RANGE, viewRange);
    }

    public float getShadowRadius() {
        return this.dataTracker.get(DisplayTrackedData.SHADOW_RADIUS);
    }

    public void setShadowRadius(float shadowRadius) {
        this.dataTracker.set(DisplayTrackedData.SHADOW_RADIUS, shadowRadius);
    }

    public float getShadowStrength() {
        return this.dataTracker.get(DisplayTrackedData.SHADOW_STRENGTH);
    }

    public void setShadowStrength(float shadowStrength) {
        this.dataTracker.set(DisplayTrackedData.SHADOW_STRENGTH, shadowStrength);
    }

    public float getDisplayWidth() {
        return this.dataTracker.get(DisplayTrackedData.WIDTH);
    }

    public float getDisplayHeight() {
        return this.dataTracker.get(DisplayTrackedData.HEIGHT);
    }

    public void setDisplayWidth(float width) {
        this.dataTracker.set(DisplayTrackedData.WIDTH, width);
    }

    public void setDisplayHeight(float height) {
        this.dataTracker.set(DisplayTrackedData.HEIGHT, height);
    }

    public void setDisplaySize(float width, float height) {
        this.setDisplayWidth(width);
        this.setDisplayHeight(height);
    }

    public void setDisplaySize(EntityDimensions dimensions) {
        this.setDisplayWidth(dimensions.width);
        this.setDisplayHeight(dimensions.height);
    }

    public int getGlowColorOverride() {
        return this.dataTracker.get(DisplayTrackedData.GLOW_COLOR_OVERRIDE);
    }

    public void setGlowColorOverride(int glowColorOverride) {
        this.dataTracker.set(DisplayTrackedData.GLOW_COLOR_OVERRIDE, glowColorOverride);
    }
}
