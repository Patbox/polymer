package eu.pb4.polymer.core.mixin.client.block;

import eu.pb4.polymer.core.api.client.ClientPolymerBlock;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.core.impl.client.interfaces.ClientBlockStorageInterface;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Just additional storage for simpler lookups on client (F3 and alike)
 */
@Environment(EnvType.CLIENT)
@Mixin(ChunkSection.class)
public class ChunkSectionMixin implements ClientBlockStorageInterface {
    @Unique
    private PalettedContainer<ClientPolymerBlock.State> polymer$container;

    @Inject(method = "<init>(Lnet/minecraft/world/chunk/ChunkSection;)V", at = @At("TAIL"))
    private void polymer$init(ChunkSection section, CallbackInfo ci) {
        this.polymer$createContainers();
    }

    @Inject(method = "<init>(Lnet/minecraft/world/chunk/PalettedContainer;Lnet/minecraft/world/chunk/ReadableContainer;)V", at = @At("TAIL"))
    private void polymer$init2(PalettedContainer blockStateContainer, ReadableContainer biomeContainer, CallbackInfo ci) {
        this.polymer$createContainers();
    }

    @Inject(method = "<init>(Lnet/minecraft/world/chunk/PalettesFactory;)V  ", at = @At("TAIL"))
    private void polymer$init3(PalettesFactory palettesFactory, CallbackInfo ci) {
        this.polymer$createContainers();
    }


    @Unique
    private void polymer$createContainers() {
        this.polymer$container = new PalettedContainer<>(ClientPolymerBlock.NONE_STATE, InternalClientRegistry.blockStatesPaletteProvider);
    }

    @Override
    public void polymer$setClientBlock(int x, int y, int z, ClientPolymerBlock.State block) {
        this.polymer$container.swapUnsafe(x & 15, y & 15, z & 15, block);
    }

    @Override
    public ClientPolymerBlock.State polymer$getClientBlock(int x, int y, int z) {
        return this.polymer$container.get(x & 15, y & 15, z & 15);
    }
}
