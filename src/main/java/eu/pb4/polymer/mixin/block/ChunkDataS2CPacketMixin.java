package eu.pb4.polymer.mixin.block;

import eu.pb4.polymer.PolymerMod;
import eu.pb4.polymer.interfaces.ChunkDataS2CPacketInterface;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChunkDataS2CPacket.class)
public class ChunkDataS2CPacketMixin implements ChunkDataS2CPacketInterface {
    @Unique
    private WorldChunk worldChunk;

    @Inject(method = "<init>(Lnet/minecraft/world/chunk/WorldChunk;)V", at = @At("TAIL"))
    private void storeWorldChunk(WorldChunk chunk, CallbackInfo ci) {
        this.worldChunk = chunk;
    }

    @Redirect(method = "<init>(Lnet/minecraft/world/chunk/WorldChunk;)V", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private boolean dontAddVirtualBlockEntities(List<NbtCompound> list, Object e) {
        NbtCompound tag = (NbtCompound) e;

        if (!PolymerMod.isVirtualBlockEntity(tag.getString("Id"))) {
            return list.add(tag);
        }
        return false;
    }

    public WorldChunk getWorldChunk() {
        return this.worldChunk;
    }
}
