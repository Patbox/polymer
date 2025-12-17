package eu.pb4.polymer.virtualentity.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.protocol.BundlerInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;

public class SafeBundler implements Consumer<Packet<ClientGamePacketListener>> {
    private List<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>();
    private final Consumer<Packet<ClientGamePacketListener>> consumer;

    public SafeBundler(Consumer<Packet<ClientGamePacketListener>> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void accept(Packet<ClientGamePacketListener> packet) {
        this.packets.add(packet);
        if (this.packets.size() == BundlerInfo.BUNDLE_SIZE_LIMIT) {
            this.consumer.accept(new ClientboundBundlePacket(this.packets));
            this.packets = new ArrayList<>();
        }
    }

    public void finish() {
        if (!this.packets.isEmpty()) {
            this.consumer.accept(new ClientboundBundlePacket(this.packets));
            this.packets = null;
        }
    }
}
