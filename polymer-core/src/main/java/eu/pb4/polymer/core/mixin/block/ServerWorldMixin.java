package eu.pb4.polymer.core.mixin.block;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.interfaces.ServerPlayerExtension;
import eu.pb4.polymer.core.impl.other.world.SimpleVirtualWorld;
import net.minecraft.block.BlockState;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.BlockEvent;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {
    @Unique
    private SimpleVirtualWorld polymerCore$virtualWorld;

    protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    @Shadow
    public abstract List<ServerPlayerEntity> getPlayers();


    @Shadow @Final private ServerChunkManager chunkManager;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void polymerCore$createVirtualWorld(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey worldKey, DimensionOptions dimensionOptions, WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld, long seed, List spawners, boolean shouldTickTime, RandomSequencesState randomSequencesState, CallbackInfo ci) {
        if (PolymerBlockUtils.isStrictBlockUpdateRequired()) {
            this.polymerCore$virtualWorld = new SimpleVirtualWorld((ServerWorld) (Object) this);
        }
    }

    @Inject(method = "processBlockEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void polymerCore$runEmulatedUpdates(BlockEvent event, CallbackInfoReturnable<Boolean> cir, BlockState state) {
        if (!state.isOf(event.block()) || this.polymerCore$virtualWorld == null) {
            return;
        }

        try {
            state.onSyncedBlockEvent(this.polymerCore$virtualWorld, event.pos(), event.type(), event.data());
        } catch (Throwable e) {
            PolymerImpl.LOGGER.error("Emulating block updates failed! Disabling it for world {}", this.getRegistryKey().getValue());
            PolymerImpl.LOGGER.error("What went wrong: ", e);
            this.polymerCore$virtualWorld = null;
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void polymerCore$sendUpdates(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        if (this.polymerCore$virtualWorld != null) {
            this.polymerCore$virtualWorld.forEachNonMatchingState(this::polymerCore$markDirty);
            this.polymerCore$virtualWorld.clear();
        }
    }

    private void polymerCore$markDirty(BlockPos pos, BlockState realState, BlockState virtualState) {
        this.chunkManager.markForUpdate(pos);
    }
}
