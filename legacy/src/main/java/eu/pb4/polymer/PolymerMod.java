package eu.pb4.polymer;

import eu.pb4.polymer.block.BlockHelper;
import eu.pb4.polymer.impl.PolymerImpl;
import eu.pb4.polymer.impl.compat.CompatStatus;
import net.fabricmc.api.ModInitializer;

import net.minecraft.util.Identifier;

import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Deprecated
public class PolymerMod implements ModInitializer {
	public static final boolean POLYMC_COMPAT = CompatStatus.POLYMC;

	public static final Logger LOGGER = PolymerImpl.LOGGER;

	@Override
	public void onInitialize() {

	}

	@Deprecated
	public static void registerVirtualBlockEntity(Identifier identifier) {
		BlockHelper.registerVirtualBlockEntity(identifier);
	}

	@Deprecated
	public static boolean isVirtualBlockEntity(Identifier identifier) {
		return BlockHelper.isVirtualBlockEntity(identifier);
	}

	@Deprecated
	public static boolean isVirtualBlockEntity(String identifier) {
		return BlockHelper.isVirtualBlockEntity(identifier);
	}
}
