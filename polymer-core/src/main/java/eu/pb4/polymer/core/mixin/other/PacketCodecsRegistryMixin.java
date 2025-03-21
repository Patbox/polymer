package eu.pb4.polymer.core.mixin.other;


import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(targets = "net/minecraft/network/codec/PacketCodecs$18", priority = 500)
public abstract class PacketCodecsRegistryMixin {
    @SuppressWarnings({"rawtypes", "ShadowModifiers"})
    @Shadow
    @Final
    private RegistryKey field_53746;

    @ModifyVariable(method = "encode(Lnet/minecraft/network/RegistryByteBuf;Ljava/lang/Object;)V", at = @At("HEAD"), argsOnly = true)
    private Object polymer$changeData(Object val, RegistryByteBuf buf) {
        var player = PacketContext.get();
        //noinspection unchecked
        var reg = buf.getRegistryManager().getOrThrow(this.field_53746);


        if (val instanceof RegistryEntry<?> registryEntry) {
            var value = registryEntry.value();
            var obj = PolymerSyncedObject.getSyncedObject(reg, value);

            if (obj != null) {
                var replacement = obj.getPolymerReplacement(player);

                if (replacement != null) {
                    //noinspection unchecked
                    return reg.getEntry(replacement);
                }
            }
        } else {
            var obj = PolymerSyncedObject.getSyncedObject(reg, val);
            if (obj != null) {
                var replacement = obj.getPolymerReplacement(player);

                if (replacement != null) {
                    return replacement;
                }
            }
        }

        return val;
    }
}