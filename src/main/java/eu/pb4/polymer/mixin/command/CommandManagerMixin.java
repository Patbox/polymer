package eu.pb4.polymer.mixin.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.command.argument.EntitySummonArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CommandManager.class)
public abstract class CommandManagerMixin {
    /**
     * Replaces client hardcoded entity argument type to Identifier to stop custom entities from being red (while keeping server side validation from EntitySummonArgumentType.getEntitySummon)
     */
    @ModifyVariable(method = "argument", at = @At("HEAD"), ordinal = 0)
    private static ArgumentType<?> replaceArgumentType(ArgumentType<?> type) {
        if (type instanceof EntitySummonArgumentType) {
            return IdentifierArgumentType.identifier();
        }

        return type;
    }

    @Inject(method = "argument", at = @At("TAIL"), cancellable = true)
    private static void makeSuggestionsServerSide(String name, ArgumentType<?> type, CallbackInfoReturnable<RequiredArgumentBuilder<ServerCommandSource, ?>> cir) {
        if (type instanceof ItemStackArgumentType || type instanceof BlockStateArgumentType) {
            cir.setReturnValue(cir.getReturnValue().suggests(type::listSuggestions));
        }
    }
}
