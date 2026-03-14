package eu.pb4.polymer.core.impl.client.compat;
/*
import eu.pb4.polymer.common.impl.entity.InternalEntityHelpers;
import eu.pb4.polymer.core.api.client.ClientPolymerBlock;
import eu.pb4.polymer.core.api.client.PolymerClientUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import mcp.mobius.waila.api.*;
import mcp.mobius.waila.api.component.ItemComponent;
import mcp.mobius.waila.api.component.PairComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.Internal
public class WthitCompatibility implements IWailaClientPlugin {
    private static final Identifier BLOCK_STATES = Identifier.tryParse("attribute.block_state");

    @Override
    public void register(IClientRegistrar registrar) {
        registrar.redirect(BlockOverride.INSTANCE, Block.class, 400);
        registrar.head(BlockOverride.INSTANCE, Block.class, 100000);
        registrar.body(BlockOverride.INSTANCE, Block.class, 100000);
        registrar.tail(BlockOverride.INSTANCE, Block.class, 100000);
        registrar.icon(BlockOverride.INSTANCE, Block.class, 500);

        registrar.head(ItemEntityOverride.INSTANCE, ItemEntity.class, 100000);
        registrar.tail(ItemEntityOverride.INSTANCE, ItemEntity.class, 100000);

        registrar.head(EntityOverride.INSTANCE, Entity.class, 100000);
        registrar.tail(EntityOverride.INSTANCE, Entity.class, 100000);

        registrar.eventListener(OtherOverrides.INSTANCE);
    }

    private static class OtherOverrides implements IEventListener {
        public static final OtherOverrides INSTANCE = new OtherOverrides();

        @Override
        public @Nullable String getHoveredItemModName(ItemStack stack, IPluginConfig config) {
            return PolymerImplUtils.getModName(stack);
        }
    }

    private static class BlockOverride implements IBlockComponentProvider {
        public static final BlockOverride INSTANCE = new BlockOverride();

        @Override
        public @Nullable ITargetRedirector.Result redirect(ITargetRedirector redirect, IBlockAccessor accessor, IPluginConfig config) {
            if (InternalClientRegistry.getBlockAt(accessor.getPosition()) != ClientPolymerBlock.NONE_STATE)
                return redirect.toSelf();
            return null;
        }

        @Override
        public @Nullable ITooltipComponent getIcon(IBlockAccessor accessor, IPluginConfig config) {
            var block = InternalClientRegistry.getBlockAt(accessor.getPosition());
            if (block != ClientPolymerBlock.NONE_STATE) {
                BlockState state = accessor.getWorld().getBlockState(accessor.getPosition());

                var itemStack = block.block().displayStack();
                if (itemStack.isEmpty()) {
                    itemStack = state.getCloneItemStack(accessor.getWorld(), accessor.getPosition(), false);
                    if (!itemStack.isEmpty() && state.hasBlockEntity()) {
                        var blockEntity = accessor.getWorld().getBlockEntity(accessor.getPosition());

                        if (blockEntity != null) {
                            itemStack.applyComponents(blockEntity.components());
                        }
                    }
                }

                return new ItemComponent(itemStack);
            }
            return null;
        }

        @Override
        public void appendHead(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
            var block = InternalClientRegistry.getBlockAt(accessor.getPosition());
            if (block != ClientPolymerBlock.NONE_STATE) {
                var formatting = IWailaConfig.get().getFormatter();
                tooltip.setLine(WailaConstants.OBJECT_NAME_TAG, formatting.blockName(block.block().name().getString()));
                if (config.getBoolean(WailaConstants.CONFIG_SHOW_REGISTRY)) {
                    tooltip.setLine(WailaConstants.REGISTRY_NAME_TAG, formatting.registryName(block.block().identifier().toString()));
                }
            }
        }

        @Override
        public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
            if (config.getBoolean(BLOCK_STATES)) {
                var block = InternalClientRegistry.getBlockAt(accessor.getPosition());
                if (block != ClientPolymerBlock.NONE_STATE) {
                    for (var state : block.states().entrySet()) {
                        var value = state.getValue();
                        var valueText = Component.literal(value).setStyle(Style.EMPTY.withColor(value.equals("true") ? ChatFormatting.GREEN : value.equals("false") ? ChatFormatting.RED : ChatFormatting.RESET));
                        tooltip.addLine(new PairComponent(Component.literal(state.getKey()), valueText));
                    }
                }
            }
        }

        @Override
        public void appendTail(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
            if (config.getBoolean(WailaConstants.CONFIG_SHOW_MOD_NAME)) {
                var block = InternalClientRegistry.getBlockAt(accessor.getPosition());
                if (block != ClientPolymerBlock.NONE_STATE) {
                    String modName = IModInfo.get(block.block().identifier()).getName();

                    if (modName == null || modName.isEmpty() || modName.equals("Minecraft")) {
                        modName = PolymerImplUtils.getModName(block.block().identifier());
                    }

                    tooltip.setLine(WailaConstants.MOD_NAME_TAG, IWailaConfig.get().getFormatter().modName(modName));
                }
            }
        }
    }

    private static final class ItemEntityOverride implements IEntityComponentProvider {
        public static final ItemEntityOverride INSTANCE = new ItemEntityOverride();

        @Override
        public void appendHead(ITooltip tooltip, IEntityAccessor accessor, IPluginConfig config) {
            if (config.getBoolean(WailaConstants.CONFIG_SHOW_REGISTRY)) {

                var stack = accessor.<ItemEntity>getEntity().getItem();
                var id = PolymerItemUtils.getServerIdentifier(stack);

                if (id != null) {
                    var formatting = IWailaConfig.get().getFormatter();
                    tooltip.setLine(WailaConstants.REGISTRY_NAME_TAG, formatting.registryName(id));
                }
            }
        }


        @Override
        public void appendTail(ITooltip tooltip, IEntityAccessor accessor, IPluginConfig config) {
            if (config.getBoolean(WailaConstants.CONFIG_SHOW_MOD_NAME)) {
                var stack = accessor.<ItemEntity>getEntity().getItem();
                var id = PolymerItemUtils.getServerIdentifier(stack);
                if (id != null) {
                    String modName = null;
                    var regBlock = BuiltInRegistries.ITEM.getValue(id);
                    if (regBlock != null && regBlock != Items.AIR) {
                        modName = IModInfo.get(regBlock).getName();
                    }

                    if (modName == null || modName.isEmpty() || (modName.equals("Minecraft") && !id.getNamespace().equals("minecraft"))) {
                        modName = PolymerImplUtils.getModName(id);
                    }

                    tooltip.setLine(WailaConstants.MOD_NAME_TAG, IWailaConfig.get().getFormatter().modName(modName));
                }
            }
        }
    }


    private static final class EntityOverride implements IEntityComponentProvider {
        public static final EntityOverride INSTANCE = new EntityOverride();

        @Override
        public @Nullable ITargetRedirector.Result redirect(ITargetRedirector redirect, IEntityAccessor accessor, IPluginConfig config) {
            if (PolymerClientUtils.getEntityType(accessor.getEntity()) != null) return redirect.toSelf();
            return null;
        }

        @Override
        public void appendHead(ITooltip tooltip, IEntityAccessor accessor, IPluginConfig config) {
            if (config.getBoolean(WailaConstants.CONFIG_SHOW_REGISTRY)) {

                var entity = accessor.getEntity();
                var type = PolymerClientUtils.getEntityType(entity);
                if (type != null) {
                    var formatting = IWailaConfig.get().getFormatter();
                    tooltip.setLine(WailaConstants.REGISTRY_NAME_TAG, formatting.registryName(type.identifier()));
                }
            }
        }

        @Override
        public void appendTail(ITooltip tooltip, IEntityAccessor accessor, IPluginConfig config) {
            if (config.getBoolean(WailaConstants.CONFIG_SHOW_MOD_NAME)) {
                var type = PolymerClientUtils.getEntityType(accessor.<ItemEntity>getEntity());
                if (type != null) {
                    String modName = null;
                    var regBlock = BuiltInRegistries.ENTITY_TYPE.getValue(type.identifier());
                    if (regBlock != null) {
                        modName = IModInfo.get(InternalEntityHelpers.getEntity(regBlock)).getName();
                    }

                    if (modName == null || modName.isEmpty() || (modName.equals("Minecraft") && !type.identifier().getNamespace().equals("minecraft"))) {
                        modName = PolymerImplUtils.getModName(type.identifier());
                    }

                    tooltip.setLine(WailaConstants.MOD_NAME_TAG, IWailaConfig.get().getFormatter().modName(modName));
                }
            }
        }
    }
}
*/