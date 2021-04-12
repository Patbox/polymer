package eu.pb4.polymer;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashSet;

public class PolymerMod implements ModInitializer {
	private static HashSet<Identifier> RECIPE_IDENTIFIERS = new HashSet<>();
	private static HashSet<String> BLOCK_ENTITY_IDENTIFIERS = new HashSet<>();

	@Override
	public void onInitialize() {
	}

	public static void registerVirtualRecipeSerializer(Identifier identifier) {
		RECIPE_IDENTIFIERS.add(identifier);
	}

	public static boolean isVirtualRecipeSerializer(Identifier identifier) {
		return RECIPE_IDENTIFIERS.contains(identifier);
	}

	public static void registerVirtualBlockEntity(Identifier identifier) {
		BLOCK_ENTITY_IDENTIFIERS.add(identifier.toString());
	}

	public static boolean isVirtualBlockEntity(Identifier identifier) {
		return BLOCK_ENTITY_IDENTIFIERS.contains(identifier.toString());
	}

	public static boolean isVirtualBlockEntity(String identifier) {
		return BLOCK_ENTITY_IDENTIFIERS.contains(identifier);
	}
}
