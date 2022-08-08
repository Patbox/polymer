package eu.pb4.polymer.mixin.other;

import eu.pb4.polymer.api.other.PolymerSoundEvent;
import eu.pb4.polymer.api.other.PolymerStatusEffect;
import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import eu.pb4.polymer.api.utils.DynamicPacket;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.api.x.BlockMapper;
import eu.pb4.polymer.impl.PolymerImpl;
import eu.pb4.polymer.impl.interfaces.EntityAttachedPacket;
import eu.pb4.polymer.impl.interfaces.PolymerNetworkHandlerExtension;
import eu.pb4.polymer.impl.interfaces.StatusEffectPacketExtension;
import eu.pb4.polymer.impl.networking.BlockPacketUtil;
import eu.pb4.polymer.impl.networking.PolymerServerProtocolHandler;
import eu.pb4.polymer.impl.other.DelayedAction;
import eu.pb4.polymer.impl.other.ScheduledPacket;
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

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin implements PolymerNetworkHandlerExtension {
    @Unique
    private final Object2IntMap<String> polymer_protocolMap = new Object2IntOpenHashMap<>();

    @Unique
    private final Object2ObjectMap<String, DelayedAction> polymer_delayedActions = new Object2ObjectArrayMap<>();
    @Unique
    private final Object2LongMap<String> polymer_rateLimits = new Object2LongOpenHashMap<>();
    @Shadow
    public ServerPlayerEntity player;
    @Shadow
    private int ticks;
    @Unique
    private boolean polymer_advancedTooltip = false;
    @Unique
    private boolean polymer_hasResourcePack = false;
    @Unique
    private boolean polymer_ignoreNextStatus = false;
    @Unique
    private ArrayList<ScheduledPacket> polymer_scheduledPackets = new ArrayList<>();
    @Unique
    private String polymer_version = "";
    @Unique
    private BlockMapper polymer_blockMapper;

    @Shadow
    public abstract void sendPacket(Packet<?> packet);

    @Shadow
    public abstract ServerPlayerEntity getPlayer();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void polymer_setupInitial(MinecraftServer server, ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        this.polymer_blockMapper = BlockMapper.getDefault(player);
    }


    @Override
    public BlockMapper polymer_getBlockMapper() {
        return this.polymer_blockMapper;
    }

    @Override
    public void polymer_setBlockMapper(BlockMapper mapper) {
        this.polymer_blockMapper = mapper;
    }

    @Override
    public boolean polymer_hasResourcePack() {
        return this.polymer_hasResourcePack;
    }

    @Override
    public void polymer_setResourcePack(boolean value) {
        this.polymer_hasResourcePack = value;
    }

    @Override
    public void polymer_schedulePacket(Packet<?> packet, int duration) {
        this.polymer_scheduledPackets.add(new ScheduledPacket(packet, this.ticks + duration));
    }

    @Override
    public boolean polymer_hasPolymer() {
        return !this.polymer_version.isEmpty();
    }

    @Override
    public String polymer_version() {
        return this.polymer_version;
    }

    @Override
    public void polymer_setVersion(String version) {
        this.polymer_version = version;
    }

    @Override
    public long polymer_lastPacketUpdate(String packet) {
        return this.polymer_rateLimits.getLong(packet);
    }

    @Override
    public void polymer_savePacketTime(String packet) {
        this.polymer_rateLimits.put(packet, System.currentTimeMillis());
    }

    @Override
    public void polymer_resetSupported() {
        this.polymer_protocolMap.clear();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void polymer_sendScheduledPackets(CallbackInfo ci) {
        if (!this.polymer_scheduledPackets.isEmpty()) {
            var array = this.polymer_scheduledPackets;
            this.polymer_scheduledPackets = new ArrayList<>();

            for (var entry : array) {
                if (entry.time() <= this.ticks) {
                    this.sendPacket(entry.packet());
                } else {
                    this.polymer_scheduledPackets.add(entry);
                }
            }
        }

        if (!this.polymer_delayedActions.isEmpty()) {
            this.polymer_delayedActions.entrySet().removeIf(e -> e.getValue().tryDoing());
        }
    }

    @Override
    public void polymer_delayAction(String identifier, int delay, Runnable action) {
        this.polymer_delayedActions.put(identifier, new DelayedAction(identifier, delay, action));
    }

    @Override
    public void polymer_setAdvancedTooltip(boolean value) {
        this.polymer_advancedTooltip = value;
    }

    @Override
    public boolean polymer_advancedTooltip() {
        return this.polymer_advancedTooltip;
    }

    @Override
    public int polymer_getSupportedVersion(String identifier) {
        return this.polymer_protocolMap.getOrDefault(identifier, -1);
    }

    @Override
    public void polymer_setSupportedVersion(String identifier, int i) {
        this.polymer_protocolMap.put(identifier, i);
    }

    @Override
    public Object2IntMap<String> polymer_getSupportMap() {
        return this.polymer_protocolMap;
    }

    @Inject(method = "onCustomPayload", at = @At("HEAD"))
    private void polymer_catchPackets(CustomPayloadC2SPacket packet, CallbackInfo ci) {
        if (packet.getChannel().getNamespace().equals(PolymerUtils.ID)) {
            PolymerServerProtocolHandler.handle((ServerPlayNetworkHandler) (Object) this, packet.getChannel(), packet.getData());
        }
    }

    @Inject(method = "onResourcePackStatus", at = @At("TAIL"))
    private void polymer_changeStatus(ResourcePackStatusC2SPacket packet, CallbackInfo ci) {
        if (PolymerRPUtils.shouldCheckByDefault() && packet.getStatus() != ResourcePackStatusC2SPacket.Status.ACCEPTED) {
            if (this.polymer_ignoreNextStatus == false) {
                this.polymer_setResourcePack(switch (packet.getStatus()) {
                    case SUCCESSFULLY_LOADED -> true;
                    case DECLINED, FAILED_DOWNLOAD, ACCEPTED -> false;
                });
            }

            this.polymer_ignoreNextStatus = false;
        }
    }


    @ModifyVariable(method = "sendPacket(Lnet/minecraft/network/Packet;Lnet/minecraft/class_7648;)V", at = @At("HEAD"))
    private Packet<?> polymer_replacePacket(Packet<?> packet) {
        if (packet instanceof PlaySoundS2CPacket soundPacket && soundPacket.getSound() instanceof PolymerSoundEvent polymerSoundEvent) {
            var soundEffect = polymerSoundEvent.getSoundEffectFor(this.player);

            if (soundEffect instanceof PolymerSoundEvent outEffect) {
                return new PlaySoundIdS2CPacket(outEffect.getId(), soundPacket.getCategory(), new Vec3d(soundPacket.getX(), soundPacket.getY(), soundPacket.getZ()), soundPacket.getVolume(), soundPacket.getPitch(), soundPacket.getSeed());
            } else if (soundEffect != null) {
                return new PlaySoundS2CPacket(soundEffect, soundPacket.getCategory(), soundPacket.getX(), soundPacket.getY(), soundPacket.getZ(), soundPacket.getVolume(), soundPacket.getPitch(), soundPacket.getSeed());
            }
        } else if (packet instanceof PlaySoundFromEntityS2CPacket soundPacket && soundPacket.getSound() instanceof PolymerSoundEvent polymerSoundEvent) {
            var soundEffect = polymerSoundEvent.getSoundEffectFor(this.player);
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
    private void polymer_skipEffects(Packet<?> packet, PacketCallbacks arg, CallbackInfo ci) {
        if (packet instanceof DynamicPacket
                || (
                        (packet instanceof PlaySoundS2CPacket soundPacket && soundPacket.getSound() == PolymerSoundEvent.EMPTY_SOUND)
                                || packet instanceof StatusEffectPacketExtension packet2
                                && ((packet2.polymer_getStatusEffect() instanceof PolymerStatusEffect pol && pol.getPolymerStatusEffect(this.player) == null))
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
    public void polymer_setIgnoreNext() {
        this.polymer_ignoreNextStatus = true;
    }
}
