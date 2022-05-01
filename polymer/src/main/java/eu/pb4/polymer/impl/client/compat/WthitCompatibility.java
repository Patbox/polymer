package eu.pb4.polymer.impl.client.compat;

import eu.pb4.polymer.api.client.PolymerClientUtils;
import eu.pb4.polymer.api.client.registry.ClientPolymerBlock;
import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.impl.client.ClientUtils;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import eu.pb4.polymer.impl.entity.InternalEntityHelpers;
import mcp.mobius.waila.api.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SkullItem;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public class WthitCompatibility implements IWailaPlugin {
    private static final Identifier BLOCK_STATES = Identifier.tryParse("waila:show_states");

    @Override
    public void register(IRegistrar registrar) {
        registrar.addComponent(BlockOverride.INSTANCE, TooltipPosition.HEAD, Block.class, 1000);
        registrar.addComponent(BlockOverride.INSTANCE, TooltipPosition.BODY, Block.class, 1000);
        registrar.addComponent(BlockOverride.INSTANCE, TooltipPosition.TAIL, Block.class, 1000);
        registrar.addOverride(BlockOverride.INSTANCE, Block.class, 1000);
        registrar.addDisplayItem(BlockOverride.INSTANCE, Block.class, 500);

        registrar.addComponent(ItemEntityOverride.INSTANCE, TooltipPosition.HEAD, ItemEntity.class, 1000);
        registrar.addComponent(ItemEntityOverride.INSTANCE, TooltipPosition.TAIL, ItemEntity.class, 1000);

        registrar.addComponent(EntityOverride.INSTANCE, TooltipPosition.HEAD, Entity.class, 1000);
        registrar.addComponent(EntityOverride.INSTANCE, TooltipPosition.TAIL, Entity.class, 1000);

        registrar.addEventListener(OtherOverrides.INSTANCE);
    }

    private static class OtherOverrides implements IEventListener {
        public static final OtherOverrides INSTANCE = new OtherOverrides();

        @Override
        public @Nullable String getHoveredItemModName(ItemStack stack, IPluginConfig config) {
            if (PolymerItemUtils.isPolymerServerItem(stack) || !ClientUtils.isClientThread()) {
                return null;
            }

            var id = PolymerItemUtils.getPolymerIdentifier(stack);

            if (id != null) {
                String modName = null;
                var regBlock = Registry.ITEM.get(id);
                if (regBlock != null) {
                    modName = IModInfo.get(regBlock).getName();
                }

                if (modName == null || modName.isEmpty() || (modName.equals("Minecraft") && !id.getNamespace().equals("minecraft"))) {
                    modName = "Server (" + id.getNamespace() + ")";
                }

                return modName;
            }
            return null;
        }
    }

    private static class BlockOverride implements IBlockComponentProvider {
        public static final BlockOverride INSTANCE = new BlockOverride();

        @Override
        public @Nullable BlockState getOverride(IBlockAccessor accessor, IPluginConfig config) {
            var block = InternalClientRegistry.getBlockAt(accessor.getPosition());
            if (block != ClientPolymerBlock.NONE_STATE) {
                return Blocks.STONE.getDefaultState();
            }
            return null;
        }

        @Override
        public ItemStack getDisplayItem(IBlockAccessor accessor, IPluginConfig config) {
            var block = InternalClientRegistry.getBlockAt(accessor.getPosition());
            if (block != ClientPolymerBlock.NONE_STATE) {
                BlockState state = accessor.getWorld().getBlockState(accessor.getPosition());

                var itemStack = state.getBlock().getPickStack(accessor.getWorld(), accessor.getPosition(), state);

                if (!itemStack.isEmpty() && state.hasBlockEntity() && itemStack.getItem() instanceof SkullItem) {
                    var blockEntity = accessor.getWorld().getBlockEntity(accessor.getPosition());

                    if (blockEntity != null) {
                        var nbtCompound = blockEntity.createNbt();
                        if (nbtCompound.contains("SkullOwner")) {
                            itemStack.getOrCreateNbt().put("SkullOwner", nbtCompound.getCompound("SkullOwner"));
                        }
                    }
                }

                return itemStack;
            }
            return ItemStack.EMPTY;
        }

        @Override
        public void appendHead(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
            var block = InternalClientRegistry.getBlockAt(accessor.getPosition());
            if (block != ClientPolymerBlock.NONE_STATE) {
                IWailaConfig.Formatting formatting = IWailaConfig.get().getFormatting();
                tooltip.set(WailaConstants.OBJECT_NAME_TAG, new LiteralText(formatting.formatBlockName(block.block().name().getString())));
                if (config.getBoolean(WailaConstants.CONFIG_SHOW_REGISTRY)) {
                    tooltip.set(WailaConstants.REGISTRY_NAME_TAG, new LiteralText(formatting.formatRegistryName(block.block().identifier().toString())));
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
                        var valueText = new LiteralText(value).setStyle(Style.EMPTY.withColor(value.equals("true") ? Formatting.GREEN : value.equals("false") ? Formatting.RED : Formatting.RESET));
                        tooltip.addPair(new LiteralText(state.getKey()), valueText);
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
                        modName = "Server";
                    }

                    tooltip.set(WailaConstants.MOD_NAME_TAG, new LiteralText(IWailaConfig.get().getFormatting().formatModName(modName)));
                }
            }
        }
    }

    private static final class ItemEntityOverride implements IEntityComponentProvider {
        public static final ItemEntityOverride INSTANCE = new ItemEntityOverride();

        @Override
        public void appendHead(ITooltip tooltip, IEntityAccessor accessor, IPluginConfig config) {
            if (config.getBoolean(WailaConstants.CONFIG_SHOW_REGISTRY)) {

                var stack = accessor.<ItemEntity>getEntity().getStack();
                if (stack.hasNbt()) {
                    var id = PolymerItemUtils.getPolymerIdentifier(stack);

                    if (id != null) {
                        IWailaConfig.Formatting formatting = IWailaConfig.get().getFormatting();
                        tooltip.set(WailaConstants.REGISTRY_NAME_TAG, new LiteralText(formatting.formatRegistryName(id)));
                    }
                }
            }
        }


        @Override
        public void appendTail(ITooltip tooltip, IEntityAccessor accessor, IPluginConfig config) {
            if (config.getBoolean(WailaConstants.CONFIG_SHOW_MOD_NAME)) {
                var stack = accessor.<ItemEntity>getEntity().getStack();
                if (stack.hasNbt()) {
                    var id = PolymerItemUtils.getPolymerIdentifier(stack);
                    if (id != null) {
                        String modName = null;
                        var regBlock = Registry.ITEM.get(id);
                        if (regBlock != null) {
                            modName = IModInfo.get(regBlock).getName();
                        }

                        if (modName == null || modName.isEmpty() || (modName.equals("Minecraft") && !id.getNamespace().equals("minecraft"))) {
                            modName = "Server";
                        }

                        tooltip.set(WailaConstants.MOD_NAME_TAG, new LiteralText(IWailaConfig.get().getFormatting().formatModName(modName)));
                    }
                }
            }
        }
    }


    private static final class EntityOverride implements IEntityComponentProvider {
        public static final EntityOverride INSTANCE = new EntityOverride();

        @Override
        public void appendTail(ITooltip tooltip, IEntityAccessor accessor, IPluginConfig config) {
            if (config.getBoolean(WailaConstants.CONFIG_SHOW_MOD_NAME)) {
                var type = PolymerClientUtils.getEntityType(accessor.<ItemEntity>getEntity());
                if (type != null) {
                    String modName = null;
                    var regBlock = Registry.ENTITY_TYPE.get(type.identifier());
                    if (regBlock != null) {
                        modName = IModInfo.get(InternalEntityHelpers.getEntity(regBlock)).getName();
                    }

                    if (modName == null || modName.isEmpty() || (modName.equals("Minecraft") && !type.identifier().getNamespace().equals("minecraft"))) {
                        modName = "Server";
                    }

                    tooltip.set(WailaConstants.MOD_NAME_TAG, new LiteralText(IWailaConfig.get().getFormatting().formatModName(modName)));
                }
            }
        }
    }
}
