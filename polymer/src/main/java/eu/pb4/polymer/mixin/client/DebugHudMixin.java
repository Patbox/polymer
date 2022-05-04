package eu.pb4.polymer.mixin.client;

import eu.pb4.polymer.api.client.PolymerClientUtils;
import eu.pb4.polymer.api.client.registry.ClientPolymerBlock;
import eu.pb4.polymer.impl.PolymerImpl;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(DebugHud.class)
public abstract class DebugHudMixin {
    @Shadow private HitResult blockHit;

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "getRightText", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 2), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void polymer_replaceBlockString(CallbackInfoReturnable<List<String>> cir, long l, long m, long n, long o, List<String> list) {
        if (this.blockHit.getType() == HitResult.Type.BLOCK && InternalClientRegistry.enabled) {
            var blockPos = ((BlockHitResult)this.blockHit).getBlockPos();
            var block = InternalClientRegistry.getBlockAt(blockPos);
            var worldState = this.client.world.getBlockState(blockPos);
            if (block != ClientPolymerBlock.NONE_STATE && block.blockState() != worldState) {
                list.add(block.block().identifier().toString());
                for (var entry : block.states().entrySet()) {
                    list.add(entry.getKey() + ": " + switch (entry.getValue()) {
                        case "true" -> Formatting.GREEN + "true";
                        case "false" -> Formatting.RED + "false";
                        default -> entry.getValue();
                    });
                }
                list.add("");
                list.add(Formatting.UNDERLINE + "Targeted Client Block: " + blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ());
            }
        }
    }

    @Inject(method = "getRightText", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 9), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void polymer_replaceEntityString(CallbackInfoReturnable<List<String>> cir, long l, long m, long n, long o, List<String> list) {
        if (this.client.targetedEntity != null && InternalClientRegistry.enabled) {
            var type = PolymerClientUtils.getEntityType(this.client.targetedEntity);

            if (type != null) {
                list.add(type.identifier().toString());
                list.add("");
                list.add(Formatting.UNDERLINE + "Targeted Client Entity");
            }
        }
    }

    @Inject(method = "getLeftText", at = @At("RETURN"))
    private void polymer_debugText(CallbackInfoReturnable<List<String>> cir) {
        if (InternalClientRegistry.enabled && PolymerImpl.DISPLAY_DEBUG_INFO_CLIENT) {
            var list = cir.getReturnValue();

            list.add(InternalClientRegistry.debugServerInfo);
            list.add(InternalClientRegistry.debugRegistryInfo);
        }
    }
}
