package eu.pb4.polymer.mixin.block.packet;

import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.api.utils.PolymerObject;
import eu.pb4.polymer.impl.interfaces.ChunkDataS2CPacketInterface;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mixin(ChunkDataS2CPacket.class)
public class ChunkDataS2CPacketMixin implements ChunkDataS2CPacketInterface {
    @Unique
    private WorldChunk polymer_worldChunk;

    @Unique
    private Packet<?>[] polymer_attachedPackets = null;

    @Inject(method = "<init>(Lnet/minecraft/world/chunk/WorldChunk;)V", at = @At("TAIL"))
    private void polymer_storeWorldChunk(WorldChunk chunk, CallbackInfo ci) {
        this.polymer_worldChunk = chunk;
    }

    @Redirect(method = "<init>(Lnet/minecraft/world/chunk/WorldChunk;)V", at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;"))
    private Set<Map.Entry<BlockPos, BlockEntity>> polymer_dontAddVirtualBlockEntities(Map<BlockPos, BlockEntity> map) {
        Set<Map.Entry<BlockPos, BlockEntity>> blockEntities = new HashSet<>();

        for (var entry : map.entrySet()) {
            if (!(entry.getValue() instanceof PolymerObject) && !PolymerBlockUtils.isRegisteredBlockEntity(entry.getValue().getType())) {
                blockEntities.add(entry);
            }
        }

        return blockEntities;
    }

    public WorldChunk polymer_getWorldChunk() {
        return this.polymer_worldChunk;
    }

    @Override
    public Packet<?>[] polymer_getPolymerSyncPackets() {
        return this.polymer_attachedPackets;
    }

    @Override
    public void polymer_setPolymerSyncPackets(Packet<?>[] packets) {
        this.polymer_attachedPackets = packets;
    }
}
