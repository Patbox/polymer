package eu.pb4.polymer.mixin.client.item.packet;

import eu.pb4.polymer.api.utils.PolymerObject;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.recipe.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(SynchronizeRecipesS2CPacket.class)
public abstract class SynchronizeRecipesS2CPacketMixin {
    @Unique List<Recipe<?>> rewrittenRecipes = null;

    @Shadow @Mutable
    private List<Recipe<?>> recipes;

    @Shadow public abstract void write(PacketByteBuf buf);

    @Inject(method = "write", at = @At("HEAD"))
    public void polymer_onWrite(PacketByteBuf buf, CallbackInfo callbackInfo) {
            recipes = recipes.stream()
                    .filter(recipe -> !(recipe.getSerializer() instanceof PolymerObject || recipe instanceof PolymerObject))
                    .collect(Collectors.toList());
    }

    /*
     * This is a hack, but I didn't have any idea how to do it in better way
     * Well, I could do it manually but I'm too lazy to make that + this one works just fine
     * This packet isn't spammed anyway so
     */
    @Environment(EnvType.CLIENT)
    @Inject(method = "getRecipes", at = @At("HEAD"), cancellable = true)
    private void polymer_replaceRecipesOnClient(CallbackInfoReturnable<List<Recipe<?>>> cir) {
        if (this.rewrittenRecipes == null) {
            try {
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                this.write(buf);
                this.rewrittenRecipes = ((SynchronizeRecipesS2CPacketAccessor) new SynchronizeRecipesS2CPacket(buf)).polymer_getRecipes();
            } catch (Exception e) {
                this.rewrittenRecipes = Collections.emptyList();
            }
        }

        cir.setReturnValue(this.rewrittenRecipes);
    }
}
