package eu.pb4.polymer.core.mixin.item.component;

import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.impl.TransformingComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.component.DebugStickState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;

@Mixin(DebugStickState.class)
public class DebugStickStateMixin implements TransformingComponent {
    @Shadow @Final private Map<Holder<Block>, Property<?>> properties;

    @Override
    public Object polymer$getTransformed(PacketContext context) {
        if (polymer$requireModification(context)) {
            return DebugStickState.EMPTY;
        }
        return this;
    }

    @Override
    public boolean polymer$requireModification(PacketContext context) {
        for (var key : this.properties.keySet()) {
            if (!PolymerSyncedObject.canSyncRawToClient(BuiltInRegistries.BLOCK, key.value(), context)) {
                return true;
            }
        }
        return false;
    }
}
