package eu.pb4.polymer.mixin.client.item.packet;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.recipe.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(SynchronizeRecipesS2CPacket.class)
public interface SynchronizeRecipesS2CPacketAccessor {
    @Accessor("recipes")
    List<Recipe<?>> polymer_getRecipes();
}
