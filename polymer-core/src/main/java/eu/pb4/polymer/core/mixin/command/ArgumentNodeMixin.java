package eu.pb4.polymer.core.mixin.command;

import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = "net/minecraft/network/packet/s2c/play/CommandTreeS2CPacket$ArgumentNode")
public class ArgumentNodeMixin {
    @Unique
    private static final Identifier SUMMONABLE_ENTITIES = Identifier.ofVanilla("summonable_entities");
    @Unique
    private static final Identifier ASK_SERVER = Identifier.ofVanilla("ask_server");

    @ModifyArg(method = "write(Lnet/minecraft/network/PacketByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeIdentifier(Lnet/minecraft/util/Identifier;)Lnet/minecraft/network/PacketByteBuf;"))
    private Identifier polymer$changeId(Identifier id) {
        if (id.equals(SUMMONABLE_ENTITIES)) {
            return ASK_SERVER;
        }
        return id;
    }
}
