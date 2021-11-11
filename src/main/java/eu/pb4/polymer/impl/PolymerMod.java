package eu.pb4.polymer.impl;

import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import eu.pb4.polymer.impl.client.rendering.PolymerResourcePack;
import eu.pb4.polymer.impl.client.compat.ReiCompatibility;
import eu.pb4.polymer.impl.compat.CompatStatus;
import eu.pb4.polymer.impl.compat.polymc.PolyMcHelpers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;


import net.fabricmc.loader.api.ModContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class PolymerMod implements ModInitializer, ClientModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("Polymer");
	public static final ModContainer CONTAINER = FabricLoader.getInstance().getModContainer("polymer").get();
	public static final String VERSION = CONTAINER.getMetadata().getVersion().getFriendlyString().split("\\+")[0];

	@Override
	public void onInitialize() {
		if (CompatStatus.POLYMC) {
			PolymerRPUtils.markAsRequired();
			ServerLifecycleEvents.SERVER_STARTED.register(PolyMcHelpers::overrideCommand);
		}
	}

	@Override
	public void onInitializeClient() {
		PolymerResourcePack.setup();
		if (FabricLoader.getInstance().isModLoaded("roughlyenoughitems")) {
			ReiCompatibility.registerEvents();
		}
	}
}
