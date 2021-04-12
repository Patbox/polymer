package eu.pb4.polymer.mixin.other;

import eu.pb4.polymer.interfaces.PlayerContextInterface;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PacketByteBuf.class)
public class PacketByteBufContextMixin implements PlayerContextInterface {
    @Unique ServerPlayerEntity player;

    @Override
    public void setPolymerPlayer(ServerPlayerEntity player) {
        this.player = player;
    }

    @Override
    public ServerPlayerEntity getPolymerPlayer() {
        return this.player;
    }
}
