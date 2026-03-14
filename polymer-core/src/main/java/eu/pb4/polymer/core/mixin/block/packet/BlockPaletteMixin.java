package eu.pb4.polymer.core.mixin.block.packet;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.IdMap;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.HashMapPalette;
import net.minecraft.world.level.chunk.LinearPalette;
import net.minecraft.world.level.chunk.SingleValuePalette;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

@Mixin(value = {LinearPalette.class, SingleValuePalette.class, HashMapPalette.class}, priority = 500)
public abstract class BlockPaletteMixin {

    @ModifyArg(method = {"write", "getSerializedSize"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/core/IdMap;getId(Ljava/lang/Object;)I"))
    public Object polymer_getIdRedirect(Object object) {
        if (object instanceof BlockState blockState) {
            return PolymerBlockUtils.getPolymerBlockState(blockState, PacketContext.get());
        }
        return object;
    }

    @Environment(EnvType.CLIENT)
    @Redirect(method = "read", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/IdMap;byIdOrThrow(I)Ljava/lang/Object;"))
    private Object polymer_replaceState(IdMap<?> instance, int i) {
        return InternalClientRegistry.decodeRegistry(instance, i);
    }
}