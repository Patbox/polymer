package eu.pb4.polymer.resourcepack.impl.client.rendering;

import com.google.gson.Gson;
import com.mojang.blaze3d.systems.RenderSystem;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.resourcepack.impl.PolymerResourcePackImpl;
import eu.pb4.polymer.resourcepack.impl.PolymerResourcePackMod;
import eu.pb4.polymer.resourcepack.mixin.client.ArmorFeatureRendererAccessor;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.HashMap;
import java.util.List;
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
                PolymerResourcePackMod.ARMOR_TEXTURES.clear();
                var polymerArmor = manager.getResource(POLYMER_ARMOR_ID);
                if (polymerArmor.isPresent()) {
                    PolymerResourcePackMod.hasArmorTextures = true;
                    try {
                        HashMap<String, String> data =  GSON.fromJson(new String(polymerArmor.get().getInputStream().readAllBytes()), HashMap.class);

                        for (var entry : data.entrySet()) {
                            var id = Identifier.of(entry.getValue());
                            var key = Integer.parseInt(entry.getKey());
                            PolymerResourcePackMod.ARMOR_TEXTURES.put(key, List.of(
                                    new ArmorMaterial.Layer(id, "", false)
                            ));
                        }
                    } catch (Exception e) {
                        CommonImpl.LOGGER.warn("Invalid armors.json file!", e);
                    }
                } else {
                    PolymerResourcePackMod.hasArmorTextures = false;
                }
            }
            return null;
        }, prepareExecutor).thenCompose(synchronizer::whenPrepared).thenAcceptAsync(v -> {
            if (PolymerResourcePackImpl.USE_ALT_ARMOR_HANDLER) {
                for (var id : PolymerResourcePackMod.ARMOR_TEXTURES.values()) {
                    var first = id.getFirst().getTexture(false);
                    var second = id.getFirst().getTexture(true);
                    this.manager.registerTexture(first, new PolymerArmorResourceTexture(first));
                    this.manager.registerTexture(second, new PolymerArmorResourceTexture(second));
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
