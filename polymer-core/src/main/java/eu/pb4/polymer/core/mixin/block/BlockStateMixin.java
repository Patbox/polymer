package eu.pb4.polymer.core.mixin.block;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.Codec;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.impl.interfaces.BlockStateExtra;
import net.minecraft.block.BlockState;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.function.Function;

@Mixin(BlockState.class)
public abstract class BlockStateMixin implements BlockStateExtra {
    @Shadow protected abstract BlockState asBlockState();

    @Unique
    private boolean polymer$calculatedIsLight;
    @Unique
    private boolean polymer$isLight;

    @Override
    public boolean polymer$isPolymerLightSource() {
        if (this.polymer$calculatedIsLight) {
            return this.polymer$isLight;
        }

        if (this.asBlockState().getBlock() instanceof PolymerBlock polymerBlock) {
            this.polymer$isLight = this.asBlockState().getLuminance() != polymerBlock.getPolymerBlockState(this.asBlockState(), PacketContext.of()).getLuminance();
        }


        this.polymer$calculatedIsLight = true;

        return false;
    }

    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;createCodec(Lcom/mojang/serialization/Codec;Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"))
    private static Codec<BlockState> patchCodec(Codec<BlockState> codec) {
        return codec.xmap(Function.identity(), content -> { // Encode
            if (PolymerCommonUtils.isServerNetworkingThreadWithContext()  && content.getBlock() instanceof PolymerBlock) {
                return PolymerBlockUtils.getPolymerBlockState(content, PacketContext.get());
            }
            return content;
        });
    }
}
