package eu.pb4.polymer.autohost.impl;

import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.api.x.EarlyPlayNetworkHandler;
import eu.pb4.polymer.impl.PolymerImplUtils;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.PlayPongC2SPacket;
import net.minecraft.network.packet.c2s.play.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

import java.util.Optional;

public class ResourcePackNetworkHandler extends EarlyPlayNetworkHandler {
    private final boolean required;
    private final boolean velocityCompat;
    private final int timeoutAge;

    //private static final WorldChunk FAKE_CHUNK = new WorldChunk(PolymerUtils.getFakeWorld(), ChunkPos.ORIGIN);
    private static final ArmorStandEntity FAKE_ENTITY = new ArmorStandEntity(EntityType.ARMOR_STAND, PolymerUtils.getFakeWorld());

    public ResourcePackNetworkHandler(Context context) {
        super(PolymerImplUtils.id("auto_host_resourcepack"), context);

        this.required = AutoHost.config.require || PolymerRPUtils.isRequired();
        this.velocityCompat = AutoHost.config.velocityCompat;

        var player = this.getPlayer();
        timeoutAge = player.age + AutoHost.config.forceJoinTicks;

        if (PolymerRPUtils.hasPack(player)) {
            this.continueJoining();
        } else {
            var server = this.getServer();
            this.sendPacket(new GameJoinS2CPacket(player.getId(), false, GameMode.SPECTATOR, null, server.getWorldRegistryKeys(), server.getRegistryManager(), server.getOverworld().getDimensionKey(), server.getOverworld().getRegistryKey(), 0, server.getPlayerManager().getMaxPlayerCount(), 2, 2, false, false, false, true, Optional.empty()));

            //this.sendPacket(new ChunkDataS2CPacket(FAKE_CHUNK, PolymerUtils.getFakeWorld().getLightingProvider(), null, null, true));
            this.sendPacket(FAKE_ENTITY.createSpawnPacket());
            this.sendPacket(new EntityTrackerUpdateS2CPacket(FAKE_ENTITY.getId(), FAKE_ENTITY.getDataTracker(), true));
            this.sendPacket(new SetCameraEntityS2CPacket(FAKE_ENTITY));
            this.sendPacket(new CustomPayloadS2CPacket(CustomPayloadS2CPacket.BRAND, (new PacketByteBuf(Unpooled.buffer())).writeString(this.getServer().getServerModName() + "/" + "polymer_loading_pack")));
            this.sendPacket(new WorldTimeUpdateS2CPacket(0, 18000, false));
            this.sendPacket(new ResourcePackSendS2CPacket(WebServer.fullAddress, WebServer.hash, this.required, AutoHost.message));
        }
    }

    @Override
    public void onResourcePackStatus(ResourcePackStatusC2SPacket packet) {
        switch (packet.getStatus()) {
            case ACCEPTED -> PolymerRPUtils.setPlayerStatus(this.getPlayer(), true);
            case SUCCESSFULLY_LOADED -> {
                getServer().sendMessage(Text.literal("Received successfully loaded!"));
                PolymerRPUtils.setPlayerStatus(this.getPlayer(), true);
                if (velocityCompat) {
                    this.sendKeepAlive();
                    this.continueJoining();
                } else {
                    getServer().sendMessage(Text.literal("sending ping!"));
                    this.sendPacket(new PlayPingS2CPacket(0));
                }
            }
            case DECLINED, FAILED_DOWNLOAD -> {
                if (this.required) {
                    this.disconnect(Text.translatable("multiplayer.texturePrompt.failure.line1"));
                } else {
                    if (velocityCompat) {
                        this.sendKeepAlive();
                        this.continueJoining();
                    } else {
                        this.sendPacket(new PlayPingS2CPacket(0));
                    }
                }
            }
        }
    }

    @Override
    public void onPong(PlayPongC2SPacket packet) {
        getServer().sendMessage(Text.literal("Received pong: "+packet.getParameter()+", velocityCompat: "+velocityCompat));
        if (packet.getParameter() == 0 && !velocityCompat) {
            //this.sendKeepAlive();
            this.continueJoining();
            getServer().sendMessage(Text.literal("Continued joining :)"));
            //this.keepAliveReceived++;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getServer().getOverworld().getTime() % 20 == 0) {
            getServer().sendMessage(Text.literal("Your age: "+getPlayer().age+", timeout: "+timeoutAge+", kaS: "+keepAliveSent+", kaR: "+keepAliveReceived+", alreadyContinued"+alreadyContinued+", canContinue"+canContinue));
        }
        if (this.getPlayer().age > this.timeoutAge) {
            this.sendKeepAlive();
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
