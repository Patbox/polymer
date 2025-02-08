package eu.pb4.polymer.core.mixin.client.rendering;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.registry.Registries;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BakedModelManager.class)
public class BakedModelManagerMixin {
    @WrapWithCondition(method = "method_65752", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V"))
    private static boolean preventWarningForPolymerBlocks(Logger instance, String s, Object object, @Local(argsOnly = true) BlockState state) {
        return !(PolymerSyncedObject.getSyncedObject(Registries.BLOCK, state.getBlock()) instanceof PolymerBlock);
    }
}
