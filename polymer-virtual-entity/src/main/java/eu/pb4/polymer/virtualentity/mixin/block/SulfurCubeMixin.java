package eu.pb4.polymer.virtualentity.mixin.block;

import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.impl.attachment.BlockAwareEntityAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.cubemob.SulfurCube;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(SulfurCube.class)
public abstract class SulfurCubeMixin extends Entity {
    public SulfurCubeMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Nullable
    @Unique
    private BlockAwareEntityAttachment attachment;


    @Inject(method = "collectEquipmentChanges", at = @At(value = "INVOKE", target = "Ljava/util/List;clear()V"), require = 0)
    private void setupInnerBlock(Map<EquipmentSlot, ItemStack> lastEquipmentItems, CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir,
                                 @Local(name = "current", type = ItemStack.class) ItemStack current) {

        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }

        var state = BlockAwareEntityAttachment.getBlockStateFrom(current);
        var holderCreator = BlockWithElementHolder.get(state);

        if (holderCreator == null) {
            if (this.attachment != null) {
                this.attachment.destroy();
                this.attachment = null;
            }
            return;
        }

        if (this.attachment != null && !this.attachment.getBlockState().is(state.getBlock())) {
            this.attachment.destroy();
            this.attachment = null;
        }

        if (this.attachment == null) {
            var holder = holderCreator.createMovingElementHolder(level, BlockPos.ZERO, state, null);
            if (holder != null) {
                this.attachment = new BlockAwareEntityAttachment(holder, state, this);
            }
        } else {
            this.attachment.setBlockState(state);
        }
    }

}
