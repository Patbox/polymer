package eu.pb4.polymer.mixin.client;

import eu.pb4.polymer.api.client.registry.ClientPolymerBlock;
import eu.pb4.polymer.impl.PolymerMod;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugHud.class)
public class DebugHudMixin {
    @Shadow private HitResult blockHit;

    @Inject(method = "getRightText", at = @At("RETURN"))
    private void polymer_replaceString(CallbackInfoReturnable<List<String>> cir) {
        if (this.blockHit.getType() == HitResult.Type.BLOCK) {
            var block = InternalClientRegistry.getBlockAt(((BlockHitResult)this.blockHit).getBlockPos());
            if (block != ClientPolymerBlock.NONE_STATE && block.block() != null) {
                var list = cir.getReturnValue();
                list.add("");
                list.add(Formatting.UNDERLINE + "Polymer Block");
                list.add(block.block().identifier().toString());
                for (var entry : block.states().entrySet()) {
                    list.add(entry.getKey() + ": " + entry.getValue());
                }
            }
        }
    }

    @Inject(method = "getLeftText", at = @At("RETURN"))
    private void polymer_debugText(CallbackInfoReturnable<List<String>> cir) {
        var list = cir.getReturnValue();

        if (InternalClientRegistry.ENABLED) {
            list.add(String.format("[Polymer] C: %s, S: %s", PolymerMod.VERSION, InternalClientRegistry.SERVER_VERSION));
            list.add(String.format("[Polymer] I: %s, IG: %s, B: %s, BS: %s, E: %s", InternalClientRegistry.ITEMS.size(), InternalClientRegistry.ITEM_GROUPS.size(), InternalClientRegistry.BLOCKS.size(), InternalClientRegistry.BLOCK_STATES.size(), InternalClientRegistry.ENTITY_TYPE.size()));
        }
    }
}
