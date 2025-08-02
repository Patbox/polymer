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

import java.util.function.Function;

@Mixin(Entity.class)
public abstract class EntityMixin implements PolymerEntityProvider {
    @Shadow public abstract EntityType<?> getType();

    @Unique
    @Nullable
    private PolymerEntity polymerEntity;


    @Inject(method = "<init>", at = @At("TAIL"))
    private void updatePolymerEntity(EntityType type, World world, CallbackInfo ci) {
        polymer$recreatePolymerEntity();
    }

    @Override
    public @Nullable PolymerEntity polymer$getPolymerEntity() {
        return this.polymerEntity;
    }

    @Override
    public void polymer$recreatePolymerEntity() {
        //noinspection unchecked
        var constructor = (Function<Object, PolymerEntity>) PolymerEntityUtils.getPolymerEntityConstructor(getType());
        this.polymerEntity = constructor != null ? constructor.apply(this) : null;
    }

    @Override
    public void polymer$setPolymerEntity(PolymerEntity polymerEntity) {
        this.polymerEntity = polymerEntity;
    }
}
