package eu.pb4.polymer;

import eu.pb4.polymer.resourcepack.ResourcePackUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;


import net.minecraft.util.Identifier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;

public class PolymerMod implements ModInitializer {
	private static HashSet<String> BLOCK_ENTITY_IDENTIFIERS = new HashSet<>();
	public static final boolean POLYMC_COMPAT = FabricLoader.getInstance().isModLoaded("polymc");

	public static final Logger LOGGER = LogManager.getLogger("Polymer");
	//public static final String VERSION = FabricLoader.getInstance().getModContainer("polymer").get().getMetadata().getVersion().getFriendlyString();

	@Override
	public void onInitialize() {
		if (POLYMC_COMPAT) {
			ResourcePackUtils.markAsRequired();
		}
	}

	/**
	 * Marks BlockEntity type as server-side only
	 *
	 * @param identifier BlockEntity's Identifier
	 */
	public static void registerVirtualBlockEntity(Identifier identifier) {
		BLOCK_ENTITY_IDENTIFIERS.add(identifier.toString());
	}

	/**
	 * Checks if BlockEntity is server-side only
	 *
	 * @param identifier BlockEntity's Identifier
	 */
	public static boolean isVirtualBlockEntity(Identifier identifier) {
		return BLOCK_ENTITY_IDENTIFIERS.contains(identifier.toString());
	}

	/**
	 * Checks if BlockEntity is server-side only
	 *
	 * @param identifier BlockEntity's Identifier (as string)
	 */
	public static boolean isVirtualBlockEntity(String identifier) {
		return BLOCK_ENTITY_IDENTIFIERS.contains(identifier);
	}
}
