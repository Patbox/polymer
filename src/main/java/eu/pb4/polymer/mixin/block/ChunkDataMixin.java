package eu.pb4.polymer.mixin.block;

import eu.pb4.polymer.block.BlockHelper;
import eu.pb4.polymer.interfaces.VirtualObject;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mixin(ChunkData.class)
public class ChunkDataMixin {

    @Redirect(method = "<init>(Lnet/minecraft/world/chunk/WorldChunk;)V", at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;"))
    private Set<Map.Entry<BlockPos, BlockEntity>> polymer_dontAddVirtualBlockEntities(Map<BlockPos, BlockEntity> map) {
        Set<Map.Entry<BlockPos, BlockEntity>> blockEntities = new HashSet<>();

        for (var entry : map.entrySet()) {
            if (!(entry.getValue() instanceof VirtualObject) && !BlockHelper.isVirtualBlockEntity(entry.getValue().getType())) {
                blockEntities.add(entry);
            }
        }

        return blockEntities;
    }
}
