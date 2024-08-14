package eu.pb4.polymer.virtualentity.mixin.accessors;

import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityPositionS2CPacket.class)
public interface EntityPositionS2CPacketAccessor {
    @Mutable
    @Accessor("entityId")
    void setId(int id);

    @Mutable
    @Accessor("x")
    void setX(double val);

    @Mutable
    @Accessor("y")
    void setY(double val);

    @Mutable
    @Accessor("z")
    void setZ(double val);

    @Mutable
    @Accessor("yaw")
    void setYaw(byte val);

    @Mutable
    @Accessor("pitch")
    void setPitch(byte val);

    @Mutable
    @Accessor("onGround")
    void setOnGround(boolean val);
}
