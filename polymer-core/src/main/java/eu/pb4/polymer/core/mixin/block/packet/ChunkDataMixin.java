package eu.pb4.polymer.core.mixin.block.packet;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import java.util.List;
import java.util.Map;

@Mixin(ChunkData.class)
public class ChunkDataMixin {
    @WrapWithCondition(method = "<init>(Lnet/minecraft/world/chunk/WorldChunk;)V", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private boolean skipPolymerEntriesForBedrock(List<?> instance, Object e, @Local Map.Entry<BlockPos, BlockEntity> entry) {
        return !PolymerCommonUtils.isBedrockPlayer(PolymerCommonUtils.getPlayerContext()) || !PolymerBlockUtils.isPolymerBlockEntityType(entry.getValue().getType());
    }
}
