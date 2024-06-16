package eu.pb4.polymer.core.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.Codec;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Objects;
import java.util.function.Function;

@Mixin(EntityAttribute.class)
public abstract class EntityAttributeMixin {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/Registry;getEntryCodec()Lcom/mojang/serialization/Codec;"))
    private static Codec<RegistryEntry<EntityAttribute>> patchCodec(Codec<RegistryEntry<EntityAttribute>> codec) {
        return codec.xmap(Function.identity(), content -> { // Encode
            if (PolymerCommonUtils.isServerNetworkingThread() && PolymerEntityUtils.isPolymerEntityAttribute(content)) {
                return EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS;
            }
            return content;
        });
    }
}
