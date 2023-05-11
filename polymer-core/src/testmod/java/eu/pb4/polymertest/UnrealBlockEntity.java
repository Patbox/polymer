package eu.pb4.polymertest;


import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymertest.mixin.DisplayEntityAccessor;
import eu.pb4.polymertest.mixin.BlockDisplayEntityAccessor;
import eu.pb4.polymertest.mixin.ItemDisplayEntityAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.Brightness;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.EntityTrackingListener;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class UnrealBlockEntity extends Entity implements PolymerEntity/*, EntityPhysicsElement*/ {
    private static final TrackedData<Long> DIRTY_MARKER = DataTracker.registerData(UnrealBlockEntity.class, TrackedDataHandlerRegistry.LONG);
    private final boolean tater;

    TrackedData<ItemStack> ITEM = ItemDisplayEntityAccessor.getITEM();
    TrackedData<Byte> ITEM_DISPLAY = ItemDisplayEntityAccessor.getITEM_DISPLAY();

    TrackedData<BlockState> BLOCK_STATE = BlockDisplayEntityAccessor.getBLOCK_STATE();
    TrackedData<Vector3f> TRANSLATION = DisplayEntityAccessor.getTRANSLATION();
    TrackedData<Vector3f> SCALE = DisplayEntityAccessor.getSCALE();
    TrackedData<Quaternionf> ROTATION_LEFT = DisplayEntityAccessor.getLEFT_ROTATION();
    TrackedData<Quaternionf> ROTATION_RIGHT = DisplayEntityAccessor.getRIGHT_ROTATION();
    //TrackedData<Long> INTER_START = DisplayEntityAccessor.getINTERPOLATION_START();
    TrackedData<Integer> INTER_DUR = DisplayEntityAccessor.getINTERPOLATION_DURATION();
    TrackedData<Integer> LIGHT = DisplayEntityAccessor.getBRIGHTNESS();

    //private final EntityRigidBody rigidBody;
    private BlockState blockState;
    private Vector3f scale;
    private Vector3f translation;
    private Quaternionf rotationLeft;
    private Quaternionf rotationRight;
    private Vector3f trackerPos = new Vector3f();
    private Vector3f trackerNext = new Vector3f();
    private Vector3f translation0 = new Vector3f();


    /*@Override
    public EntityRigidBody getRigidBody() {
        return this.rigidBody;
    }*/

    public UnrealBlockEntity(EntityType<?> type, World world) {
        super(type, world);
        this.blockState = Registries.BLOCK.getEntryList(BlockTags.WOOL).get().getRandom(this.random).get().value().getDefaultState();

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
        this.dataTracker.set(DIRTY_MARKER, this.dataTracker.get(DIRTY_MARKER) + 1);
    }

    @Override
    public Vec3d getSyncedPos() {
        return new Vec3d(this.trackerPos.x, trackerPos.y, trackerPos.z);
    }

    @Override
    public void onEntityPacketSent(Consumer<Packet<?>> consumer, Packet<?> packet) {
        if (packet.getClass() == EntityVelocityUpdateS2CPacket.class || packet.getClass() == CustomPayloadS2CPacket.class) {
            return;
        }

        consumer.accept(packet);
    }
    /*
    @Override
    public void beforeEntityTrackerTick(Set<EntityTrackingListener> listeners) {
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
    protected void initDataTracker() {
        this.dataTracker.startTracking(DIRTY_MARKER, 0l);
    }

    @Override
    public void onPlayerCollision(PlayerEntity player) {
        super.onPlayerCollision(player);
    }

    @Override
    public EntityType<?> getPolymerEntityType(ServerPlayerEntity player) {
        return this.tater ? EntityType.ITEM_DISPLAY : EntityType.BLOCK_DISPLAY;
    }

    @Override
    public void modifyRawTrackedData(List<DataTracker.SerializedEntry<?>> data, ServerPlayerEntity player, boolean initial) {
        if (data.isEmpty()) {
            data.add(DataTracker.SerializedEntry.of(INTER_DUR, -1));
            data.add(DataTracker.SerializedEntry.of(TRANSLATION, this.translation0));
            return;
        }

        if (initial) {
            data.add(DataTracker.SerializedEntry.of(SCALE, this.scale));
            if (this.tater) {
                data.add(DataTracker.SerializedEntry.of(ITEM, TestMod.TATER_BLOCK_ITEM.getDefaultStack()));
                data.add(DataTracker.SerializedEntry.of(ITEM_DISPLAY, ModelTransformationMode.FIXED.getIndex()));
            } else {
                data.add(DataTracker.SerializedEntry.of(BLOCK_STATE, this.blockState));
            }
            data.add(DataTracker.SerializedEntry.of(ROTATION_RIGHT, this.rotationRight));
        }

        //data.add(DataTracker.SerializedEntry.of(INTER_START, player.world.getTime()));
        data.add(DataTracker.SerializedEntry.of(INTER_DUR, 1));
        data.add(DataTracker.SerializedEntry.of(TRANSLATION, this.translation));
        data.add(DataTracker.SerializedEntry.of(ROTATION_LEFT, this.rotationLeft));
        data.add(DataTracker.SerializedEntry.of(LIGHT, new Brightness(Math.max(player.getWorld().getLightLevel(LightType.BLOCK, this.getBlockPos().up()), this.blockState.getLuminance()), player.getServerWorld().getLightLevel(LightType.SKY, this.getBlockPos().up())).pack()));
    }

    public void applyAffineTransformation(AffineTransformation affineTransformation) {
        translation = affineTransformation.getTranslation();
        rotationRight =  affineTransformation.getLeftRotation();
        scale = affineTransformation.getScale();
        rotationLeft = affineTransformation.getRightRotation();
        this.dataTracker.set(DIRTY_MARKER, this.dataTracker.get(DIRTY_MARKER) + 1);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {

    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {

    }
}
