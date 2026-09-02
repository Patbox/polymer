package eu.pb4.polymer.core.mixin.block;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.interfaces.PolymerIdMapper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.IdMapper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BlockMixin {
    @Shadow
    @Final
    public static IdMapper<BlockState> BLOCK_STATE_REGISTRY;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void polymer$enableMapping(CallbackInfo ci) {
        ((PolymerIdMapper<BlockState>) BLOCK_STATE_REGISTRY).polymer$setChecker(
                x -> PolymerSyncedObject.getSyncedObject(BuiltInRegistries.BLOCK, x.getBlock()) != null,
                x -> PolymerImplUtils.isServerSideSyncableEntry((Registry<Object>) (Object) BuiltInRegistries.BLOCK, x.getBlock()),
                x -> "(Block) " + BuiltInRegistries.BLOCK.getKey(x.getBlock())
        );
    }

    @Inject(method = "spawnDestroyByEntityParticles", at = @At("HEAD"))
    private void addPolymerParticles(Level level, Entity entity, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (entity instanceof ServerPlayer serverPlayer
                && PolymerBlockUtils.shouldMineServerSide(serverPlayer, pos, state)) {
            serverPlayer.connection.send(new ClientboundLevelEventPacket(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state), false));
        }
    }
}
