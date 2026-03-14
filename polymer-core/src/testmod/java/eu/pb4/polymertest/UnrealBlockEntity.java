package eu.pb4.polymertest;


import com.mojang.math.Transformation;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymertest.mixin.DisplayEntityAccessor;
import eu.pb4.polymertest.mixin.BlockDisplayEntityAccessor;
import eu.pb4.polymertest.mixin.ItemDisplayEntityAccessor;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Brightness;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class UnrealBlockEntity extends Entity implements PolymerEntity/*, EntityPhysicsElement*/ {
    private static final EntityDataAccessor<Long> DIRTY_MARKER = SynchedEntityData.defineId(UnrealBlockEntity.class, EntityDataSerializers.LONG);
    private final boolean tater;

    EntityDataAccessor<ItemStack> ITEM = ItemDisplayEntityAccessor.getDATA_ITEM_STACK_ID();
    EntityDataAccessor<Byte> ITEM_DISPLAY = ItemDisplayEntityAccessor.getDATA_ITEM_DISPLAY_ID();

    EntityDataAccessor<BlockState> BLOCK_STATE = BlockDisplayEntityAccessor.getDATA_BLOCK_STATE_ID();
    EntityDataAccessor<Vector3fc> TRANSLATION = DisplayEntityAccessor.getDATA_TRANSLATION_ID();
    EntityDataAccessor<Vector3fc> SCALE = DisplayEntityAccessor.getDATA_SCALE_ID();
    EntityDataAccessor<Quaternionfc> ROTATION_LEFT = DisplayEntityAccessor.getDATA_LEFT_ROTATION_ID();
    EntityDataAccessor<Quaternionfc> ROTATION_RIGHT = DisplayEntityAccessor.getDATA_RIGHT_ROTATION_ID();
    //TrackedData<Long> INTER_START = DisplayEntityAccessor.getINTERPOLATION_START();
    EntityDataAccessor<Integer> INTER_DUR = DisplayEntityAccessor.getDATA_TRANSFORMATION_INTERPOLATION_DURATION_ID();
    EntityDataAccessor<Integer> LIGHT = DisplayEntityAccessor.getDATA_BRIGHTNESS_OVERRIDE_ID();

    //private final EntityRigidBody rigidBody;
    private BlockState blockState;
    private Vector3fc scale;
    private Vector3fc translation;
    private Quaternionfc rotationLeft;
    private Quaternionfc rotationRight;
    private Vector3f trackerPos = new Vector3f();
    private Vector3f trackerNext = new Vector3f();
    private Vector3f translation0 = new Vector3f();


    /*@Override
    public EntityRigidBody getRigidBody() {
        return this.rigidBody;
    }*/

    public UnrealBlockEntity(EntityType<?> type, Level world) {
        super(type, world);
        this.blockState = BuiltInRegistries.BLOCK.getRandomElementOf(BlockTags.WOOL, this.random).get().value().defaultBlockState();

        if (this.random.nextFloat() > 0.85) {
            this.tater = true;
            this.scale = new Vector3f(2);
        } else {
            this.tater = false;
            this.scale = new Vector3f(1);
        }

        //this.rigidBody = new EntityRigidBody(this, MinecraftSpace.get(world), MinecraftShape.box(this.calculateBoundingBox()));
        //this.rigidBody.setMass(14f);
        //this.rigidBody.setBuoyancyType(ElementRigidBody.BuoyancyType.WATER);

        this.translation = new Vector3f();
        this.rotationLeft = new Quaternionf();
        this.rotationRight = new Quaternionf();
        this.entityData.set(DIRTY_MARKER, this.entityData.get(DIRTY_MARKER) + 1);
    }

    @Override
    public Vec3 trackingPosition() {
        return new Vec3(this.trackerPos.x, trackerPos.y, trackerPos.z);
    }

    @Override
    public void onEntityPacketSent(Consumer<Packet<?>> consumer, Packet<?> packet) {
        if (packet.getClass() == ClientboundSetEntityMotionPacket.class || packet.getClass() == ClientboundCustomPayloadPacket.class) {
            return;
        }

        consumer.accept(packet);
    }
    /*
    @Override
    public void beforeEntityTrackerTick(Set<PlayerAssociatedNetworkHandler> listeners) {
        this.trackerPos = this.trackerNext;
        this.trackerNext = Convert.toMinecraft(this.getPhysicsLocation(new com.jme3.math.Vector3f(), 1));
        rotationLeft = Convert.toMinecraft(this.getPhysicsRotation(new Quaternion(), 1));
        translation0 = this.tater ? new Vector3f(0) : new Vector3f( -0.5f).rotate(rotationLeft);
        translation = this.tater ? new Vector3f(0) : new Vector3f( -0.5f).rotate(rotationLeft)
                .add(trackerNext).add(-this.trackerPos.x, -this.trackerPos.y, -this.trackerPos.z);
        listeners.forEach(x -> x.sendPacket(new EntityTrackerUpdateS2CPacket(this.getId(), List.of())));
        this.dataTracker.set(DIRTY_MARKER, this.dataTracker.get(DIRTY_MARKER) + 1);
    }
    */
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DIRTY_MARKER, 0l);
    }

    @Override
    public void playerTouch(Player player) {
        super.playerTouch(player);
    }

    @Override
    public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput view) {

    }

    @Override
    protected void addAdditionalSaveData(ValueOutput view) {

    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return this.tater ? EntityType.ITEM_DISPLAY : EntityType.BLOCK_DISPLAY;
    }

    @Override
    public void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {
        if (data.isEmpty()) {
            data.add(SynchedEntityData.DataValue.create(INTER_DUR, -1));
            data.add(SynchedEntityData.DataValue.create(TRANSLATION, this.translation0));
            return;
        }

        if (initial) {
            data.add(SynchedEntityData.DataValue.create(SCALE, this.scale));
            if (this.tater) {
                data.add(SynchedEntityData.DataValue.create(ITEM, TestMod.TATER_BLOCK_ITEM.getDefaultInstance()));
                data.add(SynchedEntityData.DataValue.create(ITEM_DISPLAY, ItemDisplayContext.FIXED.getId()));
            } else {
                data.add(SynchedEntityData.DataValue.create(BLOCK_STATE, this.blockState));
            }
            data.add(SynchedEntityData.DataValue.create(ROTATION_RIGHT, this.rotationRight));
        }

        //data.add(DataTracker.SerializedEntry.of(INTER_START, player.world.getTime()));
        data.add(SynchedEntityData.DataValue.create(INTER_DUR, 1));
        data.add(SynchedEntityData.DataValue.create(TRANSLATION, this.translation));
        data.add(SynchedEntityData.DataValue.create(ROTATION_LEFT, this.rotationLeft));
        data.add(SynchedEntityData.DataValue.create(LIGHT, new Brightness(Math.max(player.level().getBrightness(LightLayer.BLOCK, this.blockPosition().above()), this.blockState.getLightEmission()), player.level().getBrightness(LightLayer.SKY, this.blockPosition().above())).pack()));
    }

    public void applyAffineTransformation(Transformation affineTransformation) {
        translation = affineTransformation.translation();
        rotationRight =  affineTransformation.leftRotation();
        scale = affineTransformation.scale();
        rotationLeft = affineTransformation.rightRotation();
        this.entityData.set(DIRTY_MARKER, this.entityData.get(DIRTY_MARKER) + 1);
    }
}
