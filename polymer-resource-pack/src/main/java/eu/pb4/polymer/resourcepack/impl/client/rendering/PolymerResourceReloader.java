package eu.pb4.polymer.resourcepack.impl.client.rendering;

import com.google.gson.Gson;
import com.mojang.blaze3d.systems.RenderSystem;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.resourcepack.impl.PolymerResourcePackImpl;
import eu.pb4.polymer.resourcepack.impl.PolymerResourcePackMod;
import eu.pb4.polymer.resourcepack.mixin.client.ArmorFeatureRendererAccessor;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


public record PolymerResourceReloader(TextureManager manager) implements ResourceReloader {
    private static final Gson GSON = new Gson();
    public static final Identifier POLYMER_ARMOR_ID = CommonImplUtils.id("armors.json");

    @Override
    public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        return CompletableFuture.supplyAsync(() -> {
            if (PolymerResourcePackImpl.USE_ALT_ARMOR_HANDLER) {
                PolymerResourcePackMod.ARMOR_TEXTURES_1.clear();
                PolymerResourcePackMod.ARMOR_TEXTURES_2.clear();
                var polymerArmor = manager.getResource(POLYMER_ARMOR_ID);
                if (polymerArmor.isPresent()) {
                    PolymerResourcePackMod.hasArmorTextures = true;
                    ArmorFeatureRendererAccessor.getARMOR_TEXTURE_CACHE().put("textures/models/armor/leather_layer_1.png", Identifier.of("textures/models/armor/vanilla_leather_layer_1.png"));
                    ArmorFeatureRendererAccessor.getARMOR_TEXTURE_CACHE().put("textures/models/armor/leather_layer_1_overlay.png", Identifier.of("textures/models/armor/vanilla_leather_layer_1_overlay.png"));
                    ArmorFeatureRendererAccessor.getARMOR_TEXTURE_CACHE().put("textures/models/armor/leather_layer_2.png", Identifier.of("textures/models/armor/vanilla_leather_layer_2.png"));
                    ArmorFeatureRendererAccessor.getARMOR_TEXTURE_CACHE().put("textures/models/armor/leather_layer_2_overlay.png", Identifier.of("textures/models/armor/vanilla_leather_layer_2_overlay.png"));


                    try {
                        HashMap<String, String> data =  GSON.fromJson(new String(polymerArmor.get().getInputStream().readAllBytes()), HashMap.class);

                        for (var entry : data.entrySet()) {
                            var id = Identifier.of(entry.getValue());
                            var key = Integer.parseInt(entry.getKey());
                            var tex1 = Identifier.of(id.getNamespace(), "textures/models/armor/" + id.getPath() + "_layer_1.png");
                            var tex2 = Identifier.of(id.getNamespace(), "textures/models/armor/" + id.getPath() + "_layer_2.png");
                            PolymerResourcePackMod.ARMOR_TEXTURES_1.put(key, tex1);
                            PolymerResourcePackMod.ARMOR_TEXTURES_2.put(key, tex2);
                        }
                    } catch (Exception e) {
                        CommonImpl.LOGGER.warn("Invalid armors.json file!", e);
                    }
                } else {
                    PolymerResourcePackMod.hasArmorTextures = false;
                    ArmorFeatureRendererAccessor.getARMOR_TEXTURE_CACHE().put("textures/models/armor/leather_layer_1.png", Identifier.of("textures/models/armor/leather_layer_1.png"));
                    ArmorFeatureRendererAccessor.getARMOR_TEXTURE_CACHE().put("textures/models/armor/leather_layer_1_overlay.png", Identifier.of("textures/models/armor/leather_layer_1_overlay.png"));
                    ArmorFeatureRendererAccessor.getARMOR_TEXTURE_CACHE().put("textures/models/armor/leather_layer_2.png", Identifier.of("textures/models/armor/leather_layer_2.png"));
                    ArmorFeatureRendererAccessor.getARMOR_TEXTURE_CACHE().put("textures/models/armor/leather_layer_2_overlay.png", Identifier.of("textures/models/armor/leather_layer_2_overlay.png"));

                }
            }
            return null;
        }, prepareExecutor).thenCompose(synchronizer::whenPrepared).thenAcceptAsync(v -> {
            if (PolymerResourcePackImpl.USE_ALT_ARMOR_HANDLER) {
                for (var id : PolymerResourcePackMod.ARMOR_TEXTURES_1.values()) {
                    this.manager.registerTexture(id, new PolymerArmorResourceTexture(id));
                }
                for (var id : PolymerResourcePackMod.ARMOR_TEXTURES_2.values()) {
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
        return "Polymer Resource Pack Resource Reloader";
    }
}
