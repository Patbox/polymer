package eu.pb4.polymer.mixin.compat;

import eu.pb4.polymer.api.block.PolymerBlockUtils;
import me.jellysquid.mods.lithium.common.world.chunk.LithiumHashPalette;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin(value = LithiumHashPalette.class, priority = 500)
public class lithium_BlockPaletteMixin {
    @ModifyArg(method = {"writePacket", "getPacketSize" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/IndexedIterable;getRawId(Ljava/lang/Object;)I"))
    public Object polymer_getIdRedirect(Object object) {
        if (object instanceof BlockState blockState) {
            return PolymerBlockUtils.getPolymerBlockState(blockState);
        }
        return object;
    }
}
