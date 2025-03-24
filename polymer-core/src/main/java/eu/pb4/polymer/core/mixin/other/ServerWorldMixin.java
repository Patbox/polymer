package eu.pb4.polymer.core.mixin.other;

import eu.pb4.polymer.core.impl.PolymerImplUtils;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @ModifyVariable(method = "playSound", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Entity ignoreEntityException(Entity entity) {
        return PolymerImplUtils.IGNORE_PLAY_SOUND_EXCLUSION.get() != null ? null : entity;
    }

    @ModifyVariable(method = "playSoundFromEntity", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Entity ignoreEntityException2(Entity entity) {
        return PolymerImplUtils.IGNORE_PLAY_SOUND_EXCLUSION.get() != null ? null : entity;
    }
}
