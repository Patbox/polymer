package eu.pb4.polymer.core.mixin.item.packet;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import eu.pb4.polymer.common.impl.client.ClientUtils;
import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.PolymerImpl;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
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
import java.util.HashSet;
import java.util.List;

@Mixin(SynchronizeRecipesS2CPacket.class)
public abstract class SynchronizeRecipesS2CPacketMixin implements Packet {
    @ModifyReturnValue(method = "method_55955", at = @At("TAIL"))
    private static List<RecipeEntry<?>> polymer$remapRecipes(List<RecipeEntry<?>> recipes) {
        List<RecipeEntry<?>> list = new ArrayList<>();
        var player = PolymerUtils.getPlayerContext();
        for (var recipe : recipes) {
            if (recipe.value() instanceof PolymerSyncedObject<?> syncedRecipe) {
                Recipe<?> polymerRecipe = (Recipe<?>) syncedRecipe.getPolymerReplacement(player);
                if (polymerRecipe != null) {
                    list.add(new RecipeEntry<Recipe<?>>(recipe.id(), polymerRecipe));
                }
            } else if (!(PolymerObject.is(recipe.value().getSerializer()) || PolymerObject.is(recipe))) {
                list.add(recipe);
            }
        }
        return list;
    }
    @Override
    public boolean isWritingErrorSkippable() {
        return true;
    }
}
