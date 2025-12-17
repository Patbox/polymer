package eu.pb4.polymer.soundpatcher.impl;

import eu.pb4.polymer.resourcepack.api.AssetPaths;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.extras.api.format.sound.SoundEntry;
import eu.pb4.polymer.resourcepack.extras.api.format.sound.SoundsAsset;
import java.util.List;
import net.minecraft.resources.Identifier;

public class SoundResourceGenerator {
    public static final String NAMESPACE = "polymer-sp";
    private static SoundsAsset.Builder generatedAsset;
    private static SoundsAsset.Builder generatedAssetVanilla;

    public static void moveSoundEvent(String vanillaPath, String newPath) {
        if (generatedAsset == null) {
            generatedAsset = SoundsAsset.builder();
            generatedAssetVanilla = SoundsAsset.builder();
            PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register(x -> {
                x.addData(AssetPaths.soundsAsset(NAMESPACE), generatedAsset.build());
                x.addData(AssetPaths.soundsAsset(Identifier.DEFAULT_NAMESPACE), generatedAssetVanilla.build());
            });
        }
        generatedAssetVanilla.add(vanillaPath, new SoundEntry(true, List.of()));
        generatedAsset.add(newPath, VanillaSoundJson.getSoundAsset().sounds().getOrDefault(vanillaPath, new SoundEntry(true, List.of())));
    }
}
