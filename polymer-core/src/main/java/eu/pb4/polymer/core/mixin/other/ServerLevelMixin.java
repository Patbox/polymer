package eu.pb4.polymer.core.mixin.other;

import eu.pb4.polymer.core.impl.PolymerImplUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
    @ModifyVariable(method = "playSeededSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Entity ignoreEntityException(Entity entity) {
        return PolymerImplUtils.IGNORE_PLAY_SOUND_EXCLUSION.get() != null ? null : entity;
    }

    @ModifyVariable(method = "playSeededSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Entity ignoreEntityException2(Entity entity) {
        return PolymerImplUtils.IGNORE_PLAY_SOUND_EXCLUSION.get() != null ? null : entity;
    }
}
