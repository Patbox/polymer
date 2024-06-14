package eu.pb4.polymer.core.mixin.other;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Codec;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Mixin(ComponentMap.class)
public interface ComponentMapMixin {
    @ModifyVariable(method = "createCodecFromValueMap", at = @At("HEAD"), argsOnly = true)
    private static Codec<Map<ComponentType<?>, Object>> patchCodec(Codec<Map<ComponentType<?>, Object>> codec) {
        return codec.xmap(Function.identity(), content -> { // Encode
            if (PolymerCommonUtils.isServerNetworkingThread()) {
                var player = PacketContext.get();

                var map = new IdentityHashMap<ComponentType<?>, Object>();
                for (var key : content.keySet()) {
                    var entry = content.get(key);
                    if (PolymerComponent.canSync(key, entry, player)) {
                        map.put(key, entry);
                    }
                }

                return map;
            }
            return content;
        });
    }
}
