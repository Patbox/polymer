package eu.pb4.polymertest.mixin;

import net.minecraft.server.commands.RideCommand;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RideCommand.class)
public class RideCommandMixin {
    @Redirect(method = "mount", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;is(Ljava/lang/Object;)Z"))
    private static boolean unlockPlayer(Entity instance, Object o) {
        return false;
    }
}
