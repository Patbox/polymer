package eu.pb4.polymer.autohost.impl;

import eu.pb4.polymer.resourcepack.impl.PolymerResourcePackMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundClearDialogPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ClientboundShowDialogPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dialog.ActionButton;
import net.minecraft.server.dialog.CommonButtonData;
import net.minecraft.server.dialog.CommonDialogData;
import net.minecraft.server.dialog.DialogAction;
import net.minecraft.server.dialog.NoticeDialog;
import net.minecraft.server.dialog.action.CustomAll;
import net.minecraft.server.dialog.body.DialogBody;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AutoHostTask implements ConfigurationTask {
    public static final Type KEY = new Type("polymer:autohost/send_packs");
    public static final Identifier DISCONNECT = Identifier.parse("polymer:autohost/disconnect");
    private final Collection<MinecraftServer.ServerResourcePackInfo> packs;

    private final Set<UUID> requiredPacks = new HashSet<>();
    private final Set<UUID> waitingFor = new HashSet<>();
    private final Supplier<Collection<MinecraftServer.ServerResourcePackInfo>> delayed;
    private final BooleanSupplier isReady;
    private boolean hasDelayed;
    private int statusCount = -1;
    private int tick = 0;

    public AutoHostTask(Collection<MinecraftServer.ServerResourcePackInfo> properties, boolean hasDelayed,
                        Supplier<Collection<MinecraftServer.ServerResourcePackInfo>> delayed, BooleanSupplier isReady) {
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
    public void start(Consumer<Packet<?>> sender) {
        if (AutoHost.config.clearResourcePacks) {
            sender.accept(new ClientboundResourcePackPopPacket(Optional.empty()));
        }

        if (this.hasDelayed) {
            this.sendDialog(sender);
            return;
        }
        for (var pack : packs) {
            sender.accept(new ClientboundResourcePackPushPacket(pack.id(), pack.url(), pack.hash(), pack.isRequired(), Optional.ofNullable(pack.prompt())));
        }
    }

    private void sendDialog(Consumer<Packet<?>> sender) {
        if (!AutoHost.config.dialog) {
            return;
        }

        var list = new ArrayList<DialogBody>(4);

        list.add(new PlainMessage(AutoHost.dialogHeader, 300));

        if (AutoHost.config.dialogShowDots) {
            var sb = new StringBuilder();
            var index = ((this.tick - 1) / 2) % 10;

            if (((this.tick - 1) / 2) % 20 >= 10) {
                index = 9 - index;
            }

            for (int i = 1; i < index; i++) {
                sb.append('_');
            }

            if (index > 0) {
                sb.append('o');
            }

            sb.append('O');

            if (sb.length() < 10) {
                sb.append('o');
                while (sb.length() < 10) {
                    sb.append('_');
                }
            }

            list.add(new PlainMessage(Component.literal(sb.toString()).withStyle(ChatFormatting.GRAY), 200));
        }

        list.add(new PlainMessage(PolymerResourcePackMod.STATUS.isEmpty() || !AutoHost.config.dialogShowStatus ? AutoHost.dialogDefaultBody :
                Component.literal(String.join("\n", PolymerResourcePackMod.STATUS
                        .subList(Math.max(PolymerResourcePackMod.STATUS.size() - 6, 0), PolymerResourcePackMod.STATUS.size()))), 300));

        this.statusCount = PolymerResourcePackMod.STATUS.size();
        sender.accept(new ClientboundShowDialogPacket(Holder.direct(new NoticeDialog(
                new CommonDialogData(AutoHost.dialogTitle, Optional.empty(),false, false, DialogAction.CLOSE,
                        list, List.of()),
                new ActionButton(new CommonButtonData(
                        Component.translatable("menu.disconnect"), 150),
                        Optional.of(new CustomAll(DISCONNECT, Optional.empty())))
        ))));
    }

    public void tick(Consumer<Packet<?>> sender) {
        if (this.hasDelayed && this.isReady.getAsBoolean()) {
            if (AutoHost.config.dialog) {
                sender.accept(ClientboundClearDialogPacket.INSTANCE);
            }
            var delayed = this.delayed.get();
            for (var pack : delayed) {
                if (pack.isRequired()) {
                    requiredPacks.add(pack.id());
                }
                waitingFor.add(pack.id());
            }
            for (var pack : packs) {
                sender.accept(new ClientboundResourcePackPushPacket(pack.id(), pack.url(), pack.hash(), pack.isRequired(), Optional.ofNullable(pack.prompt())));
            }
            for (var pack : delayed) {
                sender.accept(new ClientboundResourcePackPushPacket(pack.id(), pack.url(), pack.hash(), pack.isRequired(), Optional.ofNullable(pack.prompt())));
            }
            this.hasDelayed = false;
        } else if (this.hasDelayed && ++this.tick % 2 == 0) {
            sendDialog(sender);
        }
    }

    @Override
    public Type type() {
        return KEY;
    }

    public boolean onStatus(ServerConfigurationPacketListenerImpl handler, UUID id, ServerboundResourcePackPacket.Action status) {
        switch (status) {
            case DECLINED, FAILED_RELOAD, FAILED_DOWNLOAD, INVALID_URL -> {
                if (this.requiredPacks.contains(id)) {
                    handler.disconnect(Component.translatable("multiplayer.requiredTexturePrompt.disconnect"));
                }
            }
        }

        if (status.isTerminal()) {
            this.waitingFor.remove(id);
        }

        return this.waitingFor.isEmpty() && !this.hasDelayed;
    }
}
