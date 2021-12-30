package eu.pb4.polymer.ext.client.impl.client;

import com.mojang.blaze3d.systems.RenderSystem;
import eu.pb4.polymer.ext.client.impl.CEImplUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class CEToastImpl implements Toast {
    private static final List<CEToastImpl> TEXTURE_TOASTS = new ArrayList<>();
    private static long textureId = 0;

    private final Text text;
    private final long duration;
    private final ItemStack stack;
    private final int textureWidth;
    private final int textureRenderWidth;
    private final int textureHeight;
    private final Identifier textIdentifier;
    private long lastUpdate;

    public CEToastImpl(Text text, long time, @Nullable ItemStack stack, @Nullable byte[] image) {
        this.text = text;
        this.duration = time;

        this.stack = stack;

        NativeImageBackedTexture texture = null;
        try {
            if (image != null) {
                texture = ClientImplUtils.generateTexture(image);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (texture != null) {
            this.textureWidth = texture.getImage().getWidth();
            this.textureHeight =  texture.getImage().getHeight();
            this.textureRenderWidth = (int) ((24f / textureHeight) * textureWidth);
            this.textIdentifier = CEImplUtils.id("toast_texture/" + textureId++);
            MinecraftClient.getInstance().getTextureManager().registerTexture(this.textIdentifier, texture);
            TEXTURE_TOASTS.add(this);
        } else {
            this.textureWidth = -1;
            this.textureHeight = -1;
            this.textureRenderWidth = -1;
            this.textIdentifier = null;
        }
        this.lastUpdate = System.currentTimeMillis();
    }

    public Toast.Visibility draw(MatrixStack matrices, ToastManager manager, long startTime) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        manager.drawTexture(matrices, 0, 0, 0, 0, this.getWidth(), this.getHeight());

        int offset = 8;

        if (this.stack != null) {
            manager.getClient().getItemRenderer().renderInGui(this.stack, 8, 8);
            offset += 20;
        } else if (this.textIdentifier != null) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, this.textIdentifier);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            manager.drawTexture(matrices, 4, 4, this.textureRenderWidth,24, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);
            offset += this.textureRenderWidth + 4;
        }

        List<OrderedText> list = manager.getClient().textRenderer.wrapLines(text, this.getWidth() - 8 - 8);
        if (list.size() == 1) {
            manager.getClient().textRenderer.draw(matrices, list.get(0), offset, this.getHeight() / 2 - manager.getClient().textRenderer.fontHeight / 2, -1);
        } else {
            int l = this.getHeight() / 2 - list.size() * manager.getClient().textRenderer.fontHeight / 2;

            for (OrderedText orderedText : list) {
                manager.getClient().textRenderer.draw(matrices, orderedText, offset, (float) l, -1);
                l += 9;
            }
        }

        this.lastUpdate = System.currentTimeMillis();

        return startTime >= this.duration ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }

    @Override
    public Object getType() {
        return TEXTURE_TOASTS;
    }

    public static final void cleanUp(MinecraftClient client) {
        var now = System.currentTimeMillis();
        for (var toast : TEXTURE_TOASTS.toArray(new CEToastImpl[0])) {
            if (now - toast.lastUpdate > 10000) {
                TEXTURE_TOASTS.remove(toast);
                client.getTextureManager().destroyTexture(toast.textIdentifier);
            }
        }
    }
}
