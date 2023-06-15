package eu.pb4.polymer.virtualentity.api.elements;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractElement implements VirtualElement {
    private ElementHolder holder;
    private Vec3d offset = Vec3d.ZERO;
    private InteractionHandler handler = InteractionHandler.EMPTY;


    @Override
    public Vec3d getOffset() {
        return this.offset;
    }

    @Override
    public void setOffset(Vec3d offset) {
        this.offset = offset;
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
