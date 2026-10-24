package eu.pb4.polymer.autohost.impl;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.impl.PolymerResourcePackMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dialog.*;
import net.minecraft.server.dialog.action.CustomAll;
import net.minecraft.server.dialog.body.DialogBody;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.util.TriState;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AutoHostTask implements ConfigurationTask {
    public static final Type KEY = new Type("polymer:autohost/send_packs");
    public static final Identifier DISCONNECT = Identifier.parse("polymer:autohost/disconnect");
    public static final Identifier JOIN_ON_FAILURE = Identifier.parse("polymer:autohost/join_on_failure");
    private final Collection<MinecraftServer.ServerResourcePackInfo> packs;

    private final Set<UUID> requiredPacks = new HashSet<>();
    private final Set<UUID> waitingFor = new HashSet<>();
    private final Supplier<Collection<MinecraftServer.ServerResourcePackInfo>> delayed;
    private final BooleanSupplier isReady;
    private final Map<UUID, MinecraftServer.ServerResourcePackInfo> map = new HashMap<>();
    private boolean hasDelayed;
    private int tick = 0;
    private int tickAnim = 0;

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
            this.map.put(pack.id(), pack);
            sender.accept(new ClientboundResourcePackPushPacket(pack.id(), pack.url(), pack.hash(), pack.isRequired(), Optional.ofNullable(pack.prompt())));
        }
    }

    private void sendDialog(Consumer<Packet<?>> sender) {
        if (!AutoHost.config.dialog) {
            return;
        }

        var failure = PolymerResourcePackMod.successState == TriState.FALSE;
        var list = new ArrayList<DialogBody>(4);

        if (failure) {
            list.add(new PlainMessage(Component.literal("Failed to generate the resource pack! See logs for more details!").withColor(TextColor.RED), 300));
        } else {
            list.add(new PlainMessage(AutoHost.dialogHeader, 300));
        }

        if (AutoHost.config.dialogShowDots) {
            var o = failure ? 'x' : 'o';
            var O = failure ? 'X' : 'O';

            var sb = new StringBuilder();
            var index = this.tickAnim % 10;

            if (this.tickAnim % 20 >= 10) {
                index = 9 - index;
            }

            for (int i = 1; i < index; i++) {
                sb.append('_');
            }

            if (index > 0) {
                sb.append(o);
            }

            sb.append(O);

            if (sb.length() < 10) {
                sb.append(o);
                while (sb.length() < 10) {
                    sb.append('_');
                }
            }

            list.add(new PlainMessage(Component.literal(sb.toString()).withColor(failure ? TextColor.DARK_RED : TextColor.GRAY), 200));
            if (!failure) {
                this.tickAnim++;
            }
        }

        list.add(new PlainMessage(PolymerResourcePackMod.STATUS.isEmpty() || !AutoHost.config.dialogShowStatus ? AutoHost.dialogDefaultBody :
                Component.literal(String.join("\n", PolymerResourcePackMod.STATUS
                        .subList(Math.max(PolymerResourcePackMod.STATUS.size() - 6, 0), PolymerResourcePackMod.STATUS.size()))), 300));


        if (failure && AutoHost.config.allowJoiningAfterGenerationFailure) {
            sender.accept(new ClientboundShowDialogPacket(Holder.direct(new ConfirmationDialog(
                    new CommonDialogData(AutoHost.dialogTitle, Optional.empty(), false, false, DialogAction.CLOSE,
                            list, List.of()),
                    new ActionButton(new CommonButtonData(
                            Component.literal("Join anyway"), 150),
                            Optional.of(new CustomAll(JOIN_ON_FAILURE, Optional.empty()))),
                    new ActionButton(new CommonButtonData(
                            Component.translatable("menu.disconnect"), 150),
                            Optional.of(new CustomAll(DISCONNECT, Optional.empty())))
            ))));
        } else {
            sender.accept(new ClientboundShowDialogPacket(Holder.direct(new NoticeDialog(
                    new CommonDialogData(AutoHost.dialogTitle, Optional.empty(), false, false, DialogAction.CLOSE,
                            list, List.of()),
                    new ActionButton(new CommonButtonData(
                            Component.translatable("menu.disconnect"), 150),
                            Optional.of(new CustomAll(DISCONNECT, Optional.empty())))
            ))));
        }
    }

    public void tick(Consumer<Packet<?>> sender) {
        if (this.hasDelayed && this.isReady.getAsBoolean()) {
            if (AutoHost.config.dialog) {
                sender.accept(ClientboundClearDialogPacket.INSTANCE);
            }
            var delayed = this.delayed.get();
            for (var pack : delayed) {
                this.map.put(pack.id(), pack);
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
                    var text = AutoHost.disconnectMessage;
                    if (AutoHost.config.informativeDisconnect) {
                        var pack = this.map.get(id);
                        var packInfo = pack == null ? "<Invalid pack?>" : (pack.hash().isEmpty() ? "<NO HASH>" : pack.hash()) + "\n" + pack.url();
                        var main = PolymerResourcePackUtils.getMainUuid().equals(id) ? " (Main)" : "";
                        text = Component.empty().append(text)
                                .append("\n\n")
                                .append(Component.literal(status.name() + " > " + id + main + "\n" + packInfo).withStyle(ChatFormatting.GRAY))
                        ;
                    }
                    handler.disconnect(text);
                }
            }
        }

        if (status.isTerminal()) {
            this.waitingFor.remove(id);
        }

        return this.waitingFor.isEmpty() && !this.hasDelayed;
    }
}
