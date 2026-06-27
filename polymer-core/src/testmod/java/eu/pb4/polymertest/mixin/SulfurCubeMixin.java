package eu.pb4.polymertest.mixin;

import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymertest.SulfurCubeModelTestFix;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.cubemob.SulfurCube;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SulfurCube.class)
public class SulfurCubeMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void fixNonBlockItems(EntityType type, Level level, CallbackInfo ci) {
        var self = (SulfurCube) (Object) this;
        //EntityAttachment.ofTicking(new SulfurCubeModelTestFix(self), self);
    }
}
