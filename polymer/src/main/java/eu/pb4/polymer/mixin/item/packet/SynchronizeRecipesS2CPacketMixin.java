package eu.pb4.polymer.mixin.item.packet;

import eu.pb4.polymer.api.utils.PolymerObject;
import eu.pb4.polymer.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.impl.client.ClientUtils;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(SynchronizeRecipesS2CPacket.class)
public abstract class SynchronizeRecipesS2CPacketMixin implements Packet {
    @Unique List<Recipe<?>> polymer$clientRewrittenRecipes = null;

    @Shadow public abstract void write(PacketByteBuf buf);

    @Shadow @Final private List<Recipe<?>> recipes;

    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeCollection(Ljava/util/Collection;Lnet/minecraft/network/PacketByteBuf$PacketWriter;)V"))
    public Collection<Recipe<?>> polymer$remapRecipes(Collection<Recipe<?>> recipes) {
        List<Recipe<?>> list = new ArrayList<>();
        var player = PolymerUtils.getPlayer();
        for (Recipe<?> recipe : recipes) {
            if (recipe instanceof PolymerSyncedObject<?> syncedRecipe) {
                Recipe<?> polymerRecipe = (Recipe<?>) syncedRecipe.getPolymerReplacement(player);
                if (polymerRecipe != null) {
                    list.add(polymerRecipe);
                }
            } else if (!(PolymerObject.is(recipe.getSerializer()) || PolymerObject.is(recipe))) {
                list.add(recipe);
            }
        }
        return list;
    }

    /*
     * This is a hack, which converts all itemStack to client side version while running on singleplayer,
     * It's not great but I didn't have any better idea how to do it in better way
     * I could do it manually but it would require way more work + this one works just fine
     */
    @Environment(EnvType.CLIENT)
    @Inject(method = "getRecipes", at = @At("HEAD"), cancellable = true)
    private void polymer$replaceOnClient(CallbackInfoReturnable<List<Recipe<?>>> cir) {
        if (ClientUtils.isSingleplayer()) {
            if (this.polymer$clientRewrittenRecipes == null) {
                this.polymer$clientRewrittenRecipes = new ArrayList<>();

                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

                for (var recipe : this.recipes) {
                    buf.clear();
                    try {
                        ((RecipeSerializer<Recipe<?>>) recipe.getSerializer()).write(buf, recipe);
                        this.polymer$clientRewrittenRecipes.add(recipe.getSerializer().read(recipe.getId(), buf));
                    } catch (Throwable e) { // Ofc some mods have weird issues with their serializers, because why not
                        this.polymer$clientRewrittenRecipes.add(recipe);
                    }
                }
            }
            cir.setReturnValue(this.polymer$clientRewrittenRecipes);
        }
    }


    @Override
    public boolean isWritingErrorSkippable() {
        return true;
    }
}
