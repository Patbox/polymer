package eu.pb4.polymer.core.mixin.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceOrIdArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Commands.class)
public abstract class CommandsMixin {
    @Inject(method = "argument", at = @At("TAIL"))
    private static void polymer$handleSuggestions(String name, ArgumentType<?> type, CallbackInfoReturnable<RequiredArgumentBuilder<CommandSourceStack, ?>> cir) {
        if (type instanceof ItemArgument || type instanceof BlockStateArgument
                || type instanceof ResourceOrIdArgument<?> || type instanceof ResourceArgument<?>) {
            cir.getReturnValue().suggests(type::listSuggestions);
        }
    }
}
