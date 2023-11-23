package eu.pb4.polymer.common.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import eu.pb4.polymer.common.impl.client.ClientUtils;
import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
import it.unimi.dsi.fastutil.Hash;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CommonImplUtils {
    private static final ThreadLocal<ServerPlayerEntity> playerTargetHack = new ThreadLocal<>();

    public static final Hash.Strategy<Object> IDENTITY_HASH = new Hash.Strategy<Object>() {
        @Override
        public int hashCode(Object o) {
            return System.identityHashCode(o);
        }

        @Override
        public boolean equals(Object a, Object b) {
            return a == b;
        }
    };

    @Nullable
    public static ServerPlayerEntity getPlayer() {
        return playerTargetHack.get();
    }

    public static void setPlayer(ServerPlayerEntity player) {
        playerTargetHack.set(player);
    }

    public static final Text[] ICON;
    public static boolean disableResourcePackCheck;

    static {
        final String chr = "█";
        Text[] iconArray;
        try {
            var source = ImageIO.read(CommonImpl.getJarPath("assets/icon_ingame.png").toUri().toURL());
            var icon = new ArrayList<MutableText>();

            for (int y = 0; y < source.getHeight(); y++) {
                var base = Text.empty();
                int line = 0;
                int color = source.getRGB(0, y) & 0xFFFFFF;
                for (int x = 0; x < source.getWidth(); x++) {
                    int colorPixel = source.getRGB(x, y) & 0xFFFFFF;

                    if (color == colorPixel) {
                        line++;
                    } else {
                        base.append(Text.literal(chr.repeat(line)).setStyle(Style.EMPTY.withColor(color)));
                        color = colorPixel;
                        line = 1;
                    }
                }

                base.append(Text.literal(chr.repeat(line)).setStyle(Style.EMPTY.withColor(color)));
                icon.add(base);
            }

            iconArray = icon.toArray(new Text[0]);
        } catch (Throwable e) {
            e.printStackTrace();
            iconArray = new Text[0];
        }
        ICON = iconArray;
    }

    public static void registerCommands(Consumer<LiteralArgumentBuilder<ServerCommandSource>> consumer) {
        CommonCommands.COMMANDS.add((a, b) -> consumer.accept(a));
    }

    public static void registerCommands(BiConsumer<LiteralArgumentBuilder<ServerCommandSource>, CommandRegistryAccess> consumer) {
        CommonCommands.COMMANDS.add(consumer);
    }

    public static void registerDevCommands(Consumer<LiteralArgumentBuilder<ServerCommandSource>> consumer) {
        CommonCommands.COMMANDS_DEV.add((a, b) -> consumer.accept(a));
    }

    public static void registerDevCommands(BiConsumer<LiteralArgumentBuilder<ServerCommandSource>, CommandRegistryAccess> consumer) {
        CommonCommands.COMMANDS_DEV.add(consumer);
    }

    public static Predicate<ServerCommandSource> permission(String path, int operatorLevel) {
        if (CompatStatus.FABRIC_PERMISSION_API_V0) {
            return Permissions.require("polymer." + path, operatorLevel);
        } else {
            return source -> source.hasPermissionLevel(operatorLevel);
        }
    }

    public static boolean permissionCheck(ServerPlayerEntity player, String path, int operatorLevel) {
        if (CompatStatus.FABRIC_PERMISSION_API_V0) {
            return Permissions.check(player, "polymer." + path, operatorLevel);
        } else {
            return player.hasPermissionLevel(operatorLevel);
        }
    }

    public static <T> T createUnsafe(Class<T> tClass) {
        try {
            return (T) UnsafeAccess.UNSAFE.allocateInstance(tClass);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String shortId(Identifier key) {
        return key.getNamespace().equals(Identifier.DEFAULT_NAMESPACE) ? key.getPath() : key.toString();
    }

    public static Identifier id(String s) {
        return new Identifier("polymer", s);
    }

    public static boolean isMainPlayer(ServerPlayerEntity player) {
        if (CommonImpl.IS_CLIENT) {
            if (ClientUtils.isSingleplayer()) {
                return player == ClientUtils.getPlayer();
            }
        }
        return false;
    }
}
