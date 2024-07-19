package eu.pb4.polymertest;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.core.api.other.PolymerMapCodec;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.math.Vec3d;

public record TestEnchantmentEntityEffect(Text text) implements EnchantmentEntityEffect {
    public static final MapCodec<TestEnchantmentEntityEffect> CODEC = PolymerMapCodec.ofEnchantmentEntityEffect(
            TextCodecs.CODEC.fieldOf("text")
            .xmap(TestEnchantmentEntityEffect::new, TestEnchantmentEntityEffect::text));

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        user.sendMessage(text);
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> getCodec() {
        return CODEC;
    }
}
