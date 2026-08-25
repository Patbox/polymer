package eu.pb4.polymer.core.mixin.block.packet;

import eu.pb4.polymer.core.impl.PolymerLightUpdateHelper;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.BitSet;

@Mixin(ClientboundLightUpdatePacketData.class)
public class ClientboundLightUpdatePacketDataMixin {
    @Inject(method = "<init>(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/lighting/LevelLightEngine;Ljava/util/BitSet;Ljava/util/BitSet;)V", at = @At("TAIL"))
    private void polymer$patchPolymerBlockLight(ChunkPos chunkPos, LevelLightEngine lightEngine, @Nullable BitSet skyChangedLightSectionFilter,
                                                @Nullable BitSet blockChangedLightSectionFilter, CallbackInfo ci) {
        PolymerLightUpdateHelper.patchLightData((ClientboundLightUpdatePacketData) (Object) this, chunkPos, lightEngine, skyChangedLightSectionFilter, blockChangedLightSectionFilter);
    }
}
