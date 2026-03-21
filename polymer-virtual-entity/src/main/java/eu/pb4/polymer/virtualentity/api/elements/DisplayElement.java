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
        this.syncedData.set(DisplayEntityData.TRANSLATION, transformation.translation());
        this.syncedData.set(DisplayEntityData.LEFT_ROTATION, transformation.leftRotation());
        this.syncedData.set(DisplayEntityData.SCALE, transformation.scale());
        this.syncedData.set(DisplayEntityData.RIGHT_ROTATION, transformation.rightRotation());
    }

    public void setTransformation(Matrix4fc matrix) {
        float f = 1.0F / matrix.m33();
        var triple = MatrixUtil.svdDecompose(new Matrix3f(matrix).scale(f));
        this.syncedData.set(DisplayEntityData.TRANSLATION, matrix.getTranslation(new Vector3f()));
        this.syncedData.set(DisplayEntityData.LEFT_ROTATION, new Quaternionf(triple.getLeft()));
        this.syncedData.set(DisplayEntityData.SCALE, new Vector3f(triple.getMiddle()));
        this.syncedData.set(DisplayEntityData.RIGHT_ROTATION, new Quaternionf(triple.getRight()));
    }

    public void setTransformation(Matrix4x3fc matrix) {
        var triple = MatrixUtil.svdDecompose(new Matrix3f().set(matrix));
        this.syncedData.set(DisplayEntityData.TRANSLATION, matrix.getTranslation(new Vector3f()));
        this.syncedData.set(DisplayEntityData.LEFT_ROTATION, new Quaternionf(triple.getLeft()));
        this.syncedData.set(DisplayEntityData.SCALE, new Vector3f(triple.getMiddle()));
        this.syncedData.set(DisplayEntityData.RIGHT_ROTATION, new Quaternionf(triple.getRight()));
    }

    public boolean isTransformationDirty() {
        return this.syncedData.isDirty(DisplayEntityData.TRANSLATION)
                || this.syncedData.isDirty(DisplayEntityData.LEFT_ROTATION)
                || this.syncedData.isDirty(DisplayEntityData.SCALE)
                || this.syncedData.isDirty(DisplayEntityData.RIGHT_ROTATION);
    }

    public void setTranslation(Vector3fc vector3f) {
        this.syncedData.set(DisplayEntityData.TRANSLATION, new Vector3f(vector3f));
    }

    public Vector3fc getTranslation() {
        return this.syncedData.get(DisplayEntityData.TRANSLATION);
    }

    public void setScale(Vector3fc vector3f) {
        this.syncedData.set(DisplayEntityData.SCALE, new Vector3f(vector3f));
    }

    public Vector3fc getScale() {
        return this.syncedData.get(DisplayEntityData.SCALE);
    }

    public void setLeftRotation(Quaternionfc quaternion) {
        this.syncedData.set(DisplayEntityData.LEFT_ROTATION, new Quaternionf(quaternion));
    }

    public Quaternionfc getLeftRotation() {
        return this.syncedData.get(DisplayEntityData.LEFT_ROTATION);
    }

    public void setRightRotation(Quaternionfc quaternion) {
        this.syncedData.set(DisplayEntityData.RIGHT_ROTATION, new Quaternionf(quaternion));
    }

    public Quaternionfc getRightRotation() {
        return this.syncedData.get(DisplayEntityData.RIGHT_ROTATION);
    }

    public int getInterpolationDuration() {
        return this.syncedData.get(DisplayEntityData.INTERPOLATION_DURATION);
    }

    public void setInterpolationDuration(int interpolationDuration) {
        this.syncedData.set(DisplayEntityData.INTERPOLATION_DURATION, interpolationDuration);
    }

    public int getTeleportDuration() {
        return this.syncedData.get(DisplayEntityData.TELEPORTATION_DURATION);
    }

    public void setTeleportDuration(int interpolationDuration) {
        this.syncedData.set(DisplayEntityData.TELEPORTATION_DURATION, interpolationDuration);
    }

    public int getStartInterpolation() {
        return this.syncedData.get(DisplayEntityData.START_INTERPOLATION);
    }

    public void startInterpolation() {
        this.syncedData.setDirty(DisplayEntityData.START_INTERPOLATION, true);
    }

    public void setStartInterpolation(int startInterpolation) {
        this.syncedData.set(DisplayEntityData.START_INTERPOLATION, startInterpolation, true);
    }

    public void startInterpolationIfDirty() {
        if (this.isTransformationDirty()) {
            this.startInterpolation();
        }
    }

    public Display.BillboardConstraints getBillboardMode() {
        return Display.BillboardConstraints.BY_ID.apply(this.syncedData.get(DisplayEntityData.BILLBOARD));
    }

    public void setBillboardMode(Display.BillboardConstraints billboardMode) {
        this.syncedData.set(DisplayEntityData.BILLBOARD, (byte) billboardMode.ordinal());
    }

    @Nullable
    public Brightness getBrightness() {
        int i = this.syncedData.get(DisplayEntityData.BRIGHTNESS);
        return i != -1 ? Brightness.unpack(i) : null;
    }

    public void setBrightness(@Nullable Brightness brightness) {
        this.syncedData.set(DisplayEntityData.BRIGHTNESS, brightness != null ? brightness.pack() : -1);
    }

    public float getViewRange() {
        return this.syncedData.get(DisplayEntityData.VIEW_RANGE);
    }

    public void setViewRange(float viewRange) {
        this.syncedData.set(DisplayEntityData.VIEW_RANGE, viewRange);
    }

    public float getShadowRadius() {
        return this.syncedData.get(DisplayEntityData.SHADOW_RADIUS);
    }

    public void setShadowRadius(float shadowRadius) {
        this.syncedData.set(DisplayEntityData.SHADOW_RADIUS, shadowRadius);
    }

    public float getShadowStrength() {
        return this.syncedData.get(DisplayEntityData.SHADOW_STRENGTH);
    }

    public void setShadowStrength(float shadowStrength) {
        this.syncedData.set(DisplayEntityData.SHADOW_STRENGTH, shadowStrength);
    }

    public float getDisplayWidth() {
        return this.syncedData.get(DisplayEntityData.WIDTH);
    }

    public float getDisplayHeight() {
        return this.syncedData.get(DisplayEntityData.HEIGHT);
    }

    public void setDisplayWidth(float width) {
        this.syncedData.set(DisplayEntityData.WIDTH, width);
    }

    public void setDisplayHeight(float height) {
        this.syncedData.set(DisplayEntityData.HEIGHT, height);
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
        return this.syncedData.get(DisplayEntityData.GLOW_COLOR_OVERRIDE);
    }

    public void setGlowColorOverride(int glowColorOverride) {
        this.syncedData.set(DisplayEntityData.GLOW_COLOR_OVERRIDE, glowColorOverride);
    }
}
