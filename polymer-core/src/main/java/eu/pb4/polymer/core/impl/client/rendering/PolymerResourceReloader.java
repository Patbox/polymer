package eu.pb4.polymer.core.impl.client.rendering;

import com.google.gson.Gson;
import com.mojang.blaze3d.systems.RenderSystem;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.core.mixin.client.rendering.ArmorFeatureRendererAccessor;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static eu.pb4.polymer.core.impl.PolymerImplUtils.id;

public record PolymerResourceReloader(TextureManager manager) implements ResourceReloader {
    private static final Gson GSON = new Gson();
    public static final Identifier POLYMER_ARMOR_ID = id("armors.json");

    @Override
    public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        return CompletableFuture.supplyAsync(() -> {
            if (PolymerImpl.USE_ALT_ARMOR_HANDLER) {
                InternalClientRegistry.ARMOR_TEXTURES_1.clear();
                InternalClientRegistry.ARMOR_TEXTURES_2.clear();
                var polymerArmor = manager.getResource(POLYMER_ARMOR_ID);
                if (polymerArmor.isPresent()) {
                    InternalClientRegistry.hasArmorTextures = true;
                    ArmorFeatureRendererAccessor.getARMOR_TEXTURE_CACHE().put("textures/models/armor/leather_layer_1.png", new Identifier("textures/models/armor/vanilla_leather_layer_1.png"));
                    ArmorFeatureRendererAccessor.getARMOR_TEXTURE_CACHE().put("textures/models/armor/leather_layer_1_overlay.png", new Identifier("textures/models/armor/vanilla_leather_layer_1_overlay.png"));
                    ArmorFeatureRendererAccessor.getARMOR_TEXTURE_CACHE().put("textures/models/armor/leather_layer_2.png", new Identifier("textures/models/armor/vanilla_leather_layer_2.png"));
                    ArmorFeatureRendererAccessor.getARMOR_TEXTURE_CACHE().put("textures/models/armor/leather_layer_2_overlay.png", new Identifier("textures/models/armor/vanilla_leather_layer_2_overlay.png"));


                    try {
                        HashMap<String, String> data =  GSON.fromJson(new String(polymerArmor.get().getInputStream().readAllBytes()), HashMap.class);

                        for (var entry : data.entrySet()) {
                            var id = new Identifier(entry.getValue());
                            var key = Integer.parseInt(entry.getKey());
                            var tex1 = new Identifier(id.getNamespace(), "textures/models/armor/" + id.getPath() + "_layer_1.png");
                            var tex2 = new Identifier(id.getNamespace(), "textures/models/armor/" + id.getPath() + "_layer_2.png");
                            InternalClientRegistry.ARMOR_TEXTURES_1.put(key, tex1);
                            InternalClientRegistry.ARMOR_TEXTURES_2.put(key, tex2);
                        }
                    } catch (Exception e) {
                        PolymerImpl.LOGGER.warn("Invalid armors.json file! {}", e);
                    }
                } else {
                    InternalClientRegistry.hasArmorTextures = false;
                    ArmorFeatureRendererAccessor.getARMOR_TEXTURE_CACHE().put("textures/models/armor/leather_layer_1.png", new Identifier("textures/models/armor/leather_layer_1.png"));
                    ArmorFeatureRendererAccessor.getARMOR_TEXTURE_CACHE().put("textures/models/armor/leather_layer_1_overlay.png", new Identifier("textures/models/armor/leather_layer_1_overlay.png"));
                    ArmorFeatureRendererAccessor.getARMOR_TEXTURE_CACHE().put("textures/models/armor/leather_layer_2.png", new Identifier("textures/models/armor/leather_layer_2.png"));
                    ArmorFeatureRendererAccessor.getARMOR_TEXTURE_CACHE().put("textures/models/armor/leather_layer_2_overlay.png", new Identifier("textures/models/armor/leather_layer_2_overlay.png"));

                }
            }
            return null;
        }, prepareExecutor).thenCompose(synchronizer::whenPrepared).thenAcceptAsync(v -> {
            if (PolymerImpl.USE_ALT_ARMOR_HANDLER) {
                for (var id : InternalClientRegistry.ARMOR_TEXTURES_1.values()) {
                    this.manager.registerTexture(id, new PolymerArmorResourceTexture(id));
                }
                for (var id : InternalClientRegistry.ARMOR_TEXTURES_2.values()) {
                    this.manager.registerTexture(id, new PolymerArmorResourceTexture(id));
                }
            }
        }, (runnable) -> {
            Objects.requireNonNull(runnable);
            RenderSystem.recordRenderCall(runnable::run);
        });
    }

    @Override
    public String getName() {
        return "Polymer Resource Reloader";
    }
}
