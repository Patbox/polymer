package eu.pb4.polymer.impl;

import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import eu.pb4.polymer.impl.client.PolymerResourcePack;
import eu.pb4.polymer.impl.compat.ReiCompatibility;
import eu.pb4.polymer.impl.compat.polymc.PolyMcHelpers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;


import net.minecraft.resource.ResourcePack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class PolymerMod implements ModInitializer, ClientModInitializer {
	public static final boolean POLYMC_COMPAT = FabricLoader.getInstance().isModLoaded("polymc");

	public static final Logger LOGGER = LogManager.getLogger("Polymer");
	public static final String VERSION = FabricLoader.getInstance().getModContainer("polymer").get().getMetadata().getVersion().getFriendlyString().split("\\+")[0];
	public static final int PROTOCOL_VERSION = 0;

	@Override
	public void onInitialize() {
		if (POLYMC_COMPAT) {
			PolymerRPUtils.markAsRequired();
			ServerLifecycleEvents.SERVER_STARTED.register((s) -> PolyMcHelpers.overrideCommand(s));
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
