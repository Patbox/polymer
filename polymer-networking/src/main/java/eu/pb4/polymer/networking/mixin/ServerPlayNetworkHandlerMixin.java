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
    private final Object2IntMap<Identifier> polymerNet$protocolMap = new Object2IntOpenHashMap<>();

    @Unique
    private final Object2LongMap<Identifier> polymerNet$rateLimits = new Object2LongOpenHashMap<>();
    @Shadow
    public ServerPlayerEntity player;
    @Unique
    private String polymerNet$version = "";

    @Shadow
    public abstract void sendPacket(Packet<?> packet);

    @Shadow
    public abstract ServerPlayerEntity getPlayer();


    @Override
    public boolean polymerNet$hasPolymer() {
        return !this.polymerNet$version.isEmpty();
    }

    @Override
    public String polymerNet$version() {
        return this.polymerNet$version;
    }

    @Override
    public void polymerNet$setVersion(String version) {
        this.polymerNet$version = version;
    }

    @Override
    public long polymerNet$lastPacketUpdate(Identifier packet) {
        return this.polymerNet$rateLimits.getLong(packet);
    }

    @Override
    public void polymerNet$savePacketTime(Identifier packet) {
        this.polymerNet$rateLimits.put(packet, System.currentTimeMillis());
    }

    @Override
    public void polymerNet$resetSupported() {
        this.polymerNet$protocolMap.clear();
    }

    @Override
    public int polymerNet$getSupportedVersion(Identifier identifier) {
        return this.polymerNet$protocolMap.getOrDefault(identifier, -1);
    }

    @Override
    public void polymerNet$setSupportedVersion(Identifier identifier, int i) {
        this.polymerNet$protocolMap.put(identifier, i);
    }

    @Override
    public Object2IntMap<Identifier> polymerNet$getSupportMap() {
        return this.polymerNet$protocolMap;
    }

    @Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
    private void polymerNet$catchPackets(CustomPayloadC2SPacket packet, CallbackInfo ci) {
        if (ServerPacketRegistry.handle((ServerPlayNetworkHandler) (Object) this, packet.getChannel(), packet.getData())) {
            this.polymerNet$savePacketTime(packet.getChannel());
            ci.cancel();
        }
    }

    @ModifyVariable(method = "sendPacket(Lnet/minecraft/network/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At("HEAD"))
    private Packet<?> polymerNet$replacePacket(Packet<?> packet) {
        if (packet instanceof DynamicPacket dynamicPacket) {
            var out = dynamicPacket.createPacket((ServerPlayNetworkHandler) (Object) (this), this.player);

            if (out != null) {
                return out;
            }
        }

        return packet;
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At("HEAD"), cancellable = true)
    private void polymerNet$dontLeakDynamic(Packet<?> packet, PacketCallbacks arg, CallbackInfo ci) {
        if (packet instanceof DynamicPacket) {
            ci.cancel();
        }
    }
}
