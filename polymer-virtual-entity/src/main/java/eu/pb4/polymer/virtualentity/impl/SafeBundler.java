package eu.pb4.polymer.virtualentity.impl;

import net.minecraft.network.handler.PacketBundleHandler;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SafeBundler implements Consumer<Packet<ClientPlayPacketListener>> {
    private List<Packet<? super ClientPlayPacketListener>> packets = new ArrayList<>();
    private final Consumer<Packet<ClientPlayPacketListener>> consumer;

    public SafeBundler(Consumer<Packet<ClientPlayPacketListener>> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void accept(Packet<ClientPlayPacketListener> packet) {
        this.packets.add(packet);
        if (this.packets.size() == PacketBundleHandler.MAX_PACKETS) {
            this.consumer.accept(new BundleS2CPacket(this.packets));
            this.packets = new ArrayList<>();
        }
    }

    public void finish() {
        if (!this.packets.isEmpty()) {
            this.consumer.accept(new BundleS2CPacket(this.packets));
            this.packets = null;
        }
    }
}
