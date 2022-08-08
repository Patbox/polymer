package eu.pb4.polymer.mixin.client.block;

import eu.pb4.polymer.api.client.registry.ClientPolymerBlock;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import eu.pb4.polymer.impl.client.interfaces.ClientBlockStorageInterface;
import eu.pb4.polymer.mixin.other.PalettedContainerAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.ReadableContainer;
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
    private PalettedContainer<ClientPolymerBlock.State> polymer_container;

    @Unique
    private PalettedContainerAccessor<ClientPolymerBlock.State> polymer_accessor;

    @Inject(method = "<init>(ILnet/minecraft/util/registry/Registry;)V", at = @At("TAIL"))
    private void polymer_init(int chunkPos, Registry<Biome> biomeRegistry, CallbackInfo ci) {
        this.polymer_createContainers();
    }

    @Inject(method = "<init>(ILnet/minecraft/world/chunk/PalettedContainer;Lnet/minecraft/world/chunk/ReadableContainer;)V", at = @At("TAIL"))
    private void polymer_init2(int chunkPos, PalettedContainer blockStateContainer, ReadableContainer readableContainer, CallbackInfo ci) {
        this.polymer_createContainers();
    }


    private void polymer_createContainers() {
        this.polymer_container = new PalettedContainer<>(InternalClientRegistry.BLOCK_STATES, ClientPolymerBlock.NONE_STATE, PalettedContainer.PaletteProvider.BLOCK_STATE);
        this.polymer_accessor = (PalettedContainerAccessor<ClientPolymerBlock.State>) this.polymer_container;
    }

    @Override
    public void polymer_setClientPolymerBlock(int x, int y, int z, ClientPolymerBlock.State block) {
        this.polymer_accessor.polymer_set(this.polymer_accessor.polymer_paletteProvider().computeIndex(x & 15, y & 15, z & 15), block);
    }

    @Override
    public ClientPolymerBlock.State polymer_getClientPolymerBlock(int x, int y, int z) {
        return this.polymer_container.get(x & 15, y & 15, z & 15);
    }

    @Override
    public boolean polymer_hasClientPalette() {
        return this.polymer_container != null;
    }
}
