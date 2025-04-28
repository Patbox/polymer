package eu.pb4.polymer.blocks.impl;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.extras.api.format.sound.SoundEntry;
import eu.pb4.polymer.resourcepack.extras.api.format.sound.SoundsAsset;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class PolymerBlockSounds {
    public static final Reference2ObjectOpenHashMap<BlockSoundGroup, BlockSoundGroup> REMIXES = new Reference2ObjectOpenHashMap<>();
    public static final ReferenceOpenHashSet<BlockSoundGroup> SOUND_TYPES = new ReferenceOpenHashSet<>();

    static String PATH = "assets/minecraft/sounds.json";
    static String VERSION = "1.21.5";

    static {
        init();
    }

    private static void init() {
        Path outputPath = Path.of("polymer/sounds-" + VERSION + ".json");
        byte[] data = null;
        try {
            if (Files.exists(outputPath)) {
                data = Files.readAllBytes(outputPath);
            } else {
                data = MinecraftAssetFetcher.fetchSoundsJsonForVersion("1.21.5");
                if (data == null)
                    return;

                Files.write(outputPath, data);
            }
        } catch (Exception ignored) {}

        if (data != null) {
            SoundsAsset.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseReader(new InputStreamReader(new ByteArrayInputStream(data)))).ifSuccess(pair -> {
                SoundsAsset soundsAsset = pair.getFirst();
                addData(soundsAsset);
            });
        }
    }

    private static void addData(SoundsAsset vanillaSounds) {
        PolymerResourcePackUtils.RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT.register(resourcePackBuilder -> {
            if (!REMIXES.isEmpty())
                return;

            SoundEntry empty = SoundEntry.builder().replace(true).build();
            SoundsAsset newSounds = new SoundsAsset(new Object2ObjectArrayMap<>());

            String oldData = resourcePackBuilder.getStringDataOrSource(PATH);
            if (oldData != null) {
                SoundsAsset oldSoundAsset = SoundsAsset.fromJson(oldData);
                if (oldSoundAsset != null) {
                    newSounds.sounds().putAll(oldSoundAsset.sounds());
                }
            }

            for (BlockSoundGroup soundType : SOUND_TYPES) {
                newSounds.sounds().put(soundType.getBreakSound().id().getPath(), empty);
                newSounds.sounds().put(soundType.getStepSound().id().getPath(), empty);
                newSounds.sounds().put(soundType.getHitSound().id().getPath(), empty);
                newSounds.sounds().put(soundType.getFallSound().id().getPath(), empty);

                Identifier breakId = serversideId(soundType.getBreakSound().id());
                Identifier stepId = serversideId(soundType.getStepSound().id());
                Identifier hitId = serversideId(soundType.getHitSound().id());
                Identifier fallId = serversideId(soundType.getFallSound().id());

                newSounds.sounds().put(breakId.getPath(), vanillaSounds.sounds().get(soundType.getBreakSound().id().getPath()));
                newSounds.sounds().put(stepId.getPath(), vanillaSounds.sounds().get(soundType.getStepSound().id().getPath()));
                newSounds.sounds().put(hitId.getPath(), vanillaSounds.sounds().get(soundType.getHitSound().id().getPath()));
                newSounds.sounds().put(fallId.getPath(), vanillaSounds.sounds().get(soundType.getFallSound().id().getPath()));

                REMIXES.put(soundType, new BlockSoundGroup(
                        soundType.getVolume(),
                        soundType.getPitch(),
                        SoundEvent.of(breakId),
                        SoundEvent.of(stepId),
                        SoundEvent.of(soundType.getPlaceSound().id()), // no need to mess with place sounds
                        SoundEvent.of(hitId),
                        SoundEvent.of(fallId)
                ));
            }
            resourcePackBuilder.addData(PATH, newSounds.toBytes());
        });
    }

    private static Identifier serversideId(Identifier resourceLocation) {
        return resourceLocation.withSuffixedPath(".serverside");
    }
}
