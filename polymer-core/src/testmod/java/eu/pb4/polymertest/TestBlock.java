package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public class TestBlock extends Block implements PolymerBlock {

    public TestBlock(Properties settings) {
        super(settings);
    }

    private static final IntegerProperty TEST = IntegerProperty.create("test", 0, 100);

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return super.getStateForPlacement(ctx);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        EnchantmentHelper.pickHighestLevel(itemStack, EnchantmentEffectComponents.REPAIR_WITH_XP).isPresent();
        EnchantmentHelper.getItemEnchantmentLevel(world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MENDING), itemStack);
        super.setPlacedBy(world, pos, state, placer, itemStack);
        itemStack.set(DataComponents.REPAIR_COST, (itemStack.getOrDefault(DataComponents.REPAIR_COST, 0) + 1) % 100);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TEST);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return state.getValue(TEST) % 2 == 0 ? Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.UP) : Blocks.NOTE_BLOCK.defaultBlockState();
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
        float f = state.getDestroySpeed(world, pos);
        if (f == -1.0F) {
            return 0.0F;
        } else {
            int i = player.hasCorrectToolForDrops(state) ? 30 : 50;
            return player.getDestroySpeed(state) / f / (float)i;
        }
    }

    @Override
    public boolean isIgnoringBlockInteractionPlaySoundExceptedEntity(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult) {
        return true;
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return Blocks.SHORT_GRASS.defaultBlockState();
    }
}
