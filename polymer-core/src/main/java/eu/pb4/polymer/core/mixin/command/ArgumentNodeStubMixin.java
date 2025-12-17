package eu.pb4.polymer.core.mixin.command;

import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = "net/minecraft/network/protocol/game/ClientboundCommandsPacket$ArgumentNodeStub")
public class ArgumentNodeStubMixin {
    @Unique
    private static final Identifier SUMMONABLE_ENTITIES = Identifier.withDefaultNamespace("summonable_entities");
    @Unique
    private static final Identifier ASK_SERVER = Identifier.withDefaultNamespace("ask_server");

    @ModifyArg(method = "write(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;writeIdentifier(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/network/FriendlyByteBuf;"))
    private Identifier polymer$changeId(Identifier id) {
        if (id.equals(SUMMONABLE_ENTITIES)) {
            return ASK_SERVER;
        }
        return id;
    }
}
