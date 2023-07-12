package eu.pb4.polymer.resourcepack.impl.client.rendering;

import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.JsonOps;
import eu.pb4.polymer.resourcepack.impl.ArmorTextureMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.client.texture.TextureTickListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;

public class PolymerArmorResourceTexture extends ResourceTexture implements TextureTickListener {
    public int width = -1;
    public int height = -1;
    public int index = 0;
    private ArmorTextureMetadata metadata = ArmorTextureMetadata.DEFAULT;
    private NativeImage baseTexture;
    private NativeImage altTexture;
    private boolean animation;
    private int nextIndex;
    private long startingTime;

    public PolymerArmorResourceTexture(Identifier location) {
        super(location);
    }

    @Override
    public void load(ResourceManager manager) throws IOException {
        ResourceTexture.TextureData textureData = this.loadTextureData(manager);
        textureData.checkException();

        var id = new Identifier(this.location.getNamespace(), this.location.getPath().substring(0, (this.location.getPath().length() - 3)) + "polymer.json");

        var res = manager.getResource(id);
        if (res.isPresent()) {
            var val = ArmorTextureMetadata.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(new String(res.get().getInputStream().readAllBytes()))).result();

            if (val.isPresent()){
                this.metadata = val.get().getFirst();
            }

        }

        this.width = 64 * this.metadata.scale();
        this.height = 32 * this.metadata.scale();
        this.animation = this.metadata.frames() > 1;

        NativeImage nativeImage = textureData.getImage();

        this.baseTexture = nativeImage;
        if (this.metadata.interpolate()) {
            this.altTexture = new NativeImage(this.width, this.height, false);
        }

        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> this.upload(nativeImage));
        } else {
            this.upload(nativeImage);
        }
    }

    private void upload(NativeImage nativeImage) {
        TextureUtil.prepareImage(this.getGlId(), 0, this.width, this.height);
        nativeImage.upload(0, 0, 0, 0, 0, this.width, this.height, false, false, false, false);
    }

    private void update(NativeImage nativeImage, int index) {
        nativeImage.upload(0, 0, 0, 0, index * this.height, this.width, this.height, false, false, false, false);
    }

    public void baseAnimationTick() {
        var currentTime = System.currentTimeMillis();
        if (this.startingTime == 0) {
            this.startingTime = currentTime;
        }
        int delta = (int) (currentTime - this.startingTime);
        if (delta >= 40) {
            tickAnimation(currentTime);
        }
    }

    public void tickAnimation(long currentTime) {
        int frameTime = 40 * this.metadata.animationSpeed();
        long timeDiff = currentTime - this.startingTime;

        if (timeDiff > frameTime) {
            this.index++;
            this.startingTime = currentTime;
            timeDiff = 0;
            if (this.index >= this.metadata.frames()) {
                this.index = 0;
            }
            this.nextIndex = this.index + 1;
            if (this.nextIndex >= this.metadata.frames()) {
                this.nextIndex = 0;
            }

            if (!this.metadata.interpolate()) {
                this.bindTexture();
                this.update(this.baseTexture, this.index);
            }
        }

        if (this.metadata.interpolate()) {
            var delta = Math.min(((double) timeDiff) / frameTime, 1);

            for (int x = 0; x < this.width; x++) {
                for (int y = 0; y < this.height; y++) {
                    int base = this.baseTexture.getColor(x, y + this.index * this.height);
                    int next = this.baseTexture.getColor(x, y + this.nextIndex * this.height);

                    int r = this.lerp(delta, next >> 16 & 0xFF, base >> 16 & 0xFF);
                    int g = this.lerp(delta, next >> 8 & 0xFF, base >> 8 & 0xFF);
                    int b = this.lerp(delta, next & 0xFF, base & 0xFF);
                    this.altTexture.setColor(x, y, base & 0xFF000000 | r << 16 | g << 8 | b);
                }
            }

            this.bindTexture();
            this.update(this.altTexture, 0);
        }
    }


    private int lerp(double delta, int to, int from) {
        return (int) (delta * (double) to + (1.0 - delta) * (double) from);
    }


    @Override
    public void tick() {
        if (this.animation) {
            if (!RenderSystem.isOnRenderThread()) {
                RenderSystem.recordRenderCall(this::baseAnimationTick);
            } else {
                this.baseAnimationTick();
            }
        }
    }

    @Override
    public void close() {
        super.close();
        this.baseTexture.close();

        if (this.altTexture != null) {
            this.altTexture.close();
        }
    }
}
