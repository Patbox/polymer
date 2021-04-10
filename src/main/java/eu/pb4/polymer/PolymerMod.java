package eu.pb4.polymer;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

import java.util.HashSet;

public class PolymerMod implements ModInitializer {
	private static HashSet<Identifier> RECIPE_IDENTIFIERS = new HashSet<>();

	@Override
	public void onInitialize() {
	}

	public static void registerVirtualRecipeSerializer(Identifier identifier) {
		RECIPE_IDENTIFIERS.add(identifier);
	}

	public static boolean isVirtualRecipeSerializer(Identifier identifier) {
		return RECIPE_IDENTIFIERS.contains(identifier);
	}
}
