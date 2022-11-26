package eu.pb4.polymer.core.mixin.other;

import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.core.api.block.BlockMapper;
import eu.pb4.polymer.core.api.networking.DynamicPacket;
import eu.pb4.polymer.core.api.other.PolymerSoundEvent;
import eu.pb4.polymer.core.api.other.PolymerStatusEffect;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.interfaces.EntityAttachedPacket;
import eu.pb4.polymer.core.impl.interfaces.PolymerNetworkHandlerExtension;
import eu.pb4.polymer.core.impl.interfaces.StatusEffectPacketExtension;
import eu.pb4.polymer.core.impl.networking.BlockPacketUtil;
import eu.pb4.polymer.core.impl.networking.PolymerServerProtocolHandler;
import eu.pb4.polymer.core.impl.other.DelayedAction;
import eu.pb4.polymer.core.impl.other.ScheduledPacket;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.play.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundIdS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin implements PolymerNetworkHandlerExtension {
    @Unique
    private final Object2IntMap<String> polymer$protocolMap = new Object2IntOpenHashMap<>();

    @Unique
    private final Object2ObjectMap<String, DelayedAction> polymer$delayedActions = new Object2ObjectArrayMap<>();
    @Unique
    private final Object2LongMap<String> polymer$rateLimits = new Object2LongOpenHashMap<>();
    @Shadow
    public ServerPlayerEntity player;
    @Shadow
    private int ticks;
    @Unique
    private boolean polymer$advancedTooltip = false;
    @Unique
    private boolean polymer$hasResourcePack = false;
    @Unique
    private boolean polymer$ignoreNextStatus = false;
    @Unique
    private ArrayList<ScheduledPacket> polymer$scheduledPackets = new ArrayList<>();
    @Unique
    private String polymer$version = "";
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
    public boolean polymer$hasResourcePack() {
        return this.polymer$hasResourcePack;
    }

    @Override
    public void polymer$setResourcePack(boolean value) {
        this.polymer$hasResourcePack = value;
    }

    @Override
    public void polymer$schedulePacket(Packet<?> packet, int duration) {
        this.polymer$scheduledPackets.add(new ScheduledPacket(packet, this.ticks + duration));
    }

    @Override
    public boolean polymer$hasPolymer() {
        return !this.polymer$version.isEmpty();
    }

    @Override
    public String polymer$version() {
        return this.polymer$version;
    }

    @Override
    public void polymer$setVersion(String version) {
        this.polymer$version = version;
    }

    @Override
    public long polymer$lastPacketUpdate(String packet) {
        return this.polymer$rateLimits.getLong(packet);
    }

    @Override
    public void polymer$savePacketTime(String packet) {
        this.polymer$rateLimits.put(packet, System.currentTimeMillis());
    }

    @Override
    public void polymer$resetSupported() {
        this.polymer$protocolMap.clear();
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

    @Override
    public int polymer$getSupportedVersion(String identifier) {
        return this.polymer$protocolMap.getOrDefault(identifier, -1);
    }

    @Override
    public void polymer$setSupportedVersion(String identifier, int i) {
        this.polymer$protocolMap.put(identifier, i);
    }

    @Override
    public Object2IntMap<String> polymer$getSupportMap() {
        return this.polymer$protocolMap;
    }

    @Inject(method = "onCustomPayload", at = @At("HEAD"),cancellable = true)
    private void polymer$catchPackets(CustomPayloadC2SPacket packet, CallbackInfo ci) {
        if (packet.getChannel().getNamespace().equals(PolymerUtils.ID)) {
            PolymerServerProtocolHandler.handle((ServerPlayNetworkHandler) (Object) this, packet.getChannel(), packet.getData());
            ci.cancel();
        }
    }

    @Inject(method = "onResourcePackStatus", at = @At("TAIL"))
    private void polymer$changeStatus(ResourcePackStatusC2SPacket packet, CallbackInfo ci) {
        if (CompatStatus.POLYMER_RESOURCE_PACKS && PolymerResourcePackUtils.shouldCheckByDefault() && packet.getStatus() != ResourcePackStatusC2SPacket.Status.ACCEPTED) {
            if (this.polymer$ignoreNextStatus == false) {
                this.polymer$setResourcePack(switch (packet.getStatus()) {
                    case SUCCESSFULLY_LOADED -> true;
                    case DECLINED, FAILED_DOWNLOAD, ACCEPTED -> false;
                });
            }

            this.polymer$ignoreNextStatus = false;
        }
    }


    @ModifyVariable(method = "sendPacket(Lnet/minecraft/network/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At("HEAD"))
    private Packet<?> polymer$replacePacket(Packet<?> packet) {
        if (packet instanceof PlaySoundS2CPacket soundPacket && soundPacket.getSound() instanceof PolymerSoundEvent polymerSoundEvent) {
            var soundEffect = polymerSoundEvent.getPolymerReplacement(this.player);

            if (soundEffect instanceof PolymerSoundEvent outEffect) {
                return new PlaySoundIdS2CPacket(outEffect.getId(), soundPacket.getCategory(), new Vec3d(soundPacket.getX(), soundPacket.getY(), soundPacket.getZ()), soundPacket.getVolume(), soundPacket.getPitch(), soundPacket.getSeed());
            } else if (soundEffect != null) {
                return new PlaySoundS2CPacket(soundEffect, soundPacket.getCategory(), soundPacket.getX(), soundPacket.getY(), soundPacket.getZ(), soundPacket.getVolume(), soundPacket.getPitch(), soundPacket.getSeed());
            }
        } else if (packet instanceof PlaySoundFromEntityS2CPacket soundPacket && soundPacket.getSound() instanceof PolymerSoundEvent polymerSoundEvent) {
            var soundEffect = polymerSoundEvent.getPolymerReplacement(this.player);
            var entity = this.player.getWorld().getEntityById(soundPacket.getEntityId());
            if (entity != null) {
                if (soundEffect instanceof PolymerSoundEvent outEffect) {
                    return new PlaySoundIdS2CPacket(outEffect.getId(), soundPacket.getCategory(), entity.getPos(), soundPacket.getVolume(), soundPacket.getPitch(), soundPacket.getSeed());
                } else if (soundEffect != null) {
                    return new PlaySoundFromEntityS2CPacket(soundEffect, soundPacket.getCategory(), entity, soundPacket.getVolume(), soundPacket.getPitch(), soundPacket.getSeed());
                }
            }
        } else if (packet instanceof DynamicPacket dynamicPacket) {
            var out = dynamicPacket.createPacket((ServerPlayNetworkHandler) (Object) (this), this.player);

            if (out != null) {
                return out;
            }
        }

        return packet;
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At("HEAD"), cancellable = true)
    private void polymer$skipEffects(Packet<?> packet, PacketCallbacks arg, CallbackInfo ci) {
        if (packet instanceof DynamicPacket
                || (
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

    @Override
    public void polymer$setIgnoreNext() {
        this.polymer$ignoreNextStatus = true;
    }
}
