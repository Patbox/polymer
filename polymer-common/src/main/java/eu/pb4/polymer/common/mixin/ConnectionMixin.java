package eu.pb4.polymer.common.mixin;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.common.impl.CommonConnectionExt;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import java.util.HashSet;
import java.util.UUID;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;

@Mixin(Connection.class)
public abstract class ConnectionMixin implements CommonConnectionExt {
    @Shadow @Nullable public abstract PacketListener getPacketListener();

    @Unique
    private final HashSet<UUID> polymerCommon$hasResourcePack = new HashSet<>();

    @Override
    public boolean polymerCommon$hasResourcePack(UUID uuid) {
        return this.polymerCommon$hasResourcePack.contains(uuid);
    }

    @Override
    public void polymerCommon$setResourcePack(UUID uuid, boolean value) {
        var old = value ? !this.polymerCommon$hasResourcePack.add(uuid) : this.polymerCommon$hasResourcePack.remove(uuid);

        if (this.getPacketListener() instanceof ServerCommonPacketListenerImpl handler && old != value) {
            PolymerCommonUtils.ON_RESOURCE_PACK_STATUS_CHANGE.invoke(x -> x.onResourcePackChange(handler, uuid, old, value));
        }
    }

    @Override
    public void polymerCommon$setResourcePackNoEvent(UUID uuid, boolean value) {
        if (value) {
            this.polymerCommon$hasResourcePack.add(uuid);
        } else {
            this.polymerCommon$hasResourcePack.remove(uuid);
        }
    }
}
