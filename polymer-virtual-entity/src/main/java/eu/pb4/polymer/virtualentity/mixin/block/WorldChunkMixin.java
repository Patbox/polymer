package eu.pb4.polymer.virtualentity.mixin.block;

import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.chunk.BlendingData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.*;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin extends Chunk implements HolderAttachmentHolder {

    public WorldChunkMixin(ChunkPos pos, UpgradeData upgradeData, HeightLimitView heightLimitView, Registry<Biome> biome, long inhabitedTime, @Nullable ChunkSection[] sectionArrayInitializer, @Nullable BlendingData blendingData) {
        super(pos, upgradeData, heightLimitView, biome, inhabitedTime, sectionArrayInitializer, blendingData);
    }

    @Shadow
    public abstract World getWorld();

    @Unique
    private final Collection<HolderAttachment> polymerVE$holders = new ArrayList<>();

    @Override
    public void polymer$addHolder(HolderAttachment holderAttachment) {
        this.polymerVE$holders.add(holderAttachment);
    }

    @Override
    public void polymer$removeHolder(HolderAttachment holderAttachment) {
        this.polymerVE$holders.remove(holderAttachment);
    }

    @Override
    public Collection<HolderAttachment> polymer$getHolders() {
        return this.polymerVE$holders;
    }
}
