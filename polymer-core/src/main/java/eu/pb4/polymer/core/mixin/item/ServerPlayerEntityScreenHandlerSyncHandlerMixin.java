package eu.pb4.polymer.core.mixin.item;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import eu.pb4.polymer.core.impl.interfaces.GenericPlayerContext;
import net.minecraft.screen.sync.TrackedSlot;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net/minecraft/server/network/ServerPlayerEntity$1")
public class ServerPlayerEntityScreenHandlerSyncHandlerMixin {
    @Shadow @Final private ServerPlayerEntity field_58075;

    @ModifyReturnValue(method = "createTrackedSlot", at = @At("TAIL"))
    private TrackedSlot setContextForSlot(TrackedSlot slot) {
        if (slot instanceof GenericPlayerContext context) {
            context.polymer$setPlayer(this.field_58075);
        }
        return slot;
    }
}
