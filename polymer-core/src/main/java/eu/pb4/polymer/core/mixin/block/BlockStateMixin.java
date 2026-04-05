package eu.pb4.polymer.core.mixin.block;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.Codec;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.impl.interfaces.BlockStateExtra;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.function.Function;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(BlockState.class)
public abstract class BlockStateMixin implements BlockStateExtra {
    @Shadow protected abstract BlockState asState();

    @Unique
    private boolean polymer$calculatedIsLight;
    @Unique
    private boolean polymer$isLight;

    @Override
    public boolean polymer$isPolymerLightSource() {
        if (this.polymer$calculatedIsLight) {
            return this.polymer$isLight;
        }

        if (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.BLOCK, this.asState().getBlock()) instanceof PolymerBlock polymerBlock) {
            this.polymer$isLight = this.asState().getLightEmission() != polymerBlock.getPolymerBlockState(this.asState(), null).getLightEmission() || polymerBlock.forceLightUpdates(this.asState());
        }

        this.polymer$calculatedIsLight = true;
        return false;
    }

    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;codec(Lcom/mojang/serialization/Codec;Ljava/util/function/Function;Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"))
    private static Codec<BlockState> patchCodec(Codec<BlockState> codec) {
        return codec.xmap(Function.identity(), content -> { // Encode
            if (PolymerCommonUtils.isServerNetworkingThread() && PolymerSyncedObject.getSyncedObject(BuiltInRegistries.BLOCK, content.getBlock()) != null) {
                return PolymerBlockUtils.getPolymerBlockState(content, PacketContext.orElseThrow());
            }
            return content;
        });
    }
}
