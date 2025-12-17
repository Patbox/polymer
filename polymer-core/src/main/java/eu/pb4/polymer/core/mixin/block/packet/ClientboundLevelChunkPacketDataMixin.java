package eu.pb4.polymer.core.mixin.block.packet;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.block.entity.BlockEntityType;

@Mixin(ClientboundLevelChunkPacketData.class)
public class ClientboundLevelChunkPacketDataMixin {
    @WrapWithCondition(method = "<init>(Lnet/minecraft/world/level/chunk/LevelChunk;)V", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private boolean skipPolymerEntries(List<?> instance, Object e) {
        return !(PolymerSyncedObject.getSyncedObject(BuiltInRegistries.BLOCK_ENTITY_TYPE, ((BlockEntityInfoAccessor) e).getType()) instanceof PolymerSyncedObject<BlockEntityType<?>> obj
                && obj.getPolymerReplacement(((BlockEntityInfoAccessor) e).getType(), PacketContext.get()) == null);
    }
}
