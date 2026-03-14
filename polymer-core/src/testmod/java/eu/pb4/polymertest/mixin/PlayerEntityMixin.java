package eu.pb4.polymertest.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import eu.pb4.polymertest.TestMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity implements Leashable {
    private LeashData leashData;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, Level world, LeashData leashData) {
        super(entityType, world);
        this.leashData = leashData;
    }

    @ModifyReturnValue(method = "createAttributes", at = @At("RETURN"))
    private static AttributeSupplier.Builder addAttribute(AttributeSupplier.Builder original) {
        return original.add(TestMod.ATTRIBUTE);
    }

    @Nullable
    @Override
    public LeashData getLeashData() {
        return this.leashData;
    }

    @Override
    public void setLeashData(Leashable.@Nullable LeashData leashData) {
        this.leashData = leashData;
    }
}
