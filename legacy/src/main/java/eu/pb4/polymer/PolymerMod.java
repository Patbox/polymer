package eu.pb4.polymer;

import eu.pb4.polymer.block.BlockHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;


import net.minecraft.util.Identifier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Deprecated
public class PolymerMod implements ModInitializer {
	public static final boolean POLYMC_COMPAT = FabricLoader.getInstance().isModLoaded("polymc");

	public static final Logger LOGGER = LogManager.getLogger("Polymer (Legacy)");

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
