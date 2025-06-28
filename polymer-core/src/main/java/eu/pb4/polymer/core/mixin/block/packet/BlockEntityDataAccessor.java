package eu.pb4.polymer.core.mixin.block.packet;

import net.minecraft.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.network.packet.s2c.play.ChunkData$BlockEntityData")
public interface BlockEntityDataAccessor {
    @Accessor
    BlockEntityType<?> getType();
}
