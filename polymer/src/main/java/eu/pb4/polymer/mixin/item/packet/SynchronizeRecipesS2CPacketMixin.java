package eu.pb4.polymer.mixin.item.packet;

import eu.pb4.polymer.api.item.PolymerRecipe;
import eu.pb4.polymer.api.utils.PolymerObject;
import eu.pb4.polymer.api.utils.PolymerUtils;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.recipe.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mixin(SynchronizeRecipesS2CPacket.class)
public abstract class SynchronizeRecipesS2CPacketMixin implements Packet {
    @Unique List<Recipe<?>> polymer_clientRewrittenRecipes = null;

    @Shadow public abstract void write(PacketByteBuf buf);

    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeCollection(Ljava/util/Collection;Lnet/minecraft/network/PacketByteBuf$PacketWriter;)V"))
    public Collection<Recipe<?>> polymer_onWrite(Collection<Recipe<?>> recipes) {
        List<Recipe<?>> list = new ArrayList<>();
        var player = PolymerUtils.getPlayer();
        for (Recipe<?> recipe : recipes) {
            if (recipe instanceof PolymerRecipe) {
                Recipe<?> polymerRecipe = ((PolymerRecipe) recipe).getPolymerRecipe(recipe, player);
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
    private void polymer_replaceRecipesOnClient(CallbackInfoReturnable<List<Recipe<?>>> cir) {
        if (this.polymer_clientRewrittenRecipes == null) {
            try {
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                this.write(buf);
                this.polymer_clientRewrittenRecipes = ((SynchronizeRecipesS2CPacketAccessor) new SynchronizeRecipesS2CPacket(buf)).polymer_getRecipes();
            } catch (Throwable e) {
                this.polymer_clientRewrittenRecipes = Collections.emptyList();
            }
        }

        cir.setReturnValue(this.polymer_clientRewrittenRecipes);
    }


    @Override
    public boolean isWritingErrorSkippable() {
        return true;
    }
}
