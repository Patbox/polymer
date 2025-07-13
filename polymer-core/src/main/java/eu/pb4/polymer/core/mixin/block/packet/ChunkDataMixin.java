package eu.pb4.polymer.core.mixin.block.packet;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.Map;

@Mixin(ChunkData.class)
public class ChunkDataMixin {
    @WrapWithCondition(method = "<init>(Lnet/minecraft/world/chunk/WorldChunk;)V", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private boolean skipPolymerEntries(List<?> instance, Object e) {
        return !(PolymerSyncedObject.getSyncedObject(Registries.BLOCK_ENTITY_TYPE, ((BlockEntityDataAccessor) e).getType()) instanceof PolymerSyncedObject<BlockEntityType<?>> obj
                && obj.getPolymerReplacement(((BlockEntityDataAccessor) e).getType(), PacketContext.get()) == null);
    }
}
