package eu.pb4.polymertest;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Pair;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class TestEntity2 extends Creeper implements PolymerEntity {
    public TestEntity2(EntityType<TestEntity2> entityEntityType, Level world) {
        super(entityEntityType, world);
    }

    @Override
    public List<Pair<EquipmentSlot, ItemStack>> getPolymerVisibleEquipment(List<Pair<EquipmentSlot, ItemStack> > oldList, ServerPlayer player) {
        List<Pair<EquipmentSlot, ItemStack>> list = Lists.newArrayListWithCapacity(2);
        list.add(Pair.of(EquipmentSlot.MAINHAND, Items.DIAMOND.getDefaultInstance()));
        list.add(Pair.of(EquipmentSlot.HEAD, TestMod.TATER_BLOCK_ITEM.getDefaultInstance()));
        return list;
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.PLAYER;
    }

    @Override
    public float getBlockExplosionResistance(Explosion explosion, BlockGetter world, BlockPos pos, BlockState blockState, FluidState fluidState, float max) {
        return blockState.isAir() || blockState.getBlock() instanceof LiquidBlock ? 0 : blockState.getBlock().getExplosionResistance();
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.getItemInHand(hand).is(Items.STICK)) {
            this.setPose(Pose.SWIMMING);
        } else if (player.getItemInHand(hand).is(Items.BLAZE_POWDER)) {
            this.setPose(Pose.CROUCHING);
        } else if (player.getItemInHand(hand).is(Items.TRIDENT)) {
            this.setPose(Pose.SPIN_ATTACK);
        } else if (player.getItemInHand(hand).is(Items.BLAZE_ROD)) {
            this.setPose(Pose.SITTING);
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public void onBeforeSpawnPacket(ServerPlayer player, Consumer<Packet<?>> packetConsumer) {
        var packet = PolymerEntityUtils.createMutablePlayerInfoUpdatePacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED));

        var properties = new PropertyMap(ImmutableMultimap.of("textures", new Property("textures",
                "ewogICJ0aW1lc3RhbXAiIDogMTYxNDk0NDg4ODg4OSwKICAicHJvZmlsZUlkIiA6ICI1N2IzZGZiNWY4YTY0OWUyOGI1NDRlNGZmYzYzMjU2ZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJYaWthcm8iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzdmYzc1ZTBlYzAwNDAyMjMyOTZhYTRkMDhiZDI2YmU0ZDE3MmU4ZGUwNzE4NTU4ODgyMmZhZTM2M2QyMjMxOSIKICAgIH0KICB9Cn0=",
                "R/dm6ic4CYbsr66Iz859K5r1MVd7y08FUvOmJgKTE5KRcPdDNe71Vv61jzh0jQ9QeZJXsHe4+58RY2LiXn7LdPKWpNd+ljK2K4n00Yjp/MM9s6ppNOAQj32LY5UuwcXPUkTSQfr2GROM9zvY93lAuILr6xodvUoIrPcbBDHgxuN6FDiE1jKfFF5z2yZIHOVZXqJPJ+0ri1sw3mjMhbO3dPdpzTW24olgR3wqbXgfEwIeiMk1En+wBtce6ZnNHNXIaMj4fFDAsMmFKqvFcPY8SjfjW/jWBDYNFUCMpxTS2XduQGhSoSlNXG+OrI93Ya/iObGeqAp9WCqFvkV8azyG1VTFfegZCFrUwKV+819B8Q3H3JzJOzES9zvhX5CDKYaE4QvWAqGzTOVw7h0NxtOh9alFkbRR2lWFiBhUMT8EqRjkb+OyBVe9vGRJOU448aLQFyuEWLICje9FAmOHRH0JFpMDEKCLvAAZKZAOx9jceQKrcrcAS0f9nnqjWLLrWMK8lWh0CNcPN1P51rQsxMUlWddNEig+RyjOLHIz/fsv3EQ7yycWkeFfkxq0NAVZGajp4T3NhtWG+WlYywafy5Gtys0Mmv4CXu6xzoUdeLhtMjwgmqfdatQlAJGiZCuSMc1KwWis2inI1YDg5jIy8BTViFBGn76mks21iUEpL4JP8FU="
        )));

        var gameprofile = new GameProfile(this.getUUID(), "Test NPC");
        packet.entries().add(new ClientboundPlayerInfoUpdatePacket.Entry(this.getUUID(), gameprofile, false, 0, GameType.ADVENTURE, null,  true,0, null));
        packetConsumer.accept(packet);
    }


    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        player.connection.send(new ClientboundPlayerInfoRemovePacket(List.of(this.getUUID())));
        super.startSeenByPlayer(player);
    }
}
