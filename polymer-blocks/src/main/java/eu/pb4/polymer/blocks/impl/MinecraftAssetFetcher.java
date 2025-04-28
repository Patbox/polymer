package eu.pb4.polymer.blocks.impl;

import com.google.gson.Gson;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class MinecraftAssetFetcher {
    private static final HttpClient http = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();

    private static final String VERSION_MANIFEST_URL =
            "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    private static final String ASSET_CDN_BASE =
            "https://resources.download.minecraft.net/";

    public static byte @Nullable [] fetchSoundsJsonForVersion(String versionId) throws IOException, InterruptedException {
        VersionManifest manifest = fetchJson(VERSION_MANIFEST_URL, VersionManifest.class);

        VersionEntry version = manifest.versions.stream()
                .filter(v -> v.id.equals(versionId))
                .findFirst()
                .orElseThrow(() -> new IOException("Version not found: " + versionId));

        VersionMetadata metadata = fetchJson(version.url, VersionMetadata.class);
        String assetIndexUrl = metadata.assetIndex.url;

        AssetIndex assetIndex = fetchJson(assetIndexUrl, AssetIndex.class);
        AssetObject sounds = assetIndex.objects.get("minecraft/sounds.json");

        if (sounds == null) {
            return null;
        }

        String hash = sounds.hash;
        String assetUrl = ASSET_CDN_BASE + hash.substring(0, 2) + "/" + hash;

        return downloadBytes(assetUrl);
    }

    private static <T> T fetchJson(String uri, Class<T> type) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(uri)).build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        return gson.fromJson(response.body(), type);
    }

    private static byte[] downloadBytes(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).build();
        HttpResponse<InputStream> response = http.send(request, HttpResponse.BodyHandlers.ofInputStream());

        try (InputStream in = response.body();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            in.transferTo(out);
            return out.toByteArray();
        }
    }

    static class VersionManifest {
        List<VersionEntry> versions;
    }

    static class VersionEntry {
        String id;
        String url;
    }

    static class VersionMetadata {
        AssetIndexRef assetIndex;
    }

    static class AssetIndexRef {
        String url;
    }

    static class AssetIndex {
        Map<String, AssetObject> objects;
    }

    static class AssetObject {
        String hash;
    }
}
