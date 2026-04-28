package eu.pb4.polymer.core.mixin.other;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(PacketEncoder.class)
public class PacketEncoderMixin {
    @WrapMethod(method = "encode(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;Lio/netty/buffer/ByteBuf;)V")
    private void providePacket(ChannelHandlerContext ctx, Packet<?> packet, ByteBuf output, Operation<Void> original) {
        ScopedValue.where(PolymerImplUtils.WRITTEN_PACKET, packet).run(() -> original.call(ctx, packet, output));
    }
}
