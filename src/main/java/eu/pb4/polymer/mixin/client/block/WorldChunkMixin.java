package eu.pb4.polymer.mixin.client.block;

import eu.pb4.polymer.api.client.registry.ClientPolymerBlock;
import eu.pb4.polymer.impl.client.interfaces.ClientBlockStorageInterface;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.*;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.BlendingData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Environment(EnvType.CLIENT)
@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin extends Chunk implements ClientBlockStorageInterface {

    public WorldChunkMixin(ChunkPos pos, UpgradeData upgradeData, HeightLimitView heightLimitView, Registry<Biome> biome, long inhabitedTime, @Nullable ChunkSection[] sectionArrayInitializer, @Nullable BlendingData blendingData) {
        super(pos, upgradeData, heightLimitView, biome, inhabitedTime, sectionArrayInitializer, blendingData);
    }

    @Override
    public void polymer_setClientPolymerBlock(int x, int y, int z, ClientPolymerBlock.State block) {
        var section = this.getSection(this.getSectionIndex(y));

        if (section != null && !section.isEmpty()) {
            ((ClientBlockStorageInterface) section).polymer_setClientPolymerBlock(x, y, z, block);
        }
    }

    @Override
    public ClientPolymerBlock.State polymer_getClientPolymerBlock(int x, int y, int z) {
        var section = this.getSection(this.getSectionIndex(y));

        if (section != null && !section.isEmpty()) {
            return ((ClientBlockStorageInterface) section).polymer_getClientPolymerBlock(x, y, z);
        }

        return ClientPolymerBlock.NONE_STATE;
    }

    @Override
    public boolean polymer_hasClientPalette() {
        return true;
    }
}
