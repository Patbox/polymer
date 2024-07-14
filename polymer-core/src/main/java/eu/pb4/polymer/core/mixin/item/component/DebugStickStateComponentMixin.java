package eu.pb4.polymer.core.mixin.item.component;

import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.impl.TransformingComponent;
import net.minecraft.block.Block;
import net.minecraft.component.type.DebugStickStateComponent;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Map;

@Mixin(DebugStickStateComponent.class)
public class DebugStickStateComponentMixin implements TransformingComponent {
    @Shadow @Final private Map<RegistryEntry<Block>, Property<?>> properties;

    @Override
    public Object polymer$getTransformed(PacketContext context) {
        if (polymer$requireModification(context)) {
            return DebugStickStateComponent.DEFAULT;
        }
        return this;
    }

    @Override
    public boolean polymer$requireModification(PacketContext context) {
        for (var key : this.properties.keySet()) {
            if (!PolymerSyncedObject.canSyncRawToClient(Registries.BLOCK, key.value(), context.getPlayer())) {
                return true;
            }
        }
        return false;
    }
}
