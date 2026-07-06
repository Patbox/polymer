package eu.pb4.polymer.virtualentity.api;

import eu.pb4.polymer.virtualentity.mixin.accessors.EntityAccessor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public class LocalInterpolationHandler {
    public static final int DEFAULT_INTERPOLATION_STEPS = 3;
    private final Entity entity;
    private final InterpolationData interpolationData;
    private final @Nullable Consumer<LocalInterpolationHandler> onInterpolationStart;
    private int interpolationSteps;
    private @Nullable Vec3 previousTickPosition;
    private @Nullable Vec2 previousTickRot;
    private Vec3 position;
    private Vec2 rotation;

    public LocalInterpolationHandler(final Entity entity) {
        this(entity, 3, null);
    }

    public LocalInterpolationHandler(final Entity entity, final int interpolationSteps) {
        this(entity, interpolationSteps, null);
    }

    public LocalInterpolationHandler(final Entity entity, final @Nullable Consumer<LocalInterpolationHandler> onInterpolationStart) {
        this(entity, 3, onInterpolationStart);
    }

    public LocalInterpolationHandler(final Entity entity, final int interpolationSteps, final @Nullable Consumer<LocalInterpolationHandler> onInterpolationStart) {
        this.interpolationData = new InterpolationData(0, Vec3.ZERO, 0.0F, 0.0F);
        this.interpolationSteps = interpolationSteps;
        this.entity = entity;
        this.onInterpolationStart = onInterpolationStart;
        this.position = this.entity.position();
        this.rotation = this.entity.getRotationVector();
    }

    public Vec3 position() {
        return this.position;
    }

    public float yRot() {
        return this.rotation.y;
    }

    public float xRot() {
        return this.rotation.x;
    }

    public void interpolateTo(final Vec3 position, final float yRot, final float xRot) {
        if (this.interpolationSteps == 0) {
            this.position = position;
            this.rotation = new Vec2(xRot, yRot);
            this.cancel();
        } else if (!this.hasActiveInterpolation() || !Objects.equals(this.yRot(), yRot) || !Objects.equals(this.xRot(), xRot) || !Objects.equals(this.position(), position)) {
            this.interpolationData.steps = this.interpolationSteps;
            this.interpolationData.position = position;
            this.interpolationData.yRot = yRot;
            this.interpolationData.xRot = xRot;
            this.previousTickPosition = this.position;
            this.previousTickRot = this.rotation;
            if (this.onInterpolationStart != null) {
                this.onInterpolationStart.accept(this);
            }

        }
    }

    public boolean hasActiveInterpolation() {
        return this.interpolationData.steps > 0;
    }

    public void setInterpolationLength(final int steps) {
        this.interpolationSteps = steps;
    }

    public void interpolate() {
        if (!this.hasActiveInterpolation()) {
            this.cancel();
        } else {
            double alpha = (double) 1.0F / (double) this.interpolationData.steps;
            if (this.previousTickPosition != null) {
                Vec3 deltaSinceLastInterpolation = this.position.subtract(this.previousTickPosition);
                if (this.entity.level().noCollision(this.entity, ((EntityAccessor) this.entity).callMakeBoundingBox(this.interpolationData.position.add(deltaSinceLastInterpolation)))) {
                    this.interpolationData.addDelta(deltaSinceLastInterpolation);
                }
            }

            if (this.previousTickRot != null) {
                float deltaYRotSinceLastInterpolation = this.rotation.y - this.previousTickRot.y;
                float deltaXRotSinceLastInterpolation = this.rotation.x - this.previousTickRot.x;
                this.interpolationData.addRotation(deltaYRotSinceLastInterpolation, deltaXRotSinceLastInterpolation);
            }

            double x = Mth.lerp(alpha, this.position.x, this.interpolationData.position.x);
            double y = Mth.lerp(alpha, this.position.y, this.interpolationData.position.y);
            double z = Mth.lerp(alpha, this.position.z, this.interpolationData.position.z);
            Vec3 newPosition = new Vec3(x, y, z);
            float newYRot = (float) Mth.rotLerp(alpha, this.rotation.y, this.interpolationData.yRot);
            float newXRot = (float) Mth.lerp(alpha, this.rotation.x, this.interpolationData.xRot);
            this.position = newPosition;
            this.rotation = new Vec2(newXRot, newYRot);
            this.interpolationData.decrease();
            this.previousTickPosition = newPosition;
            this.previousTickRot = this.rotation;
        }
    }

    public void cancel() {
        this.interpolationData.steps = 0;
        this.previousTickPosition = null;
        this.previousTickRot = null;
    }

    private static class InterpolationData {
        protected int steps;
        private Vec3 position;
        private float yRot;
        private float xRot;

        private InterpolationData(final int steps, final Vec3 position, final float yRot, final float xRot) {
            this.steps = steps;
            this.position = position;
            this.yRot = yRot;
            this.xRot = xRot;
        }

        public void decrease() {
            --this.steps;
        }

        public void addDelta(final Vec3 delta) {
            this.position = this.position.add(delta);
        }

        public void addRotation(final float yRot, final float xRot) {
            this.yRot += yRot;
            this.xRot += xRot;
        }
    }
}
