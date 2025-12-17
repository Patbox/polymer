package eu.pb4.polymer.virtualentity.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import net.fabricmc.api.ModInitializer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;


@ApiStatus.Internal
public class VirtualEntityMod implements ModInitializer {
	@Override
	public void onInitialize() {
		CommonImplUtils.registerDevCommands(this::commands);
	}

	private void commands(LiteralArgumentBuilder<CommandSourceStack> builder, CommandBuildContext commandRegistryAccess) {
		builder.then(literal("ve_blockbound").then(argument("pos", BlockPosArgument.blockPos()).executes((ctx) -> {
			var b = BlockBoundAttachment.get(ctx.getSource().getLevel(), BlockPosArgument.getBlockPos(ctx, "pos"));

			if (b == null) {
				ctx.getSource().sendSuccess(() -> Component.literal("No block bound!"), false);
			} else {
				ctx.getSource().sendSuccess(() -> Component.literal("Found: " + b.holder()), false);
				for (var e : b.holder().getElements()) {
					ctx.getSource().sendSuccess(() -> Component.literal("- " + e), false);
				}
			}


			return b != null ? b.holder().getElements().size() : -1;
		})));
	}
}
