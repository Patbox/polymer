package eu.pb4.polymer.autohost.impl;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayerConfigurationTask;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class AutoHostTask implements ServerPlayerConfigurationTask {
    public static final Key KEY = new Key("polymer:autohost/send_packs");
    private final Collection<MinecraftServer.ServerResourcePackProperties> packs;

    private final Set<UUID> requiredPacks = new HashSet<>();
    private final Set<UUID> waitingFor = new HashSet<>();

    public AutoHostTask(Collection<MinecraftServer.ServerResourcePackProperties> properties) {
        this.packs = properties;
        for (var pack : packs) {
            if (pack.isRequired()) {
                requiredPacks.add(pack.id());
            }
            waitingFor.add(pack.id());
        }
    }

    @Override
    public void sendPacket(Consumer<Packet<?>> sender) {
        for (var pack : packs) {
            sender.accept(new ResourcePackSendS2CPacket(pack.id(), pack.url(), pack.hash(), pack.isRequired(), pack.prompt()));
        }
    }

    @Override
    public Key getKey() {
        return KEY;
    }

    public boolean onStatus(ServerConfigurationNetworkHandler handler, UUID id, ResourcePackStatusC2SPacket.Status status) {
        switch (status) {
            case DECLINED, FAILED_RELOAD, FAILED_DOWNLOAD, INVALID_URL -> {
                if (this.requiredPacks.contains(id)) {
                    handler.disconnect(Text.translatable("multiplayer.requiredTexturePrompt.disconnect"));
                }
            }
        }

        if (status.hasFinished()) {
            this.waitingFor.remove(id);
        }

        return this.waitingFor.isEmpty();
    }
}
