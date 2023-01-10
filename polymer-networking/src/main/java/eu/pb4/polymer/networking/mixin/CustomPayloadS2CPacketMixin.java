package eu.pb4.polymer.networking.mixin;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.networking.api.PolymerServerNetworking;
import eu.pb4.polymer.networking.api.ServerPacketWriter;
import eu.pb4.polymer.networking.impl.CustomPayloadS2CExt;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CustomPayloadS2CPacket.class)
public class CustomPayloadS2CPacketMixin implements CustomPayloadS2CExt {
    @Shadow @Final private Identifier channel;
    @Nullable
    @Unique
    private ServerPacketWriter polymerNet$writer;

    @Override
    public void polymerNet$setWriter(ServerPacketWriter packet) {
        this.polymerNet$writer = packet;
    }

    @Inject(method = "write", at = @At("TAIL"))
    private void polymerNet$write(PacketByteBuf buf, CallbackInfo ci) {
        if (this.polymerNet$writer != null) {
            var player = PolymerCommonUtils.getPlayerContext();

            if (player != null) {
                var ver = PolymerServerNetworking.getSupportedVersion(player.networkHandler, this.channel);
                buf.writeVarInt(ver);
                this.polymerNet$writer.write(player.networkHandler, buf, this.channel, ver);
            }
        }
    }

    @Environment(EnvType.CLIENT)
    @ModifyArg(method = "getData", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;<init>(Lio/netty/buffer/ByteBuf;)V"))
    private ByteBuf polymerNet$replaceEmpty(ByteBuf vanilla) {
        if (this.polymerNet$writer != null) {
            return Unpooled.buffer();
        }
        return vanilla;
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "getData", at = @At("RETURN"))
    private void polymerNet$writeData(CallbackInfoReturnable<PacketByteBuf> cir) {
        if (this.polymerNet$writer != null) {
            var player = PolymerCommonUtils.getPlayerContext();
            if (player != null) {
                var ver = PolymerServerNetworking.getSupportedVersion(player.networkHandler, this.channel);
                var buf = cir.getReturnValue();
                buf.writeVarInt(ver);
                this.polymerNet$writer.write(player.networkHandler, buf, this.channel, ver);
            }
        }
    }
}
