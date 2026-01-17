package eu.pb4.polymertest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.core.api.other.PolymerMapCodec;
import eu.pb4.polymer.resourcepack.api.PackResource;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import eu.pb4.polymer.resourcepack.extras.api.format.font.BitmapProvider;
import eu.pb4.polymer.resourcepack.extras.api.format.font.FontAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.font.SpaceProvider;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.dialog.body.DialogBody;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferUShort;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;

public record TestDialogImageBody(String image, boolean dither) implements DialogBody {
    private static Identifier[] FONT_PIXEL = Util.make(new Identifier[9], x -> {
        for (int i = 0; i < 9; i++) {
            x[i] = Identifier.fromNamespaceAndPath("test", "pixel_" + i);
        }
    });

    private static Style[] STYLE_PIXEL = Util.make(new Style[9], x -> {
        for (int i = 0; i < 9; i++) {
            x[i] = Style.EMPTY.withoutShadow().withFont(new FontDescription.Resource(FONT_PIXEL[i]));
        }
    });

    public static final MapCodec<TestDialogImageBody> CODEC = PolymerMapCodec.ofDialogBody(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.STRING.fieldOf("path").forGetter(TestDialogImageBody::image),
                    Codec.BOOL.optionalFieldOf("dither", false).forGetter(TestDialogImageBody::dither)
            ).apply(instance, TestDialogImageBody::new)),
            (data, context) -> {
        try {
            var image = ImageIO.read(Files.newInputStream(FabricLoader.getInstance().getGameDir().resolve(data.image)));
            var component = Component.empty();

            int line = 0;
            for (int y = 0; y < image.getHeight(); y++) {
                var b = new StringBuffer();
                for (int x = 0; x < image.getWidth(); x++) {
                    var val = data.dither ? sample(image, x, y) : to15BitColor(image.getRGB(x, y));
                    b.append((char) val);
                    b.append((char) 0xFF01);
                }

                var isLast = y == image.getHeight() - 1;

                if (line != 8 && !isLast) {
                    var w = image.getWidth();
                    while (w > 255) {
                        b.append((char) 0xFFFF);
                        w -= 255;
                    }
                    b.append((char) (0xFF00 + w));
                }

                var str = b.toString();
                component.append(Component.literal(str).setStyle(STYLE_PIXEL[line]));

                if (++line == 9 || isLast) {
                    line = 0;
                    component.append("\n");
                }
            }

            return new PlainMessage(component, image.getWidth() + 20);
        } catch (Throwable e) {
            return new PlainMessage(Component.literal("Error!"), 100);
        }
    });

    @Override
    public MapCodec<? extends DialogBody> mapCodec() {
        return CODEC;
    }


    public static void generateResources(ResourcePackBuilder builder) {
        var texture = new BufferedImage(256, 128, BufferedImage.TYPE_USHORT_555_RGB);

        var chars = new ArrayList<String>(128);
        {
            for (var y = 0; y < 128; y++) {
                var b = new StringBuffer();
                for (var x = 0; x < 256; x++) {
                    b.append((char) (y * 256 + x + 0x100));
                }
                chars.add(b.toString());
            }
        }
        SpaceProvider spaces;
        {
            var b = SpaceProvider.builder();
            for (var i = 0; i < 256; i++) {
                b.add((char) 0xFF00 | i, -i);
            }
            spaces = b.build();
        }

        var id = Identifier.fromNamespaceAndPath("test", "colormap.png");
        for (int i = 0; i < 9; i++) {
            var b = FontAsset.builder();
            b.add(spaces);
            b.add(new BitmapProvider(id, chars, -i, 1));
            builder.addData("assets/test/font/pixel_" + i + ".json" , PackResource.fromAsset(b.build()));
        }


        for (int color = 0; color < 32 * 32 * 32; color++) {
            texture.setRGB(color % 256, color / 256, from15BitColor(color + 0x100));
        }
        builder.addData("assets/test/textures/colormap.png", PackResource.fromImage(texture));
    }

    static int to15BitColor(int rgb) {
        return (((ARGB.red(rgb) >>> 3) << 10) | ((ARGB.green(rgb) >>> 3) << 5) | ((ARGB.blue(rgb) >> 3))) + 0x100;
    }

    static int from15BitColor(int rgb) {
        rgb -= 0x100;
        return (((rgb >> 10) & 0b11111) << (16 + 3)) | (((rgb >> 5) & 0b11111) << (8 + 3)) | ((rgb & 0b11111) << 3);
    }

    public static int sample(BufferedImage scratchImg, int x, int y) {
        var imageColor = scratchImg.getRGB(x, y);
        var closestColor = to15BitColor(imageColor);
        var palletedColor = from15BitColor(closestColor);

        var errorR = ARGB.red(imageColor) - ARGB.red(palletedColor);
        var errorG = ARGB.green(imageColor) - ARGB.green(palletedColor);
        var errorB = ARGB.blue(imageColor) - ARGB.blue(palletedColor);
        if (scratchImg.getWidth() > x + 1) {
            scratchImg.setRGB(x + 1, y, applyError(scratchImg.getRGB(x + 1, y), errorR, errorG, errorB, 7.0 / 16.0));
        }
        if (scratchImg.getHeight() > y + 1) {
            if (x > 0) {
                scratchImg.setRGB(x - 1, y + 1, applyError(scratchImg.getRGB(x - 1, y + 1), errorR, errorG, errorB, 3.0 / 16.0));
            }
            scratchImg.setRGB(x, y + 1, applyError(scratchImg.getRGB(x, y + 1), errorR, errorG, errorB, 5.0 / 16.0));
            if (scratchImg.getWidth() > x + 1) {
                scratchImg.setRGB(x + 1, y + 1, applyError(scratchImg.getRGB(x + 1, y + 1), errorR, errorG, errorB, 1.0 / 16.0));
            }
        }

        return closestColor;
    }

    private static int applyError(int pixelColor, int errorR, int errorG, int errorB, double quantConst) {
        int pR = Mth.clamp( ARGB.red(pixelColor) + (int) ((double) errorR * quantConst), 0, 255);
        int pG = Mth.clamp(ARGB.green(pixelColor) + (int) ((double) errorG * quantConst), 0, 255);
        int pB = Mth.clamp(ARGB.blue(pixelColor) + (int) ((double) errorB * quantConst), 0, 255);
        return ARGB.color(ARGB.alpha(pixelColor), pR, pG, pB);
    }
}
