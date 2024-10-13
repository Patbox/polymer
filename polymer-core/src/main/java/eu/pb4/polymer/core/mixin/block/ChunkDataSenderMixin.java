package eu.pb4.polymer.core.mixin.block;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.server.network.ChunkDataSender;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Unit;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.BitSet;

@Mixin(ChunkDataSender.class)
public class ChunkDataSenderMixin {
    @WrapOperation(method = "sendChunkData", at = @At(value = "NEW", target = "(Lnet/minecraft/world/chunk/WorldChunk;Lnet/minecraft/world/chunk/light/LightingProvider;Ljava/util/BitSet;Ljava/util/BitSet;)Lnet/minecraft/network/packet/s2c/play/ChunkDataS2CPacket;"), require = 0)
    private static ChunkDataS2CPacket addContext(WorldChunk chunk, LightingProvider lightProvider, @Nullable BitSet skyBits, @Nullable BitSet blockBits, Operation<ChunkDataS2CPacket> call,
                                                 @Local(argsOnly = true) ServerPlayNetworkHandler handler) {
        return PolymerCommonUtils.executeWithNetworkingLogic(handler, () -> call.call(chunk, lightProvider, skyBits, blockBits));
    }

    @WrapWithCondition(method = "unload", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"), require = 0)
    private boolean skipChunkClearing(ServerPlayNetworkHandler instance, Packet packet) {
        return PolymerImplUtils.IS_RELOADING_WORLD.get() == null;
    }
}
