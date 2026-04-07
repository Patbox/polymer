package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.item.VanillaModeledPolymerItem;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.UseEffects;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class TestPickaxeItem extends Item implements VanillaModeledPolymerItem {

    public TestPickaxeItem(Item polymerItem, ToolMaterial material, int attackDamage, float attackSpeed, Properties settings) {
        super(settings.tool(material, BlockTags.MINEABLE_WITH_PICKAXE, attackDamage, attackSpeed, 0));
    }

    @Override
    public @NonNull InteractionResult useOn(@NonNull UseOnContext context) {
        InteractionResult axeResult = Items.NETHERITE_AXE.useOn(context);
        if (axeResult.consumesAction()) return axeResult;
        InteractionResult shovelResult = Items.NETHERITE_SHOVEL.useOn(context);
        if (shovelResult.consumesAction()) return shovelResult;
        return super.useOn(context);
    }

    @Override
    public boolean isPolymerBlockInteraction(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult, InteractionResult actionResult) {
        return true;
    }

    @Override
    public boolean isIgnoringBlockInteractionPlaySoundExceptedEntity(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult) {
        return true;
    }

    @Override
    public Item getPolymerItem(ItemStack stack, PacketContext context) {
        return Items.WOODEN_PICKAXE;
    }


    @Override
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        return Identifier.fromNamespaceAndPath("polymertest", "pickaxe");
    }

    @Override
    public void modifyBasePolymerItemStack(ItemStack out, ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        VanillaModeledPolymerItem.super.modifyBasePolymerItemStack(out, stack, context, lookup);
        out.set(DataComponents.CONSUMABLE, new Consumable(999999f, ItemUseAnimation.NONE, Holder.direct(SoundEvents.EMPTY), false, List.of()));
        out.set(DataComponents.USE_EFFECTS, new UseEffects(true, false, 1));
    }

    @Override
    public void modifyClientTooltip(List<Component> tooltip, ItemStack stack, PacketContext context) {
        tooltip.add(0, Component.literal("Hello"));
        tooltip.add(Component.literal("World!"));
    }
}
