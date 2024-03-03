package eu.pb4.polymer.core.mixin.other;


import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "net/minecraft/network/codec/PacketCodecs$15", priority = 500)
public abstract class PacketCodecsRegistryMixin {

    @Shadow @Final private RegistryKey field_48575;

    @ModifyVariable(method = "encode(Lnet/minecraft/network/RegistryByteBuf;Ljava/lang/Object;)V", at = @At("HEAD"), argsOnly = true)
    private Object polymer$changeData(Object val, RegistryByteBuf buf) {
        var player = PolymerUtils.getPlayerContext();
        
        if (player != null) {
            if (val instanceof PolymerSyncedObject<?> polymerSyncedObject) {
                var obj = polymerSyncedObject.getPolymerReplacement(player);

                if (obj != null) {
                    return obj;
                }
            } else if (val instanceof RegistryEntry<?> registryEntry) {
                var value = registryEntry.value();
                if (value instanceof PolymerSyncedObject<?> polymerSyncedObject) {
                    var obj = polymerSyncedObject.getPolymerReplacement(player);

                    if (obj != null) {
                        //noinspection unchecked
                        return buf.getRegistryManager().get(this.field_48575).getEntry(obj);
                    }
                }
            }
        }

        return val;
    }
}