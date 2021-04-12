package eu.pb4.polymer.mixin.other;

// Based on mixin from PolyMC - https://github.com/TheEpicBlock/PolyMc/blob/master/src/main/java/io/github/theepicblock/polymc/mixins/context/PacketPlayerContextContainer.java

import eu.pb4.polymer.interfaces.PlayerContextInterface;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Modified version of PolyMC mixin - https://github.com/TheEpicBlock/PolyMc/blob/master/src/main/java/io/github/theepicblock/polymc/mixins/context/PacketPlayerContextContainer.java

@Mixin({AdvancementUpdateS2CPacket.class,
        EntityEquipmentUpdateS2CPacket.class,
        InventoryS2CPacket.class,
        ScreenHandlerSlotUpdateS2CPacket.class,
        SynchronizeRecipesS2CPacket.class,
        SetTradeOffersS2CPacket.class,
        EntityTrackerUpdateS2CPacket.class,
        ParticleS2CPacket.class,
        SynchronizeTagsS2CPacket.class})

public class PacketContextMixin implements PlayerContextInterface {
    @Unique
    private ServerPlayerEntity player;

    @Override
    public ServerPlayerEntity getPolymerPlayer() {
        return this.player;
    }

    @Override
    public void setPolymerPlayer(ServerPlayerEntity player) {
        this.player = player;
    }


    @Inject(method = "write(Lnet/minecraft/network/PacketByteBuf;)V", at = @At("HEAD"))
    private void writeInject(PacketByteBuf buf, CallbackInfo ci) {
        ((PlayerContextInterface)buf).setPolymerPlayer(this.player);
    }
}