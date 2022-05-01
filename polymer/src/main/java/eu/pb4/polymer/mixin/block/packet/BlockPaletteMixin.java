package eu.pb4.polymer.mixin.block.packet;

import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.impl.PolymerImplUtils;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.world.chunk.ArrayPalette;
import net.minecraft.world.chunk.BiMapPalette;
import net.minecraft.world.chunk.SingularPalette;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = {ArrayPalette.class, SingularPalette.class, BiMapPalette.class}, priority = 500)
public abstract class BlockPaletteMixin {

    @ModifyArg(method = {"writePacket", "getPacketSize"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/IndexedIterable;getRawId(Ljava/lang/Object;)I"))
    public Object polymer_getIdRedirect(Object object) {
        if (object instanceof BlockState blockState) {
            return PolymerBlockUtils.getPolymerBlockState(blockState, PolymerImplUtils.getPlayer());
        }
        return object;
    }

    @Environment(EnvType.CLIENT)
    @Redirect(method = "readPacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/IndexedIterable;getOrThrow(I)Ljava/lang/Object;"))
    private Object polymer_replaceState(IndexedIterable<?> instance, int i) {
        if (instance == Block.STATE_IDS) {
            return InternalClientRegistry.decodeState(i);
        }

        return instance.getOrThrow(i);
    }
}