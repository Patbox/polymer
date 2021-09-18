package eu.pb4.polymer;

import eu.pb4.polymer.block.BlockHelper;
import eu.pb4.polymer.other.polymc.PolyMcHelpers;
import eu.pb4.polymer.resourcepack.ResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;


import net.minecraft.util.Identifier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashSet;

@ApiStatus.Internal
public class PolymerMod implements ModInitializer {
	public static final boolean POLYMC_COMPAT = FabricLoader.getInstance().isModLoaded("polymc");

	public static final Logger LOGGER = LogManager.getLogger("Polymer");
	//public static final String VERSION = FabricLoader.getInstance().getModContainer("polymer").get().getMetadata().getVersion().getFriendlyString();

	@Override
	public void onInitialize() {
		if (POLYMC_COMPAT) {
			ResourcePackUtils.markAsRequired();

			// While rest of code doesn't depend on Fabric API in anyway, usage here is still fine, as PolyMC requires it
			ServerLifecycleEvents.SERVER_STARTED.register((s) -> PolyMcHelpers.overrideCommand(s));
		}
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
