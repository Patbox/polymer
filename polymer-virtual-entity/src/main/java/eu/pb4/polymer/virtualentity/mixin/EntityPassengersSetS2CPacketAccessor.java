package eu.pb4.polymer.virtualentity.mixin;

import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientboundSetPassengersPacket.class)
public interface EntityPassengersSetS2CPacketAccessor {
    @Mutable
    @Accessor
    void setVehicle(int id);

    @Mutable
    @Accessor
    void setPassengers(int[] passengerIds);
}
