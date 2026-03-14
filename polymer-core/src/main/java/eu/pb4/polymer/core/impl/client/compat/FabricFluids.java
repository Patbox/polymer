package eu.pb4.polymer.core.impl.client.compat;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.impl.ImplPolymerRegistryEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class FabricFluids {
    public static void register() {
        ImplPolymerRegistryEvent.iterateAndRegister(BuiltInRegistries.FLUID, (fluid) -> {
            if (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.FLUID, fluid) != null && FluidRenderingRegistry.getOverride(fluid) == null) {
                var mat = new Material(Identifier.withDefaultNamespace("missing"));
                FluidRenderingRegistry.register(fluid, new FluidModel.Unbaked(mat, mat, null, null));
            }
        });
    }
}
