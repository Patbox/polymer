package eu.pb4.polymertest;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.core.api.other.PolymerMapCodec;
import net.minecraft.server.dialog.*;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.dialog.action.Action;
import net.minecraft.server.dialog.body.PlainMessage;

public record TestDialog() implements Dialog {
    public static final MapCodec<TestDialog> CODEC = PolymerMapCodec.ofDialog(MapCodec.unit(TestDialog::new), ((data, context) -> {
        return new NoticeDialog(data.common(), new ActionButton(new CommonButtonData(Component.literal("It worked!"), 80), data.onCancel()));
    }));

    @Override
    public CommonDialogData common() {
        PacketContext.get().getGameProfile();
        return new CommonDialogData(Component.literal("Test Dialog"), Optional.empty(), true, true, DialogAction.CLOSE,
                List.of(new PlainMessage(Component.nullToEmpty(PacketContext.get().getGameProfile() != null ? PacketContext.get().getGameProfile().name() : "<NULL>"), 500)), List.of());
    }

    @Override
    public MapCodec<? extends Dialog> codec() {
        return CODEC;
    }

    @Override
    public Optional<Action> onCancel() {
        return Optional.empty();
    }
}
