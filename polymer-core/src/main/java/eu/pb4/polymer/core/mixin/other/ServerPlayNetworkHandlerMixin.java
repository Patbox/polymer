package eu.pb4.polymer.core.mixin.other;

import eu.pb4.polymer.core.api.block.BlockMapper;
import eu.pb4.polymer.core.api.other.PolymerSoundEvent;
import eu.pb4.polymer.core.api.other.PolymerStatusEffect;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.interfaces.EntityAttachedPacket;
import eu.pb4.polymer.core.impl.interfaces.PolymerNetworkHandlerExtension;
import eu.pb4.polymer.core.impl.interfaces.StatusEffectPacketExtension;
import eu.pb4.polymer.core.impl.networking.BlockPacketUtil;
import eu.pb4.polymer.core.impl.other.DelayedAction;
import eu.pb4.polymer.core.impl.other.ScheduledPacket;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin implements PolymerNetworkHandlerExtension {
    @Unique
    private final Object2ObjectMap<String, DelayedAction> polymer$delayedActions = new Object2ObjectArrayMap<>();
    @Shadow
    public ServerPlayerEntity player;
    @Shadow
    private int ticks;
    @Unique
    private boolean polymer$advancedTooltip = false;
    @Unique
    private ArrayList<ScheduledPacket> polymer$scheduledPackets = new ArrayList<>();
    @Unique
    private BlockMapper polymer$blockMapper;
    private List<Runnable> polymer$afterSequence = new ArrayList<>();

    @Shadow
    public abstract void sendPacket(Packet<?> packet);

    @Shadow
    public abstract ServerPlayerEntity getPlayer();

    @Shadow private int sequence;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void polymer$setupInitial(MinecraftServer server, ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        this.polymer$blockMapper = BlockMapper.getDefault(player);
    }


    @Override
    public BlockMapper polymer$getBlockMapper() {
        return this.polymer$blockMapper;
    }

    @Override
    public void polymer$setBlockMapper(BlockMapper mapper) {
        this.polymer$blockMapper = mapper;
    }

    @Override
    public void polymer$schedulePacket(Packet<?> packet, int duration) {
        this.polymer$scheduledPackets.add(new ScheduledPacket(packet, this.ticks + duration));
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void polymer$sendScheduledPackets(CallbackInfo ci) {
        if (!this.polymer$scheduledPackets.isEmpty()) {
            var array = this.polymer$scheduledPackets;
            this.polymer$scheduledPackets = new ArrayList<>();

            for (var entry : array) {
                if (entry.time() <= this.ticks) {
                    this.sendPacket(entry.packet());
                } else {
                    this.polymer$scheduledPackets.add(entry);
                }
            }
        }

        if (!this.polymer$delayedActions.isEmpty()) {
            this.polymer$delayedActions.entrySet().removeIf(e -> e.getValue().tryDoing());
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V", ordinal = 0, shift = At.Shift.AFTER))
    private void polymer$sendSequencePackets(CallbackInfo ci) {
        if (!this.polymer$afterSequence.isEmpty()) {
            for (var entry : this.polymer$afterSequence) {
                entry.run();
            }
            this.polymer$afterSequence.clear();
        }
    }

    @Override
    public void polymer$delayAction(String identifier, int delay, Runnable action) {
        this.polymer$delayedActions.put(identifier, new DelayedAction(identifier, delay, action));
    }

    @Override
    public void polymer$setAdvancedTooltip(boolean value) {
        this.polymer$advancedTooltip = value;
    }

    @Override
    public boolean polymer$advancedTooltip() {
        return this.polymer$advancedTooltip;
    }


    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At("HEAD"), cancellable = true)
    private void polymer$skipEffects(Packet<?> packet, PacketCallbacks arg, CallbackInfo ci) {
        if ((
                        (packet instanceof PlaySoundS2CPacket soundPacket && soundPacket.getSound() == PolymerSoundEvent.EMPTY_SOUND)
                                || packet instanceof StatusEffectPacketExtension packet2
                                && ((packet2.polymer$getStatusEffect() instanceof PolymerStatusEffect pol && pol.getPolymerReplacement(this.player) == null))
                ) || !EntityAttachedPacket.shouldSend(packet, this.player)
        ) {
            ci.cancel();
        }

        if (PolymerImpl.DONT_USE_BLOCK_DELTA_PACKET && packet instanceof ChunkDeltaUpdateS2CPacket cPacket) {
            BlockPacketUtil.splitChunkDelta((ServerPlayNetworkHandler) (Object) this, cPacket);
            ci.cancel();
        }
    }

    @Override
    public void polymer$delayAfterSequence(Runnable runnable) {
        if (this.sequence == -1) {
            runnable.run();
        } else {
            this.polymer$afterSequence.add(runnable);
        }
    }
}
