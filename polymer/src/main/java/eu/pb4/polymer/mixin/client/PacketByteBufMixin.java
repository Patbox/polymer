package eu.pb4.polymer.mixin.client;

import eu.pb4.polymer.impl.client.InternalClientRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.collection.IndexedIterable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(PacketByteBuf.class)
public class PacketByteBufMixin {
    @Redirect(method = "readRegistryValue", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/IndexedIterable;get(I)Ljava/lang/Object;"))
    private Object polymer$clientRemapToIds(IndexedIterable instance, int i) {
        return InternalClientRegistry.decodeRegistry(instance, i);
    }
}
