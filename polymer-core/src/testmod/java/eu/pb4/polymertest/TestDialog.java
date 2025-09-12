package eu.pb4.polymertest;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.core.api.other.PolymerMapCodec;
import net.minecraft.dialog.AfterAction;
import net.minecraft.dialog.DialogActionButtonData;
import net.minecraft.dialog.DialogButtonData;
import net.minecraft.dialog.DialogCommonData;
import net.minecraft.dialog.action.DialogAction;
import net.minecraft.dialog.body.PlainMessageDialogBody;
import net.minecraft.dialog.type.Dialog;
import net.minecraft.dialog.type.NoticeDialog;
import net.minecraft.text.Text;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.Optional;

public record TestDialog() implements Dialog {
    public static final MapCodec<TestDialog> CODEC = PolymerMapCodec.ofDialog(MapCodec.unit(TestDialog::new), ((data, context) -> {
        return new NoticeDialog(data.common(), new DialogActionButtonData(new DialogButtonData(Text.literal("It worked!"), 80), data.getCancelAction()));
    }));

    @Override
    public DialogCommonData common() {
        PacketContext.get().getGameProfile();
        return new DialogCommonData(Text.literal("Test Dialog"), Optional.empty(), true, true, AfterAction.CLOSE,
                List.of(new PlainMessageDialogBody(Text.of(PacketContext.get().getGameProfile() != null ? PacketContext.get().getGameProfile().name() : "<NULL>"), 500)), List.of());
    }

    @Override
    public MapCodec<? extends Dialog> getCodec() {
        return CODEC;
    }

    @Override
    public Optional<DialogAction> getCancelAction() {
        return Optional.empty();
    }
}
