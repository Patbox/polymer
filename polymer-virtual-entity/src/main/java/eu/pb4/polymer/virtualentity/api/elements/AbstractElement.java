package eu.pb4.polymer.virtualentity.api.elements;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class AbstractElement implements VirtualElement {
    private ElementHolder holder;
    private Vec3d offset = Vec3d.ZERO;
    @Nullable
    private Vec3d overridePos;
    @Nullable
    protected Vec3d lastSyncedPos;
    private InteractionHandler handler = InteractionHandler.EMPTY;



    @Override
    public Vec3d getOffset() {
        return this.offset;
    }

    @Override
    public void setOffset(Vec3d offset) {
        this.offset = offset;
    }

    @Nullable
    public Vec3d getOverridePos() {
        return this.overridePos;
    }

    @Nullable
    public void setOverridePos(Vec3d vec3d) {
        this.overridePos = vec3d;
    }

    @Override
    public Vec3d getLastSyncedPos() {
        return this.lastSyncedPos;
    }
    public void updateLastSyncedPos() {
        this.lastSyncedPos = getCurrentPos();
    }

    @Override
    public @Nullable ElementHolder getHolder() {
        return this.holder;
    }

    @Override
    public void setHolder(ElementHolder holder) {
        this.holder = holder;
    }

    @Override
    public InteractionHandler getInteractionHandler(ServerPlayerEntity player) {
        return this.handler;
    }

    public void setInteractionHandler(InteractionHandler handler) {
        this.handler = handler;
    }
}
