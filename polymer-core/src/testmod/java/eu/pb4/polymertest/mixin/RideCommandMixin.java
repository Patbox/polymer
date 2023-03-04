package eu.pb4.polymertest.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.command.RideCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RideCommand.class)
public class RideCommandMixin {
    @Redirect(method = "executeMount", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getType()Lnet/minecraft/entity/EntityType;"))
    private static EntityType<?> unlockPlayer(Entity instance) {
        return null;
    }
}
