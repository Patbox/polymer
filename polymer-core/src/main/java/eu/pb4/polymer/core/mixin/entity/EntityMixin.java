package eu.pb4.polymer.core.mixin.entity;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.impl.interfaces.PolymerEntityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin implements PolymerEntityProvider {
    @Shadow public abstract EntityType<?> getType();

    @Unique
    @Nullable
    private PolymerEntity polymerEntity;


    @Inject(method = "<init>", at = @At("TAIL"))
    private void updatePolymerEntity(EntityType type, World world, CallbackInfo ci) {
        var constructor = PolymerEntityUtils.getPolymerEntityConstructor(getType());
        this.polymerEntity = constructor != null ? constructor.get(this) : null;
    }

    @Override
    public @Nullable PolymerEntity polymer$getPolymerEntity() {
        return this.polymerEntity;
    }
}
