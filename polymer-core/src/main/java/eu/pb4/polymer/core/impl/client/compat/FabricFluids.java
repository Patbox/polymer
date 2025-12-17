package eu.pb4.polymer.core.impl.client.compat;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.core.impl.ImplPolymerRegistryEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class FabricFluids {
    public static void register() {

        var renderer = new FluidRenderHandler() {
            @Override
            public TextureAtlasSprite[] getFluidSprites(@Nullable BlockAndTintGetter view, @Nullable BlockPos pos, FluidState state) {
                return new TextureAtlasSprite[] { Minecraft.getInstance().getModelManager().getMissingBlockStateModel().particleIcon() };
            }
        };

        ImplPolymerRegistryEvent.iterateAndRegister(BuiltInRegistries.FLUID, (fluid) -> {
            if (fluid instanceof PolymerObject && FluidRenderHandlerRegistry.INSTANCE.get(fluid) == null) {
                FluidRenderHandlerRegistry.INSTANCE.register(fluid, renderer);
            }
        });
    }
}
