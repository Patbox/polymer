package eu.pb4.polymer.core.mixin.item.component;

import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.impl.TransformingComponent;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(MapDecorations.class)
public abstract class MapDecorationsMixin implements TransformingComponent {

    @Shadow
    @Final
    private Map<String, MapDecorations.Entry> decorations;

    @Override
    public Object polymer$getTransformed(PacketContext context) {
        if (!polymer$requireModification(context)) {
            return this;
        }

        var transformed = new LinkedHashMap<String, MapDecorations.Entry>();
        for (var entry : this.decorations.entrySet()) {
            var decoration = entry.getValue();
            MapDecorationType type = decoration.type().value();
            var synced = PolymerSyncedObject.getSyncedObject(BuiltInRegistries.MAP_DECORATION_TYPE, type);
            if (synced != null && !synced.canSyncRawToClient(context)) {
                var replacement = BuiltInRegistries.MAP_DECORATION_TYPE.wrapAsHolder(synced.getPolymerReplacement(type, context));
                decoration = new MapDecorations.Entry(replacement, decoration.x(), decoration.z(), decoration.rotation());
            }
            transformed.put(entry.getKey(), decoration);
        }
        return new MapDecorations(transformed);
    }

    @Override
    public boolean polymer$requireModification(PacketContext context) {
        for (var decoration : this.decorations.values()) {
            if (!PolymerSyncedObject.canSyncRawToClient(BuiltInRegistries.MAP_DECORATION_TYPE, decoration.type().value(), context)) {
                return true;
            }
        }
        return false;
    }
}
