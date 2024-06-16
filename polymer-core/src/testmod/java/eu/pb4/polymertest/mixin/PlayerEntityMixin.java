package eu.pb4.polymertest.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import eu.pb4.polymertest.TestMod;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyReturnValue(method = "createPlayerAttributes", at = @At("RETURN"))
    private static DefaultAttributeContainer.Builder addAttribute(DefaultAttributeContainer.Builder original) {
        return original.add(TestMod.ATTRIBUTE);
    }
}
