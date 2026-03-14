package eu.pb4.polymer.core.mixin.item;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import eu.pb4.polymer.core.impl.interfaces.GenericPlayerContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.RemoteSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net/minecraft/server/level/ServerPlayer$1")
public class ServerPlayerContainerSynchronizerMixin {
    @Shadow
    @Final
    private ServerPlayer this$0;

    @ModifyReturnValue(method = "createSlot", at = @At("TAIL"))
    private RemoteSlot setContextForSlot(RemoteSlot slot) {
        if (slot instanceof GenericPlayerContext context) {
            context.polymer$setPlayer(this.this$0);
        }
        return slot;
    }
}
