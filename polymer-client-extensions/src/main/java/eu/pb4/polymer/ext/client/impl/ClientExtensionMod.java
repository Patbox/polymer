package eu.pb4.polymer.ext.client.impl;

import net.fabricmc.api.ModInitializer;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ClientExtensionMod implements ModInitializer {

	@Override
	public void onInitialize() {
		CEServerProtocol.initialize();


		ServerPlayerEntity player = null;

	}
}
