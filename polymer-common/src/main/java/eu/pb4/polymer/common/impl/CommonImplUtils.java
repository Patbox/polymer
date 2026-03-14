package eu.pb4.polymer.common.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import eu.pb4.polymer.common.impl.client.ClientUtils;
import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
import it.unimi.dsi.fastutil.Hash;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CommonImplUtils {
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
    public static final Component[] ICON;
    public static boolean disableResourcePackCheck;

    static {
        final String chr = "█";
        Component[] iconArray;
        try {
            var source = ImageIO.read(CommonImpl.getJarPath("assets/icon_ingame.png").toUri().toURL());
            var icon = new ArrayList<MutableComponent>();

            for (int y = 0; y < source.getHeight(); y++) {
                var base = Component.empty();
                int line = 0;
                int color = source.getRGB(0, y) & 0xFFFFFF;
                for (int x = 0; x < source.getWidth(); x++) {
                    int colorPixel = source.getRGB(x, y) & 0xFFFFFF;

                    if (color == colorPixel) {
                        line++;
                    } else {
                        base.append(Component.literal(chr.repeat(line)).setStyle(Style.EMPTY.withColor(color).withShadowColor(color | 0xFF000000)));
                        color = colorPixel;
                        line = 1;
                    }
                }

                base.append(Component.literal(chr.repeat(line)).setStyle(Style.EMPTY.withColor(color).withShadowColor(color | 0xFF000000)));
                icon.add(base);
            }

            iconArray = icon.toArray(new Component[0]);
        } catch (Throwable e) {
            e.printStackTrace();
            iconArray = new Component[0];
        }
        ICON = iconArray;
    }

    public static void registerCommands(Consumer<LiteralArgumentBuilder<CommandSourceStack>> consumer) {
        CommonCommands.COMMANDS.add((a, b) -> consumer.accept(a));
    }

    public static void registerCommands(BiConsumer<LiteralArgumentBuilder<CommandSourceStack>, CommandBuildContext> consumer) {
        CommonCommands.COMMANDS.add(consumer);
    }

    public static void registerDevCommands(Consumer<LiteralArgumentBuilder<CommandSourceStack>> consumer) {
        CommonCommands.COMMANDS_DEV.add((a, b) -> consumer.accept(a));
    }

    public static void registerDevCommands(BiConsumer<LiteralArgumentBuilder<CommandSourceStack>, CommandBuildContext> consumer) {
        CommonCommands.COMMANDS_DEV.add(consumer);
    }

    public static Predicate<CommandSourceStack> permission(String path, int operatorLevel) {
        // Todo: fix the check once Fabric permission api is merged
        //if (CompatStatus.FABRIC_PERMISSION_API_V0) {
        //    return Permissions.require("polymer." + path, operatorLevel);
        //} else {
            return source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(operatorLevel)));
        //}
    }

    public static boolean permissionCheck(ServerPlayer player, String path, int operatorLevel) {
        //if (CompatStatus.FABRIC_PERMISSION_API_V0) {
        //    return Permissions.check(player, "polymer." + path, operatorLevel);
        //} else {
            return player.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(operatorLevel)));
        //}
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
        return Identifier.fromNamespaceAndPath("polymer", s);
    }

    public static boolean isMainPlayer(ServerPlayer player) {
        if (CommonImpl.IS_CLIENT) {
            if (ClientUtils.isSingleplayer()) {
                return player == ClientUtils.getPlayer();
            }
        }
        return false;
    }
}
