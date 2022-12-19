package eu.pb4.polymer.core.mixin.command;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.suggestion.SuggestionProviders;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = "net/minecraft/network/packet/s2c/play/CommandTreeS2CPacket$ArgumentNode")
public class ArgumentNodeMixin {
    @ModifyArg(method = "computeId", at = @At(value = "INVOKE", target = "Lnet/minecraft/command/suggestion/SuggestionProviders;computeId(Lcom/mojang/brigadier/suggestion/SuggestionProvider;)Lnet/minecraft/util/Identifier;"))
    private static SuggestionProvider<CommandSource> polymer$changeId(SuggestionProvider<CommandSource> provider) {
        if (provider.equals(SuggestionProviders.SUMMONABLE_ENTITIES)) {
            return SuggestionProviders.ASK_SERVER;
        }
        return provider;
    }
}
