package eu.pb4.polymer.core.mixin.other;


import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(targets = "net/minecraft/network/codec/ByteBufCodecs$29", priority = 500)
public abstract class ByteBufCodecsRegistryMixin {

    @Shadow @Final private ResourceKey val$registryKey;

    @SuppressWarnings({"rawtypes", "ShadowModifiers"})

    @ModifyVariable(method = "encode(Lnet/minecraft/network/RegistryFriendlyByteBuf;Ljava/lang/Object;)V", at = @At("HEAD"), argsOnly = true)
    private Object polymer$changeData(Object val, RegistryFriendlyByteBuf buf) {
        var player = PacketContext.get();
        //noinspection unchecked
        var reg = buf.registryAccess().lookupOrThrow(this.val$registryKey);


        if (val instanceof Holder<?> registryEntry) {
            var value = registryEntry.value();
            var obj = PolymerSyncedObject.getSyncedObject(reg, value);

            if (obj != null) {
                var replacement = obj.getPolymerReplacement(value, player);

                if (replacement != null) {
                    //noinspection unchecked
                    return reg.wrapAsHolder(replacement);
                }

                return reg.get(0);
            }
        } else {
            var obj = PolymerSyncedObject.getSyncedObject(reg, val);
            if (obj != null) {
                var replacement = obj.getPolymerReplacement(val, player);

                if (replacement != null) {
                    return replacement;
                }
                return reg.byId(0);
            }
        }

        return val;
    }
}