package eu.pb4.polymer.virtualentity.api;

import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.impl.HolderHolder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class ElementHolder {
    private final Consumer<Packet<ClientPlayPacketListener>> EMPTY_PACKET_CONSUMER = (p) -> {};

    private HolderAttachment attachment;
    private final List<VirtualElement> elements = new ObjectArrayList<>();
    private final List<ServerPlayNetworkHandler> players = new ArrayList<>();
    protected Vec3d currentPos = Vec3d.ZERO;
    private final IntList entityIds = new IntArrayList();

    public boolean isPartOf(int entityId) {
        return this.entityIds.contains(entityId);
    }

    public IntList getEntityIds() {
        return this.entityIds;
    }

    public <T extends VirtualElement> T addElement(T element) {
        if (this.addElementWithoutUpdates(element)) {
            for (var player : this.players) {
                var x = new ArrayList<Packet<ClientPlayPacketListener>>();
                element.startWatching(player.getPlayer(), x::add);
                player.sendPacket(new BundleS2CPacket(x));
            }
        }
        return element;
    }

    public boolean addElementWithoutUpdates(VirtualElement element) {
        if (!this.elements.contains(element)) {
            this.elements.add(element);
            this.entityIds.addAll(element.getEntityIds());
            element.setHolder(this);
            return true;
        }
        return false;
    }

    public void removeElement(VirtualElement element) {
        if (this.removeElementWithoutUpdates(element)) {
            var packet = new EntitiesDestroyS2CPacket(element.getEntityIds());
            for (var player : this.players) {
                for (var e : this.elements) {
                    e.stopWatching(player.getPlayer(), player::sendPacket);
                }
                player.sendPacket(packet);
            }
        }
    }

    public boolean removeElementWithoutUpdates(VirtualElement element) {
        if (this.elements.contains(element)) {
            this.elements.remove(element);
            this.entityIds.removeAll(element.getEntityIds());
            element.setHolder(null);
            return true;
        }
        return false;
    }

    public List<VirtualElement> getElements() {
        return Collections.unmodifiableList(this.elements);
    }

    public boolean startWatching(ServerPlayNetworkHandler player) {
        if (this.players.contains(player)) {
            return false;
        }
        this.players.add(player);
        ((HolderHolder) player).polymer$addHolder(this);
        var packets = new ArrayList<Packet<ClientPlayPacketListener>>();

        for (var e : this.elements) {
            e.startWatching(player.getPlayer(), packets::add);
        }

        this.startWatchingExtraPackets(player, packets::add);

        if (this.attachment != null) {
            this.attachment.startWatchingExtraPackets(player, packets::add);
        }

        player.sendPacket(new BundleS2CPacket(packets));

        return true;
    }

    protected void startWatchingExtraPackets(ServerPlayNetworkHandler player, Consumer<Packet<ClientPlayPacketListener>> packetConsumer) {
    }

    public final boolean startWatching(ServerPlayerEntity player) {
        return startWatching(player.networkHandler);
    }

    public boolean stopWatching(ServerPlayNetworkHandler player) {
        if (!this.players.contains(player)) {
            return false;
        }
        this.players.remove(player);
        ((HolderHolder) player).polymer$removeHolder(this);

        Consumer<Packet<ClientPlayPacketListener>> packetConsumer = player.isConnectionOpen() ? player::sendPacket : EMPTY_PACKET_CONSUMER;

        for (var e : this.elements) {
            e.stopWatching(player.getPlayer(), packetConsumer);
        }
        packetConsumer.accept(new EntitiesDestroyS2CPacket(this.entityIds));

        return true;
    }

    public final boolean stopWatching(ServerPlayerEntity player) {
        return stopWatching(player.networkHandler);
    }

    public void tick() {
        if (this.attachment == null) {
            return;
        }

        this.onTick();

        this.updatePosition();

        for (var e : this.elements) {
            e.tick();
        }
    }

    protected void onTick() {
    }

    protected void updatePosition() {
        if (this.attachment == null || !this.attachment.canUpdatePosition()) {
            return;
        }

        var newPos = this.attachment.getPos();

        if (!this.currentPos.equals(newPos)) {
            var delta = newPos.subtract(newPos);
            this.notifyElementsOfPositionUpdate(newPos, delta);
            this.currentPos = newPos;
        }
    }

    protected void notifyElementsOfPositionUpdate(Vec3d newPos, Vec3d delta) {
        for (var e : this.elements) {
            e.notifyMove(this.currentPos, newPos, delta);
        }
    }

    public void sendPacket(Packet<ClientPlayPacketListener> packet) {
        for (var player : players) {
            player.sendPacket(packet);
        }
    }

    @Nullable
    public HolderAttachment getAttachment() {
        return this.attachment;
    }

    public void setAttachment(@Nullable HolderAttachment attachment) {
        this.attachment = attachment;
        if (attachment != null) {
            if (this.currentPos == Vec3d.ZERO && attachment.canUpdatePosition()) {
                this.currentPos = attachment.getPos();
            }
            attachment.updateCurrentlyTracking(new ArrayList<>(this.players));
        }
    }

    public Vec3d getPos() {
        if (this.currentPos == Vec3d.ZERO && attachment != null && attachment.canUpdatePosition()) {
            this.currentPos = attachment.getPos();
        }

        return this.currentPos;
    }

    public VirtualElement.InteractionHandler getInteraction(int id, ServerPlayerEntity player) {
        for (var x : this.elements) {
            if (x.getEntityIds().contains(id)) {
                return x.getInteractionHandler(player);
            }
        }
        return VirtualElement.InteractionHandler.EMPTY;
    }

    public void destroy() {
        for (var x : new ArrayList<>(this.players)) {
            this.stopWatching(x);
        }

        if (this.attachment != null) {
            this.attachment.destroy();
        }
    }

    public Collection<ServerPlayNetworkHandler> getWatchingPlayers() {
        return this.players;
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return 31;
    }

    public void notifyUpdate(HolderAttachment.UpdateType updateType) {
    }
}
