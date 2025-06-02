package eu.pb4.polymer.autohost.impl;

import eu.pb4.polymer.resourcepack.impl.PolymerResourcePackMod;
import net.minecraft.dialog.*;
import net.minecraft.dialog.action.DynamicCustomDialogAction;
import net.minecraft.dialog.body.PlainMessageDialogBody;
import net.minecraft.dialog.type.NoticeDialog;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.common.ClearDialogS2CPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.network.packet.s2c.common.ShowDialogS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayerConfigurationTask;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AutoHostTask implements ServerPlayerConfigurationTask {
    public static final Key KEY = new Key("polymer:autohost/send_packs");
    public static final Identifier DISCONNECT = Identifier.of("polymer:autohost/disconnect");
    private final Collection<MinecraftServer.ServerResourcePackProperties> packs;

    private final Set<UUID> requiredPacks = new HashSet<>();
    private final Set<UUID> waitingFor = new HashSet<>();
    private final Supplier<Collection<MinecraftServer.ServerResourcePackProperties>> delayed;
    private final BooleanSupplier isReady;
    private boolean hasDelayed;
    private int statusCount = -1;

    public AutoHostTask(Collection<MinecraftServer.ServerResourcePackProperties> properties, boolean hasDelayed,
                        Supplier<Collection<MinecraftServer.ServerResourcePackProperties>> delayed, BooleanSupplier isReady) {
        this.packs = properties;
        for (var pack : packs) {
            if (pack.isRequired()) {
                requiredPacks.add(pack.id());
            }
            waitingFor.add(pack.id());
        }
        this.hasDelayed = hasDelayed;
        this.delayed = delayed;
        this.isReady = isReady;
    }

    @Override
    public void sendPacket(Consumer<Packet<?>> sender) {
        if (this.hasDelayed) {
            this.sendDialog(sender);
        }
        for (var pack : packs) {
            sender.accept(new ResourcePackSendS2CPacket(pack.id(), pack.url(), pack.hash(), pack.isRequired(), Optional.ofNullable(pack.prompt())));
        }
    }

    private void sendDialog(Consumer<Packet<?>> sender) {
        if (!AutoHost.config.dialog) {
            return;
        }

        this.statusCount = PolymerResourcePackMod.STATUS.size();
        sender.accept(new ShowDialogS2CPacket(RegistryEntry.of(new NoticeDialog(
                new DialogCommonData(AutoHost.dialogTitle, Optional.empty(),false, false, AfterAction.CLOSE,
                        List.of(new PlainMessageDialogBody(PolymerResourcePackMod.STATUS.isEmpty() || !AutoHost.config.dialogShowStatus ? AutoHost.dialogDefaultBody :
                                Text.literal(String.join("\n", PolymerResourcePackMod.STATUS
                                        .subList(Math.max(PolymerResourcePackMod.STATUS.size() - 32, 0), PolymerResourcePackMod.STATUS.size()))), 300)), List.of()),
                new DialogActionButtonData(new DialogButtonData(
                        Text.translatable("menu.disconnect"), 150),
                        Optional.of(new DynamicCustomDialogAction(DISCONNECT, Optional.empty())))
        ))));
    }

    public void tick(Consumer<Packet<?>> sender) {
        if (this.hasDelayed && this.isReady.getAsBoolean()) {
            if (!AutoHost.config.dialog) {
                sender.accept(ClearDialogS2CPacket.INSTANCE);
            }
            var delayed = this.delayed.get();
            for (var pack : delayed) {
                if (pack.isRequired()) {
                    requiredPacks.add(pack.id());
                }
                waitingFor.add(pack.id());
            }
            for (var pack : delayed) {
                sender.accept(new ResourcePackSendS2CPacket(pack.id(), pack.url(), pack.hash(), pack.isRequired(), Optional.ofNullable(pack.prompt())));
            }
            this.hasDelayed = false;
        } else if (this.hasDelayed && this.statusCount != PolymerResourcePackMod.STATUS.size()) {
            sendDialog(sender);
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

        return this.waitingFor.isEmpty() && !this.hasDelayed;
    }
}
