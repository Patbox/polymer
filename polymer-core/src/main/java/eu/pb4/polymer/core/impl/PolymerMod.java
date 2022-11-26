package eu.pb4.polymer.core.impl;

import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.core.impl.client.compat.FabricFluids;
import eu.pb4.polymer.core.impl.client.compat.ReiCompatibility;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import org.jetbrains.annotations.ApiStatus;


@ApiStatus.Internal
public class PolymerMod implements ModInitializer, ClientModInitializer {
	@Override
	public void onInitialize() {
		CommonImplUtils.registerCommands(Commands::register);
		CommonImplUtils.registerDevCommands(Commands::registerDev);
	}

	@Override
	public void onInitializeClient() {
		if (CompatStatus.REI) {
			ReiCompatibility.registerEvents();
		}

		if (CompatStatus.FABRIC_FLUID_RENDERERING) {
			FabricFluids.register();
		}
	}
}
