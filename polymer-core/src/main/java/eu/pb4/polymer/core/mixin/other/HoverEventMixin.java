package eu.pb4.polymer.core.mixin.other;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.Codec;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Objects;
import java.util.function.Function;

@Mixin(HoverEvent.class)
public abstract class HoverEventMixin {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;xmap(Ljava/util/function/Function;Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"))
    private static Codec<HoverEvent> patchCodec(Codec<HoverEvent> codec) {
        return codec.xmap(Function.identity(), content -> { // Encode
            if (PolymerCommonUtils.isServerNetworkingThread()) {
                if (content.getAction() == HoverEvent.Action.SHOW_ENTITY) {
                    var val = Objects.requireNonNull(content.getValue(HoverEvent.Action.SHOW_ENTITY));
                    if (PolymerEntityUtils.isPolymerEntityType(val.entityType)) {
                        val = new HoverEvent.EntityContent(val.entityType, val.uuid, val.name);
                        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, Texts.join(val.asTooltip(), Text.literal("\n")));
                    }
                }
            }
            return content;
        });
    }
}
