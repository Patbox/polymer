package eu.pb4.polymer.virtualentity.mixin.accessors;

import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.server.level.ServerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerEntity.class)
public interface ServerEntityAccessor {
    @Accessor
    VecDeltaCodec getPositionCodec();
}
