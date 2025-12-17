package eu.pb4.polymertest;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.core.api.other.PolymerMapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

public record TestEnchantmentEntityEffect(Component text) implements EnchantmentEntityEffect {
    public static final MapCodec<TestEnchantmentEntityEffect> CODEC = PolymerMapCodec.ofEnchantmentEntityEffect(
            ComponentSerialization.CODEC.fieldOf("text")
            .xmap(TestEnchantmentEntityEffect::new, TestEnchantmentEntityEffect::text));

    @Override
    public void apply(ServerLevel world, int level, EnchantedItemInUse context, Entity user, Vec3 pos) {
        if (user instanceof ServerPlayer player) {
            player.sendSystemMessage(text);
        }
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> codec() {
        return CODEC;
    }
}
