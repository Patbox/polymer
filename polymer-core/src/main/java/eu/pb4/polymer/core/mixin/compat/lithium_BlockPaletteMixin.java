package eu.pb4.polymer.core.mixin.compat;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import me.jellysquid.mods.lithium.common.world.chunk.LithiumHashPalette;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.util.collection.IndexedIterable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.nucleoid.packettweaker.PacketContext;

@Pseudo
@Mixin(value = LithiumHashPalette.class, priority = 500)
public class lithium_BlockPaletteMixin {
    @ModifyArg(method = {"writePacket", "getPacketSize" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/IndexedIterable;getRawId(Ljava/lang/Object;)I"))
    public Object polymer$getIdRedirect(Object object) {
        if (object instanceof BlockState blockState) {
            return PolymerBlockUtils.getPolymerBlockState(blockState, PacketContext.get());
        }
        return object;
    }

    @Environment(EnvType.CLIENT)
    @Redirect(method = "readPacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/IndexedIterable;get(I)Ljava/lang/Object;"), require = 0)
    private Object polymer$replaceState(IndexedIterable<?> instance, int index) {
        return InternalClientRegistry.decodeRegistry(instance, index);
    }
}
