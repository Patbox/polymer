package eu.pb4.polymer.core.mixin.other;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.serialization.Codec;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.HoverEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Optional;
import java.util.function.Function;

@Mixin(Registry.class)
public interface RegistryMixin {
    @Shadow RegistryEntry<Object> getEntry(Object value);

    @Shadow Optional<RegistryEntry.Reference<Object>> getEntry(int rawId);

    @ModifyReturnValue(method = "getReferenceEntryCodec", at = @At(value = "RETURN"))
    private Codec<RegistryEntry.Reference<Object>> patchCodec(Codec<RegistryEntry.Reference<Object>> codec) {
        return codec.xmap(Function.identity(), content -> { // Encode
            if (PolymerCommonUtils.isServerNetworkingThread() && content.hasKeyAndValue()
                    && content.value() instanceof PolymerSyncedObject<?> obj) {
                var ctx = PacketContext.get();
                if (obj.canSyncRawToClient(ctx)) {
                    return content;
                }
                var val = obj.getPolymerReplacement(ctx);
                return val != null && this.getEntry(val) instanceof RegistryEntry.Reference<Object> ref ? ref : this.getEntry(0).orElseThrow();
            }
            return content;
        });
    }
}
