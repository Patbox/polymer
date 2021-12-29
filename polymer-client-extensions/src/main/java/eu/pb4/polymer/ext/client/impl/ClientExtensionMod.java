package eu.pb4.polymer.ext.client.impl;

import net.fabricmc.api.ModInitializer;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ClientExtensionMod implements ModInitializer {

	@Override
	public void onInitialize() {
		CEProtocolServer.initialize();
	}
}
