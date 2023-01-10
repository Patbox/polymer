package eu.pb4.polymer.autohost.impl;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.networking.api.EarlyPlayNetworkHandler;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.PlayPongC2SPacket;
import net.minecraft.network.packet.c2s.play.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;

import java.util.Optional;

public class ResourcePackNetworkHandler extends EarlyPlayNetworkHandler {
    private final boolean required;

    //private static final WorldChunk FAKE_CHUNK = new WorldChunk(PolymerUtils.getFakeWorld(), ChunkPos.ORIGIN);
    private static final ArmorStandEntity FAKE_ENTITY = new ArmorStandEntity(EntityType.ARMOR_STAND, PolymerCommonUtils.getFakeWorld());
    private boolean delayed;

    public ResourcePackNetworkHandler(Context context) {
        super(new Identifier("polymer","auto_host_resourcepack"), context);

        this.required = AutoHost.config.require || PolymerResourcePackUtils.isRequired();

        var player = this.getPlayer();

        if (PolymerResourcePackUtils.hasPack(player)) {
            this.continueJoining();
        } else {
            this.sendInitialGameJoin();

            //this.sendPacket(new ChunkDataS2CPacket(FAKE_CHUNK, PolymerUtils.getFakeWorld().getLightingProvider(), null, null, true));
            this.sendPacket(FAKE_ENTITY.createSpawnPacket());
            this.sendPacket(new EntityTrackerUpdateS2CPacket(FAKE_ENTITY.getId(), FAKE_ENTITY.getDataTracker().getChangedEntries()));
            this.sendPacket(new SetCameraEntityS2CPacket(FAKE_ENTITY));
            this.sendPacket(new CustomPayloadS2CPacket(CustomPayloadS2CPacket.BRAND, (new PacketByteBuf(Unpooled.buffer())).writeString(this.getServer().getServerModName() + "/" + "polymer_loading_pack")));
            this.sendPacket(new WorldTimeUpdateS2CPacket(0, 18000, false));
            if (WebServer.isPackReady) {
                this.sendPacket(new ResourcePackSendS2CPacket(WebServer.fullAddress, WebServer.hash, this.required, AutoHost.message));
            } else {
                this.delayed = true;
            }
        }
    }

    @Override
    public void onTick() {
        if (this.delayed && WebServer.isPackReady) {
            this.delayed = false;
            this.sendPacket(new ResourcePackSendS2CPacket(WebServer.fullAddress, WebServer.hash, this.required, AutoHost.message));
        }
    }

    @Override
    public void onResourcePackStatus(ResourcePackStatusC2SPacket packet) {
        switch (packet.getStatus()) {
            case ACCEPTED -> PolymerResourcePackUtils.setPlayerStatus(this.getPlayer(), true);
            case SUCCESSFULLY_LOADED -> {
                PolymerResourcePackUtils.setPlayerStatus(this.getPlayer(), true);
                this.sendPacket(new PlayPingS2CPacket(0));
            }
            case DECLINED, FAILED_DOWNLOAD -> {
                if (this.required) {
                    this.disconnect(Text.translatable("multiplayer.texturePrompt.failure.line1"));
                } else {
                    this.sendPacket(new PlayPingS2CPacket(0));
                }
            }
        }
    }

    @Override
    public void onPong(PlayPongC2SPacket packet) {
        if (packet.getParameter() == 0) {
            this.continueJoining();
        }
    }

    static {
        {
            FAKE_ENTITY.setPos(0, 64, 0);
            FAKE_ENTITY.setNoGravity(true);
            FAKE_ENTITY.setInvisible(true);
            var nbt = new NbtCompound();
            FAKE_ENTITY.writeCustomDataToNbt(nbt);
            nbt.putBoolean("Marker", true);
            FAKE_ENTITY.readCustomDataFromNbt(nbt);
        }
    }
}
