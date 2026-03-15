package eu.pb4.polymer.core.mixin.client.item;

import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Environment(EnvType.CLIENT)
@Mixin(ItemStack.class)
public class ItemStackMixin {

    @ModifyArg(method = "addDetailsToTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;literal(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;", ordinal = 0))
    private String polymer$changeId(String id) {
        var identifier = PolymerItemUtils.getPolymerIdentifier((ItemStack) (Object) this);

        return identifier != null ? identifier.toString() : id;
    }
}
