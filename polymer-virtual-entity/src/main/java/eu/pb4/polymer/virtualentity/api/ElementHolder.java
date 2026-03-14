package eu.pb4.polymer.virtualentity.api;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.impl.HolderHolder;
import eu.pb4.polymer.virtualentity.impl.SafeBundler;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

public class ElementHolder {
    private final Consumer<Packet<ClientGamePacketListener>> EMPTY_PACKET_CONSUMER = (p) -> {};

    private HolderAttachment attachment;
    private final List<VirtualElement> elements = new ObjectArrayList<>();
    private final List<ServerGamePacketListenerImpl> players = new ArrayList<>();
    protected Vec3 currentPos = Vec3.ZERO;
    private ChunkPos currentChunkPos = null;

    private final IntList entityIds = new IntArrayList();
    private final IntList attachedPassengerEntityIds = new IntArrayList();

    public boolean isPartOf(int entityId) {
        return this.entityIds.contains(entityId);
    }

    public IntList getEntityIds() {
        return this.entityIds;
    }

    public <T extends VirtualElement> T addElement(T element) {
        if (this.addElementWithoutUpdates(element)) {
            for (var player : this.players) {
                var x = new SafeBundler(player::send);
                element.startWatching(player.getPlayer(), x);
                x.finish();
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
            var packet = new ClientboundRemoveEntitiesPacket(element.getEntityIds());
            for (var player : this.players) {
                for (var e : this.elements) {
                    e.stopWatching(player.getPlayer(), player::send);
                }
                player.send(packet);
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

    public boolean startWatching(ServerGamePacketListenerImpl player) {
        if (this.players.contains(player)) {
            return false;
        }
        this.players.add(player);
        ((HolderHolder) player).polymer$addHolder(this);
        var packets = new SafeBundler(player::send);

        for (var e : this.elements) {
            e.startWatching(player.getPlayer(), packets);
        }

        this.startWatchingExtraPackets(player, packets);

        if (this.attachment != null) {
            this.attachment.startWatchingExtraPackets(player, packets);
        }

        packets.finish();

        return true;
    }

    protected void startWatchingExtraPackets(ServerGamePacketListenerImpl player, Consumer<Packet<ClientGamePacketListener>> packetConsumer) {
    }

    public final boolean startWatching(ServerPlayer player) {
        return startWatching(player.connection);
    }

    public boolean stopWatching(ServerGamePacketListenerImpl player) {
        if (!this.players.contains(player)) {
            return false;
        }
        this.players.remove(player);
        ((HolderHolder) player).polymer$removeHolder(this);

        Consumer<Packet<ClientGamePacketListener>> packetConsumer = player.isAcceptingMessages() ? player::send : EMPTY_PACKET_CONSUMER;

        for (var e : this.elements) {
            e.stopWatching(player.getPlayer(), packetConsumer);
        }
        packetConsumer.accept(new ClientboundRemoveEntitiesPacket(this.entityIds));

        return true;
    }

    public final boolean stopWatching(ServerPlayer player) {
        return stopWatching(player.connection);
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
            var delta = newPos.subtract(this.currentPos);
            this.notifyElementsOfPositionUpdate(newPos, delta);
            this.currentPos = newPos;
            this.currentChunkPos = null;
        }
    }

    protected void updateInitialPosition() {
        var newPos = this.attachment.getPos();

        for (var e : this.elements) {
            e.setInitialPosition(newPos);
        }

        this.currentPos = newPos;
        this.currentChunkPos = null;
    }

    protected void invalidateCaches() {
        this.currentChunkPos = null;
    }

    public ChunkPos getChunkPos() {
        if (this.currentChunkPos == null) {
            this.currentChunkPos =  ChunkPos.containing(BlockPos.containing(this.currentPos));
        }
        return this.currentChunkPos;
    }

    protected void notifyElementsOfPositionUpdate(Vec3 newPos, Vec3 delta) {
        for (var e : this.elements) {
            e.notifyMove(this.currentPos, newPos, delta);
        }
    }

    public void sendPacket(Packet<? extends ClientGamePacketListener> packet) {
        for (var player : players) {
            player.send(packet);
        }
    }

    public void sendPacket(Packet<? extends ClientGamePacketListener> packet, Predicate<ServerPlayer> predicate) {
        for (var player : players) {
            if (predicate.test(player.getPlayer())) {
                player.send(packet);
            }
        }
    }

    @Nullable
    public HolderAttachment getAttachment() {
        return this.attachment;
    }

    public void setAttachment(@Nullable HolderAttachment attachment) {
        var oldAttachment = this.attachment;
        this.attachment = attachment;
        if (attachment != null) {
            if (this.currentPos == Vec3.ZERO && attachment.canUpdatePosition()) {
                this.updateInitialPosition();
            }
            attachment.updateCurrentlyTracking(new ArrayList<>(this.players));
            this.onAttachmentSet(attachment, oldAttachment);
        } else if (oldAttachment != null) {
            this.onAttachmentRemoved(oldAttachment);
        }
    }

    protected void onAttachmentSet(HolderAttachment attachment, @Nullable HolderAttachment oldAttachment) {
    }

    protected void onAttachmentRemoved(HolderAttachment oldAttachment) {
    }

    public Vec3 getPos() {
        if (this.currentPos == Vec3.ZERO && attachment != null && attachment.canUpdatePosition()) {
            this.currentPos = attachment.getPos();
        }

        return this.currentPos;
    }

    public VirtualElement.InteractionHandler getInteraction(int id, ServerPlayer player) {
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

    public Collection<ServerGamePacketListenerImpl> getWatchingPlayers() {
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

    public IntList getAttachedPassengerEntityIds() {
        return this.attachedPassengerEntityIds;
    }

    public <T extends VirtualElement> T addPassengerElement(T element) {
        this.addElement(element);
        attachedPassengerEntityIds.addAll(element.getEntityIds());
        return element;
    }

    public void addPassengerId(int i) {
        this.attachedPassengerEntityIds.add(i);
    }

    public void removePassengerId(int i) {
        this.attachedPassengerEntityIds.removeInt(i);
    }
}
