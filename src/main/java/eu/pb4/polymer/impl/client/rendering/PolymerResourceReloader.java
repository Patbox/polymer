package eu.pb4.polymer.impl.client.rendering;

import eu.pb4.polymer.impl.PolymerImpl;
import eu.pb4.polymer.impl.PolymerMod;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import eu.pb4.polymer.impl.resourcepack.DefaultRPBuilder;
import eu.pb4.polymer.mixin.client.rendering.ArmorFeatureRendererAccessor;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static eu.pb4.polymer.impl.PolymerImpl.id;

public record PolymerResourceReloader(TextureManager manager) implements ResourceReloader {
    public static final Identifier POLYMER_ARMOR_ID = id("armors.json");

    @Override
    public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        return CompletableFuture.supplyAsync(() -> {
            if (PolymerImpl.USE_ALT_ARMOR_HANDLER) {
                InternalClientRegistry.ARMOR_TEXTURES_1.clear();
                InternalClientRegistry.ARMOR_TEXTURES_2.clear();
                ArmorFeatureRendererAccessor.getARMOR_TEXTURE_CACHE().clear();
                ArmorFeatureRendererAccessor.getARMOR_TEXTURE_CACHE().put("textures/models/armor/leather_layer_1.png", new Identifier("textures/models/armor/vanilla_leather_layer_1.png"));
                ArmorFeatureRendererAccessor.getARMOR_TEXTURE_CACHE().put("textures/models/armor/leather_layer_1_overlay.png", new Identifier("textures/models/armor/vanilla_leather_layer_1_overlay.png"));
                ArmorFeatureRendererAccessor.getARMOR_TEXTURE_CACHE().put("textures/models/armor/leather_layer_2.png", new Identifier("textures/models/armor/vanilla_leather_layer_2.png"));
                ArmorFeatureRendererAccessor.getARMOR_TEXTURE_CACHE().put("textures/models/armor/leather_layer_2_overlay.png", new Identifier("textures/models/armor/vanilla_leather_layer_2_overlay.png"));
                if (manager.containsResource(POLYMER_ARMOR_ID)) {
                    try {
                        HashMap<String, String> data = DefaultRPBuilder.GSON.fromJson(new String(manager.getResource(POLYMER_ARMOR_ID).getInputStream().readAllBytes()), HashMap.class);

                        for (var entry : data.entrySet()) {
                            var id = new Identifier(entry.getValue());
                            var key = Integer.parseInt(entry.getKey());
                            var tex1 = new Identifier(id.getNamespace(), "textures/models/armor/" + id.getPath() + "_layer_1.png");
                            var tex2 = new Identifier(id.getNamespace(), "textures/models/armor/" + id.getPath() + "_layer_2.png");
                            InternalClientRegistry.ARMOR_TEXTURES_1.put(key, tex1);
                            InternalClientRegistry.ARMOR_TEXTURES_2.put(key, tex2);
                        }
                    } catch (Exception e) {
                        PolymerImpl.LOGGER.warn("Invalid armors.json file!");
                        PolymerImpl.LOGGER.warn(e);
                    }
                }
            }
            return null;
        }, prepareExecutor).thenCompose(synchronizer::whenPrepared).thenAcceptAsync(v -> {
            if (PolymerImpl.USE_ALT_ARMOR_HANDLER) {
                for (var id : InternalClientRegistry.ARMOR_TEXTURES_1.values()) {
                    this.manager.registerTexture(id, new AnimatedResourceTexture(id));
                }
                for (var id : InternalClientRegistry.ARMOR_TEXTURES_2.values()) {
                    this.manager.registerTexture(id, new AnimatedResourceTexture(id));
                }
            }
        });
    }

    @Override
    public String getName() {
        return "Polymer Resource Reloader";
    }
}
