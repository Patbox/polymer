package eu.pb4.polymer.core.mixin.entity;

import net.minecraft.network.packet.s2c.play.PlayerSpawnS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(PlayerSpawnS2CPacket.class)
public interface PlayerSpawnS2CPacketAccessor {
    @Mutable
    @Accessor
    void setId(int id);

    @Mutable
    @Accessor
    void setUuid(UUID uuid);

    @Mutable
    @Accessor("x")
    void setX(double x);

    @Mutable
    @Accessor("y")
    void setY(double y);

    @Mutable
    @Accessor("z")
    void setZ(double z);

    @Mutable
    @Accessor
    void setYaw(byte yaw);

    @Mutable
    @Accessor
    void setPitch(byte pitch);
}
