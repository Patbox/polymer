package eu.pb4.polymer.virtualentity.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import net.fabricmc.api.ModInitializer;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;


@ApiStatus.Internal
public class VirtualEntityMod implements ModInitializer {
	@Override
	public void onInitialize() {
		CommonImplUtils.registerDevCommands(this::commands);
	}

	private void commands(LiteralArgumentBuilder<ServerCommandSource> builder, CommandRegistryAccess commandRegistryAccess) {
		builder.then(literal("ve_blockbound").then(argument("pos", BlockPosArgumentType.blockPos()).executes((ctx) -> {
			var b = BlockBoundAttachment.get(ctx.getSource().getWorld(), BlockPosArgumentType.getBlockPos(ctx, "pos"));

			if (b == null) {
				ctx.getSource().sendFeedback(() -> Text.literal("No block bound!"), false);
			} else {
				ctx.getSource().sendFeedback(() -> Text.literal("Found: " + b.holder()), false);
				for (var e : b.holder().getElements()) {
					ctx.getSource().sendFeedback(() -> Text.literal("- " + e), false);
				}
			}


			return b != null ? b.holder().getElements().size() : -1;
		})));
	}
}
