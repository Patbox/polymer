package eu.pb4.polymer.mixin.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.api.utils.PolymerUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.HoverEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HoverEvent.ItemStackContent.class)
public abstract class ItemStackContentMixin {
    @Shadow public abstract ItemStack asStack();

    @Inject(method = "toJson", at = @At("HEAD"), cancellable = true)
    private void polymer$replaceItem(CallbackInfoReturnable<JsonElement> cir) {
        if (PolymerItemUtils.isPolymerServerItem(this.asStack())) {
            var stack = PolymerItemUtils.getPolymerItemStack(this.asStack(), PolymerUtils.getPlayer());

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", Registries.ITEM.getId(stack.getItem()).toString());
            if (stack.getCount() != 1) {
                jsonObject.addProperty("count", stack.getCount());
            }

            if (stack.hasNbt()) {
                jsonObject.addProperty("tag", stack.getNbt().toString());
            }

            cir.setReturnValue(jsonObject);
        }
    }
}
