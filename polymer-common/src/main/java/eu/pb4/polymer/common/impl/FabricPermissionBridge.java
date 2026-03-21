package eu.pb4.polymer.common.impl;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.player.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.function.Predicate;


/**
 * Temporary wrapper for permission checks, targets yet to be merged fabric-permission-api-v1, making the mod support it before it's finalized.
 * Also contains a fallback when it's not present or it changes and fails to adapt.
 */
public class FabricPermissionBridge {
    public static final boolean IS_LOADED = FabricLoader.getInstance().isModLoaded("fabric-permission-api-v1");

    private static MethodHandle permissionCheckCallPlayer;
    private static MethodHandle permissionCheckCallCommand;
    private static final boolean enabled;

    public static boolean checkPermission(Player player, Identifier permission, PermissionLevel level) {
        if (enabled) {
            try {
                return (boolean) permissionCheckCallPlayer.bindTo(player).invokeExact(permission, level);
            } catch (Throwable e) {
                e.printStackTrace();
                // Should never happen!
            }
        }

        return player.permissions().hasPermission(new Permission.HasCommandLevel(level));
    }

    public static boolean checkPermission(CommandSourceStack player, Identifier permission, PermissionLevel level) {
        if (enabled) {
            try {
                return (boolean) permissionCheckCallCommand.bindTo(player).invokeExact(permission, level);
            } catch (Throwable e) {
                e.printStackTrace();
                // Should never happen!
            }
        }

        return player.permissions().hasPermission(new Permission.HasCommandLevel(level));
    }

    public static Predicate<CommandSourceStack> require(Identifier permission, PermissionLevel level) {
        return ctx -> checkPermission(ctx, permission, level);
    }

    private static MethodHandle findCheckPermission(Class<?> clazz) throws Throwable {
        var lookup = MethodHandles.publicLookup();
        var meth = clazz.getMethod("checkPermission", Identifier.class, PermissionLevel.class);
        return permissionCheckCallCommand = lookup.unreflect(meth);
    }

    static {
        var e = false;
        if (IS_LOADED) {
            try {
                permissionCheckCallPlayer = findCheckPermission(Player.class);
                permissionCheckCallCommand = findCheckPermission(CommandSourceStack.class);
                e = true;
            } catch (Throwable err) {
                err.printStackTrace();
            }
        }
        enabled = e;
    }
}
