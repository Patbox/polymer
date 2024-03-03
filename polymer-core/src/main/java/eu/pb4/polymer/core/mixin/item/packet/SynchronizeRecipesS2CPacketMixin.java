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
    @Unique List<RecipeEntry<?>> polymer$clientRewrittenRecipes = null;

    @Shadow @Final private List<RecipeEntry<?>> recipes;

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

    /*
     * This is a hack, which converts all itemStack to client side version while running on singleplayer,
     * It's not great but I didn't have any better idea how to do it in better way
     * I could do it manually but it would require way more work + this one works just fine
     */
    /*@Environment(EnvType.CLIENT)
    @Inject(method = "getRecipes", at = @At("HEAD"), cancellable = true)
    private void polymer$replaceOnClient(CallbackInfoReturnable<List<RecipeEntry<?>>> cir) {
        if (ClientUtils.isSingleplayer()) {
            try {
                if (this.polymer$clientRewrittenRecipes == null) {
                    var rec = new ArrayList<RecipeEntry<?>>();

                    var buf = new PacketByteBuf(Unpooled.buffer(1024 * 40));
                    var player = ClientUtils.getPlayer();

                    var brokenIds = new HashSet<Identifier>();
                    for (var recipe : this.recipes) {
                        if (recipe.value() instanceof PolymerSyncedObject<?> syncedRecipe) {
                            if (player == null) {
                                continue;
                            }
                            var recipeData = (Recipe<?>) syncedRecipe.getPolymerReplacement(player);
                            if (recipeData == null) {
                                continue;
                            }
                            recipe = new RecipeEntry<Recipe<?>>(recipe.id(), recipeData);
                        }
                        buf.clear();
                        try {
                            ((RecipeSerializer<Recipe<?>>) recipe.value().getSerializer()).write(buf, recipe.value());
                            rec.add(new RecipeEntry<Recipe<?>>(recipe.id(), recipe.value().getSerializer().read(buf)));
                        } catch (Throwable e) { // Ofc some mods have weird issues with their serializers, because why not
                            rec.add(recipe);
                            brokenIds.add(Registries.RECIPE_SERIALIZER.getId(recipe.value().getSerializer()));
                            if (PolymerImpl.LOG_MORE_ERRORS) {
                                PolymerImpl.LOGGER.error("Couldn't rewrite recipe!", e);
                            }
                        }
                    }
                    if (!brokenIds.isEmpty()) {
                        PolymerImpl.LOGGER.warn("Failed to rewrite recipes of types: {} ", brokenIds);
                    }
                    this.polymer$clientRewrittenRecipes = rec;
                }
                cir.setReturnValue(this.polymer$clientRewrittenRecipes);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }*/


    @Override
    public boolean isWritingErrorSkippable() {
        return true;
    }
}
