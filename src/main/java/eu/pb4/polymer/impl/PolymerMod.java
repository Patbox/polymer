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

import org.jetbrains.annotations.ApiStatus;


@ApiStatus.Internal
public class PolymerMod implements ModInitializer, ClientModInitializer {
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
