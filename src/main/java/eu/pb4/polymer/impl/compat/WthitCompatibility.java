package eu.pb4.polymer.impl.compat;

import eu.pb4.polymer.impl.client.InternalClientRegistry;
import mcp.mobius.waila.api.*;
import net.minecraft.block.Block;
import net.minecraft.text.LiteralText;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class WthitCompatibility implements IWailaPlugin {
    @Override
    public void register(IRegistrar registrar) {
        registrar.addComponent(BlockOverride.INSTANCE, TooltipPosition.HEAD, Block.class, 1000);
        registrar.addComponent(BlockOverride.INSTANCE, TooltipPosition.TAIL, Block.class, 1000);
    }

    public static class BlockOverride implements IBlockComponentProvider {
        public static final BlockOverride INSTANCE = new BlockOverride();

        @Override
        public void appendHead(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
            var block = InternalClientRegistry.getBlockAt(accessor.getPosition());
            if (block != null) {
                IWailaConfig.Formatting formatting = IWailaConfig.get().getFormatting();
                tooltip.set(WailaConstants.OBJECT_NAME_TAG, new LiteralText(formatting.formatBlockName(block.block().name().getString())));
                if (config.getBoolean(WailaConstants.CONFIG_SHOW_REGISTRY)) {
                    tooltip.set(WailaConstants.REGISTRY_NAME_TAG, new LiteralText(formatting.formatRegistryName(block.block().identifier().toString())));
                }
            }
        }

        @Override
        public void appendTail(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
            if (config.getBoolean(WailaConstants.CONFIG_SHOW_MOD_NAME)) {
                var block = InternalClientRegistry.getBlockAt(accessor.getPosition());
                if (block != null) {
                    String modName = null;
                    var regBlock = Registry.BLOCK.get(block.block().identifier());
                    if (regBlock != null) {
                        modName = IModInfo.get(regBlock).getName();
                    }

                    if (modName == null || modName.equals("Minecraft")) {
                        modName = "Server";
                    }

                    tooltip.set(WailaConstants.MOD_NAME_TAG, new LiteralText(IWailaConfig.get().getFormatting().formatModName(modName)));
                }
            }
        }
    }
}
