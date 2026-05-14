package eu.pb4.polymer.common.impl;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.player.Player;

import java.util.function.Predicate;
/**
 * Temporary wrapper for permission checks, targets yet to be merged fabric-permission-api-v1, making the mod support it before it's finalized.
 * Also contains a fallback when it's not present or it changes and fails to adapt.
 */
public class FabricPermissionBridge {
    public static final boolean IS_LOADED = FabricLoader.getInstance().isModLoaded("fabric-permission-api-v1");
    public static final boolean IS_LOADED_LEGACY = FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0");

    public static boolean checkPermission(Player player, Identifier permission, PermissionLevel level) {
        if (IS_LOADED) {
            var res = player.checkPermission(permission);
            if (res != TriState.DEFAULT) {
                return res.get();
            }
        }

        if (IS_LOADED_LEGACY) {
            return Permissions.check(player, permission.toShortLanguageKey().replace('/', '.'), level);
        }

        return player.permissions().hasPermission(new Permission.HasCommandLevel(level));
    }

    public static boolean checkPermission(CommandSourceStack player, Identifier permission, PermissionLevel level) {
        if (IS_LOADED) {
            var res = player.checkPermission(permission);
            if (res != TriState.DEFAULT) {
                return res.get();
            }
        }

        if (IS_LOADED_LEGACY) {
            return Permissions.check(player, permission.toShortLanguageKey().replace('/', '.'), level);
        }

        return player.permissions().hasPermission(new Permission.HasCommandLevel(level));
    }

    public static boolean checkPermission(Player player, Identifier permission, boolean defaultValue) {
        if (IS_LOADED) {
            var res = player.checkPermission(permission);
            if (res != TriState.DEFAULT) {
                return res.get();
            }
        }

        if (IS_LOADED_LEGACY) {
            return Permissions.check(player, permission.toShortLanguageKey().replace('/', '.'), defaultValue);
        }

        return defaultValue;
    }

    public static boolean checkPermission(CommandSourceStack player, Identifier permission, boolean defaultValue) {
        if (IS_LOADED) {
            var res = player.checkPermission(permission);
            if (res != TriState.DEFAULT) {
                return res.get();
            }
        }

        if (IS_LOADED_LEGACY) {
            return Permissions.check(player, permission.toShortLanguageKey().replace('/', '.'), defaultValue);
        }

        return defaultValue;
    }

    public static Predicate<CommandSourceStack> require(Identifier permission, PermissionLevel level) {
        return ctx -> checkPermission(ctx, permission, level);
    }

    public static Predicate<CommandSourceStack> require(Identifier permission, boolean def) {
        return ctx -> checkPermission(ctx, permission, def);
    }
}