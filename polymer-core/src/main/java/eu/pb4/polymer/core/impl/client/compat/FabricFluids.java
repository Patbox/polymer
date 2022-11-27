package eu.pb4.polymer.core.impl.client.compat;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class FabricFluids {
    public static void register() {

        var renderer = new FluidRenderHandler() {
            @Override
            public Sprite[] getFluidSprites(@Nullable BlockRenderView view, @Nullable BlockPos pos, FluidState state) {
                return new Sprite[] { MinecraftClient.getInstance().getBakedModelManager().getMissingModel().getParticleSprite() };
            }
        };

        for (var fluid : Registries.FLUID) {
            if (fluid instanceof PolymerObject && FluidRenderHandlerRegistry.INSTANCE.get(fluid) == null) {
                FluidRenderHandlerRegistry.INSTANCE.register(fluid, renderer);
            }
        }

        PolymerImplUtils.ON_REGISTERED.register((reg, obj) -> {
            if (obj instanceof Fluid fluid && fluid instanceof PolymerObject && FluidRenderHandlerRegistry.INSTANCE.get(fluid) == null) {
                FluidRenderHandlerRegistry.INSTANCE.register(fluid, renderer);
            }
        });
    }
}
