package eu.pb4.polymer.core.mixin.other;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.serialization.Codec;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;

@Mixin(Registry.class)
public interface RegistryMixin {
    @Shadow Holder<Object> wrapAsHolder(Object value);

    @Shadow Optional<Holder.Reference<Object>> get(int rawId);

    @ModifyReturnValue(method = "referenceHolderWithLifecycle", at = @At(value = "RETURN"))
    private Codec<Holder.Reference<Object>> patchCodec(Codec<Holder.Reference<Object>> codec) {
        return codec.xmap(Function.identity(), content -> { // Encode
            if (PolymerCommonUtils.isServerNetworkingThread() && content.isBound()
                    && content.value() instanceof PolymerSyncedObject<?> obj) {
                var ctx = PacketContext.get();
                if (obj.canSyncRawToClient(ctx)) {
                    return content;
                }
                //noinspection unchecked
                var val = ((PolymerSyncedObject<Object>) obj).getPolymerReplacement(content.value(), ctx);
                return val != null && this.wrapAsHolder(val) instanceof Holder.Reference<Object> ref ? ref : this.get(0).orElseThrow();
            }
            return content;
        });
    }
}
