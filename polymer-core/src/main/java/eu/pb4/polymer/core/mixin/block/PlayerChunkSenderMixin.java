package eu.pb4.polymer.core.mixin.block;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.BitSet;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;

@Mixin(value = PlayerChunkSender.class, priority = 1001)
public class PlayerChunkSenderMixin {
    @WrapOperation(method = "sendChunk", at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/lighting/LevelLightEngine;Ljava/util/BitSet;Ljava/util/BitSet;)Lnet/minecraft/network/protocol/game/ClientboundLevelChunkWithLightPacket;"), require = 0)
    private static ClientboundLevelChunkWithLightPacket addContext(LevelChunk chunk, LevelLightEngine lightProvider, @Nullable BitSet skyBits, @Nullable BitSet blockBits, Operation<ClientboundLevelChunkWithLightPacket> call,
                                                 @Local(argsOnly = true) ServerGamePacketListenerImpl handler) {
        return PacketContext.supplyWithContext(handler, () -> call.call(chunk, lightProvider, skyBits, blockBits));
    }

    @WrapWithCondition(method = "dropChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"), require = 0)
    private boolean skipChunkClearing(ServerGamePacketListenerImpl instance, Packet packet) {
        return PolymerImplUtils.IS_RELOADING_LEVEL.get() == null;
    }
}
