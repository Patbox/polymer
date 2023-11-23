package eu.pb4.polymer.common.mixin;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.common.impl.CommonClientConnectionExt;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin implements CommonClientConnectionExt {
    @Shadow @Nullable public abstract PacketListener getPacketListener();

    @Unique
    private final Object2BooleanOpenHashMap<UUID> polymerCommon$hasResourcePack = new Object2BooleanOpenHashMap<>();

    @Override
    public boolean polymerCommon$hasResourcePack(UUID uuid) {
        return this.polymerCommon$hasResourcePack.getBoolean(uuid);
    }

    @Override
    public void polymerCommon$setResourcePack(UUID uuid, boolean value) {
        var old = this.polymerCommon$hasResourcePack.put(uuid, value);

        if (this.getPacketListener() instanceof ServerCommonNetworkHandler handler) {
            PolymerCommonUtils.ON_RESOURCE_PACK_STATUS_CHANGE.invoke(x -> x.onResourcePackChange(handler, uuid, old, value));
        }
    }

    @Override
    public void polymerCommon$setResourcePackNoEvent(UUID uuid, boolean value) {
        this.polymerCommon$hasResourcePack.put(uuid, value);
    }
}
