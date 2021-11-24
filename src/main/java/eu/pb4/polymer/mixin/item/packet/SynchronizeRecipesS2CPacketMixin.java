package eu.pb4.polymer.mixin.item.packet;

import eu.pb4.polymer.api.item.PolymerRecipe;
import eu.pb4.polymer.api.utils.PolymerObject;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.recipe.Recipe;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mixin(SynchronizeRecipesS2CPacket.class)
public abstract class SynchronizeRecipesS2CPacketMixin {
    @Unique List<Recipe<?>> rewrittenRecipes = null;

    @Final @Shadow @Mutable private List<Recipe<?>> recipes;

    @Shadow public abstract void write(PacketByteBuf buf);

    @Inject(method = "<init>(Ljava/util/Collection;)V", at = @At("TAIL"))
    public void polymer_onInit(Collection<Recipe<?>> recipes, CallbackInfo ci) {
        List<Recipe<?>> list = new ArrayList<>();
        for (Recipe<?> recipe : recipes) {
            if (recipe instanceof PolymerRecipe) {
                Recipe<?>  polymerRecipe = ((PolymerRecipe) recipe).getPolymerRecipe(recipe);
                if (polymerRecipe != null) {
                    list.add(polymerRecipe);
                    continue;
                }
            }
            if (!(PolymerObject.is(recipe.getSerializer()) || PolymerObject.is(recipe))) {
                list.add(recipe);
            }
        }
        this.recipes = list;
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
