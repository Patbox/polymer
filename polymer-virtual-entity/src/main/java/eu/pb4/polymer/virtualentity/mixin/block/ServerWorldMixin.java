package eu.pb4.polymer.virtualentity.mixin.block;

import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {

    @Inject(method = "tickChunk", at = @At("TAIL"))
    private void hologramApi$tickHolograms(WorldChunk chunk, int randomTickSpeed, CallbackInfo ci) {
        for (var hologram : new ArrayList<>(((HolderAttachmentHolder) chunk).polymerVE$getHolders())) {
            hologram.tick();
        }
    }
}
