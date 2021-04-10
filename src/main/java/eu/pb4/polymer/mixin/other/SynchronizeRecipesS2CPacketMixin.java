package eu.pb4.polymer.mixin.other;

import eu.pb4.polymer.PolymerMod;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.stream.Collectors;


/*
 * Based on mixin from PolyMC - https://github.com/TheEpicBlock/PolyMc/blob/master/src/main/java/io/github/theepicblock/polymc/mixins/item/CustomRecipeFix.java
 */

@Mixin(SynchronizeRecipesS2CPacket.class)
public class SynchronizeRecipesS2CPacketMixin {
    @Shadow private List<Recipe<?>> recipes;

    @Inject(method = "write", at = @At("HEAD"))
    public void onWrite(PacketByteBuf buf, CallbackInfo callbackInfo) {
            recipes = recipes.stream()
                    .filter(recipe -> !PolymerMod.isVirtualRecipeSerializer(Registry.RECIPE_SERIALIZER.getId(recipe.getSerializer())))
                    .collect(Collectors.toList());
    }
}
