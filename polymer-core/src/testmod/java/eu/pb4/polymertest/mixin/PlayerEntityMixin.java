package eu.pb4.polymertest.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import eu.pb4.polymertest.TestMod;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Leashable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements Leashable {
    private LeashData leashData;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world, LeashData leashData) {
        super(entityType, world);
        this.leashData = leashData;
    }

    @ModifyReturnValue(method = "createPlayerAttributes", at = @At("RETURN"))
    private static DefaultAttributeContainer.Builder addAttribute(DefaultAttributeContainer.Builder original) {
        return original.add(TestMod.ATTRIBUTE);
    }

    @Nullable
    @Override
    public LeashData getLeashData() {
        return this.leashData;
    }

    @Override
    public void setLeashData(@Nullable Leashable.LeashData leashData) {
        this.leashData = leashData;
    }
}
