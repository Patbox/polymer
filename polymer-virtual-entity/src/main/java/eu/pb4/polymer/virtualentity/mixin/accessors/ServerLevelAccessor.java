package eu.pb4.polymer.virtualentity.mixin.accessors;

import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.atomic.AtomicInteger;

@Mixin(ServerLevel.class)
public interface ServerLevelAccessor {
    @Accessor("ENTITY_COUNTER")
    static AtomicInteger polymer$getENTITY_COUNTER() {
        throw new UnsupportedOperationException();
    }
}
