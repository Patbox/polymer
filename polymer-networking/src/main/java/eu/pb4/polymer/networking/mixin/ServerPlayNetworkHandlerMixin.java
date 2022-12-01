package eu.pb4.polymer.networking.mixin;

import eu.pb4.polymer.networking.api.DynamicPacket;
import eu.pb4.polymer.networking.impl.NetworkHandlerExtension;
import eu.pb4.polymer.networking.impl.ServerPacketRegistry;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin implements NetworkHandlerExtension {
    @Unique
    private final Object2IntMap<Identifier> polymer$protocolMap = new Object2IntOpenHashMap<>();

    @Unique
    private final Object2LongMap<Identifier> polymer$rateLimits = new Object2LongOpenHashMap<>();
    @Shadow
    public ServerPlayerEntity player;
    @Unique
    private String polymer$version = "";

    @Shadow
    public abstract void sendPacket(Packet<?> packet);

    @Shadow
    public abstract ServerPlayerEntity getPlayer();


    @Override
    public boolean polymer$hasPolymer() {
        return !this.polymer$version.isEmpty();
    }

    @Override
    public String polymer$version() {
        return this.polymer$version;
    }

    @Override
    public void polymer$setVersion(String version) {
        this.polymer$version = version;
    }

    @Override
    public long polymer$lastPacketUpdate(Identifier packet) {
        return this.polymer$rateLimits.getLong(packet);
    }

    @Override
    public void polymer$savePacketTime(Identifier packet) {
        this.polymer$rateLimits.put(packet, System.currentTimeMillis());
    }

    @Override
    public void polymer$resetSupported() {
        this.polymer$protocolMap.clear();
    }

    @Override
    public int polymer$getSupportedVersion(Identifier identifier) {
        return this.polymer$protocolMap.getOrDefault(identifier, -1);
    }

    @Override
    public void polymer$setSupportedVersion(Identifier identifier, int i) {
        this.polymer$protocolMap.put(identifier, i);
    }

    @Override
    public Object2IntMap<Identifier> polymer$getSupportMap() {
        return this.polymer$protocolMap;
    }

    @Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
    private void polymer$catchPackets(CustomPayloadC2SPacket packet, CallbackInfo ci) {
        if (ServerPacketRegistry.handle((ServerPlayNetworkHandler) (Object) this, packet.getChannel(), packet.getData())) {
            this.polymer$savePacketTime(packet.getChannel());
            ci.cancel();
        }
    }

    @ModifyVariable(method = "sendPacket(Lnet/minecraft/network/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At("HEAD"))
    private Packet<?> polymer$replacePacket(Packet<?> packet) {
        if (packet instanceof DynamicPacket dynamicPacket) {
            var out = dynamicPacket.createPacket((ServerPlayNetworkHandler) (Object) (this), this.player);

            if (out != null) {
                return out;
            }
        }

        return packet;
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At("HEAD"), cancellable = true)
    private void polymer$dontLeakDynamic(Packet<?> packet, PacketCallbacks arg, CallbackInfo ci) {
        if (packet instanceof DynamicPacket) {
            ci.cancel();
        }
    }
}
