package eu.pb4.polymertestpce;

import com.mojang.brigadier.CommandDispatcher;
import eu.pb4.polymer.api.networking.PolymerSyncUtils;
import eu.pb4.polymer.ext.client.api.PolymerClientExtensions;
import eu.pb4.polymer.impl.PolymerImpl;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

import static net.minecraft.server.command.CommandManager.literal;

public class TestMod implements ModInitializer, ClientModInitializer {

    @Override
    public void onInitialize() {
        var path = PolymerImpl.getGameDir().resolve("test_background.png");
        byte[] image = null;

        if (path.toFile().exists()) {
            try {
                {
                    var bufferedImage = ImageIO.read(Files.newInputStream(path));

                    var byteArrayOutputStream = new ByteArrayOutputStream();
                    ImageIO.write(bufferedImage, "PNG", byteArrayOutputStream);
                    image = byteArrayOutputStream.toByteArray();
                }

                byte[] finalImage = image;
                PolymerSyncUtils.ON_SYNC_CUSTOM.register((handler, bool) -> {
                    PolymerClientExtensions.setReloadScreen(handler, 0x225522, 0xFFFFFF, PolymerClientExtensions.ReloadLogoOverride.FULL_SCREEN, finalImage);
                });
            } catch (Exception e) {
                // noop
            }
        }
        byte[] finalImage = image;

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(literal("toastbase1").executes((ctx) -> {
                PolymerClientExtensions.createToast(ctx.getSource().getPlayer().networkHandler, new LiteralText("Hello world!\nTater"), 20000);
                return 0;
            }));

            dispatcher.register(literal("toastbase2").executes((ctx) -> {
                PolymerClientExtensions.createToast(ctx.getSource().getPlayer().networkHandler, new LiteralText("Colored").setStyle(Style.EMPTY.withColor(0x99FF99).withItalic(true)), 5000);
                return 0;
            }));

            dispatcher.register(literal("toastitem").executes((ctx) -> {
                var player = ctx.getSource().getPlayer();
                PolymerClientExtensions.createToast(player.networkHandler, player.getMainHandStack().getName(), player.getMainHandStack(), 20000);
                return 0;
            }));

            if (finalImage != null) {
                dispatcher.register(literal("toasttexture").executes((ctx) -> {
                    var player = ctx.getSource().getPlayer();
                    PolymerClientExtensions.createToast(player.networkHandler, new LiteralText("texture test"), finalImage, 20000);
                    return 0;
                }));
            }
        });
    }

    @Override
    public void onInitializeClient() {

    }
}
