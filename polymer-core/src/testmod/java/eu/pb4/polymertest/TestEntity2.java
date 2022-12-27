package eu.pb4.polymertest;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;

public class TestEntity2 extends CreeperEntity implements PolymerEntity {
    public TestEntity2(EntityType<TestEntity2> entityEntityType, World world) {
        super(entityEntityType, world);
    }

    @Override
    public List<Pair<EquipmentSlot, ItemStack>> getPolymerVisibleEquipment(List<Pair<EquipmentSlot, ItemStack> > oldList, ServerPlayerEntity player) {
        List<Pair<EquipmentSlot, ItemStack>> list = Lists.newArrayListWithCapacity(2);
        list.add(Pair.of(EquipmentSlot.MAINHAND, Items.DIAMOND.getDefaultStack()));
        list.add(Pair.of(EquipmentSlot.HEAD, TestMod.TATER_BLOCK_ITEM.getDefaultStack()));
        return list;
    }

    @Override
    public EntityType<?> getPolymerEntityType(ServerPlayerEntity player) {
        return EntityType.PLAYER;
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        return PolymerEntityUtils.createPlayerSpawnPacket(this);
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (player.getStackInHand(hand).isOf(Items.STICK)) {
            this.setPose(EntityPose.SWIMMING);
        } else if (player.getStackInHand(hand).isOf(Items.BLAZE_POWDER)) {
            this.setPose(EntityPose.CROUCHING);
        } else if (player.getStackInHand(hand).isOf(Items.TRIDENT)) {
            this.setPose(EntityPose.SPIN_ATTACK);
        } else if (player.getStackInHand(hand).isOf(Items.BLAZE_ROD)) {
            this.setPose(EntityPose.SITTING);
        }

        return super.interactMob(player, hand);
    }

    @Override
    public void onBeforeSpawnPacket(Consumer<Packet<?>> packetConsumer) {
        var packet = PolymerEntityUtils.createMutablePlayerListPacket(EnumSet.of(PlayerListS2CPacket.Action.ADD_PLAYER, PlayerListS2CPacket.Action.UPDATE_LISTED));
        var gameprofile = new GameProfile(this.getUuid(), "Test NPC");
        gameprofile.getProperties().put("textures", new Property("textures",
                "ewogICJ0aW1lc3RhbXAiIDogMTYxNDk0NDg4ODg4OSwKICAicHJvZmlsZUlkIiA6ICI1N2IzZGZiNWY4YTY0OWUyOGI1NDRlNGZmYzYzMjU2ZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJYaWthcm8iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzdmYzc1ZTBlYzAwNDAyMjMyOTZhYTRkMDhiZDI2YmU0ZDE3MmU4ZGUwNzE4NTU4ODgyMmZhZTM2M2QyMjMxOSIKICAgIH0KICB9Cn0=",
                "R/dm6ic4CYbsr66Iz859K5r1MVd7y08FUvOmJgKTE5KRcPdDNe71Vv61jzh0jQ9QeZJXsHe4+58RY2LiXn7LdPKWpNd+ljK2K4n00Yjp/MM9s6ppNOAQj32LY5UuwcXPUkTSQfr2GROM9zvY93lAuILr6xodvUoIrPcbBDHgxuN6FDiE1jKfFF5z2yZIHOVZXqJPJ+0ri1sw3mjMhbO3dPdpzTW24olgR3wqbXgfEwIeiMk1En+wBtce6ZnNHNXIaMj4fFDAsMmFKqvFcPY8SjfjW/jWBDYNFUCMpxTS2XduQGhSoSlNXG+OrI93Ya/iObGeqAp9WCqFvkV8azyG1VTFfegZCFrUwKV+819B8Q3H3JzJOzES9zvhX5CDKYaE4QvWAqGzTOVw7h0NxtOh9alFkbRR2lWFiBhUMT8EqRjkb+OyBVe9vGRJOU448aLQFyuEWLICje9FAmOHRH0JFpMDEKCLvAAZKZAOx9jceQKrcrcAS0f9nnqjWLLrWMK8lWh0CNcPN1P51rQsxMUlWddNEig+RyjOLHIz/fsv3EQ7yycWkeFfkxq0NAVZGajp4T3NhtWG+WlYywafy5Gtys0Mmv4CXu6xzoUdeLhtMjwgmqfdatQlAJGiZCuSMc1KwWis2inI1YDg5jIy8BTViFBGn76mks21iUEpL4JP8FU="
                ));
        packet.getEntries().add(new PlayerListS2CPacket.Entry(this.getUuid(), gameprofile, false, 0, GameMode.ADVENTURE, null, null));
        packetConsumer.accept(packet);
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        player.networkHandler.sendPacket(new PlayerRemoveS2CPacket(List.of(this.getUuid())));
        super.onStartedTrackingBy(player);
    }
}
