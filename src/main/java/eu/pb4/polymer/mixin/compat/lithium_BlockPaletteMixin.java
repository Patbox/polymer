package eu.pb4.polymer.mixin.compat;

import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.api.client.PolymerClientDecoded;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import me.jellysquid.mods.lithium.common.world.chunk.LithiumHashPalette;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.collection.IndexedIterable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

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

    @Environment(EnvType.CLIENT)
    @Redirect(method = "readPacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/IndexedIterable;get(I)Ljava/lang/Object;"))
    private Object polymer_replaceState(IndexedIterable<?> instance, int i) {
        if (instance == Block.STATE_IDS && i > PolymerBlockUtils.BLOCK_STATE_OFFSET) {
            var state = InternalClientRegistry.BLOCK_STATES.get(i - PolymerBlockUtils.BLOCK_STATE_OFFSET);

            if (state != null && state.realServerBlockState() != null && PolymerClientDecoded.checkDecode(state.realServerBlockState().getBlock())) {
                return state.realServerBlockState();
            }
        }

        return instance.get(i);
    }
}
