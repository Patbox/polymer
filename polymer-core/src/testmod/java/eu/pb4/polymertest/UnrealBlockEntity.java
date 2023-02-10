package eu.pb4.polymertest;

import com.jme3.math.Quaternion;
import dev.lazurite.rayon.api.EntityPhysicsElement;
import dev.lazurite.rayon.impl.bullet.collision.body.ElementRigidBody;
import dev.lazurite.rayon.impl.bullet.collision.body.EntityRigidBody;
import dev.lazurite.rayon.impl.bullet.collision.body.shape.MinecraftShape;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymertest.mixin.DisplayEntityAccessor;
import eu.pb4.polymertest.mixin.BlockDisplayEntityAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.Brightness;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.EntityTrackingListener;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Set;

public class UnrealBlockEntity extends Entity implements PolymerEntity/*, EntityPhysicsElement*/ {
    private static final TrackedData<Long> DIRTY_MARKER = DataTracker.registerData(UnrealBlockEntity.class, TrackedDataHandlerRegistry.LONG);

    TrackedData<BlockState> BLOCK_STATE = BlockDisplayEntityAccessor.getBLOCK_STATE();
    TrackedData<Vector3f> TRANSLATION = DisplayEntityAccessor.getTRANSLATION();
    TrackedData<Vector3f> SCALE = DisplayEntityAccessor.getSCALE();
    TrackedData<Quaternionf> ROTATION_LEFT = DisplayEntityAccessor.getLEFT_ROTATION();
    TrackedData<Quaternionf> ROTATION_RIGHT = DisplayEntityAccessor.getRIGHT_ROTATION();
    TrackedData<Long> INTER_START = DisplayEntityAccessor.getINTERPOLATION_START();
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


    /*@Override
    public EntityRigidBody getRigidBody() {
        return this.rigidBody;
    }*/

    public UnrealBlockEntity(EntityType<?> type, World world) {
        super(type, world);
        this.blockState = Registries.BLOCK.getEntryList(BlockTags.WOOL).get().getRandom(this.random).get().value().getDefaultState();;
        this.setPosition(this.getPos());

        //this.rigidBody = new EntityRigidBody(this);
        //this.rigidBody.setMass(15f);
        //this.rigidBody.setBuoyancyType(ElementRigidBody.BuoyancyType.WATER);

        this.scale = new Vector3f(1, 1, 1);
        this.translation = new Vector3f();
        this.rotationLeft = new Quaternionf();
        this.rotationRight = new Quaternionf();
    }

    /*@Override
    public MinecraftShape.Convex createShape() {
        final var box = cast().getBoundingBox();
        return MinecraftShape.convex(box);
    }*/

    @Override
    protected Box calculateBoundingBox() {
        if (this.blockState == null) {
            return super.calculateBoundingBox();
        }

        return this.blockState.getCollisionShape(this.world, BlockPos.ORIGIN, ShapeContext.absent()).getBoundingBox()
                .offset(this.getPos()).offset(-0.5, 0, -0.5);
    }

    @Override
    public Vec3d getSyncedPos() {
        return new Vec3d(this.trackerPos.x, trackerPos.y, trackerPos.z);
    }

    @Override
    public void onEntityTrackerTick(Set<EntityTrackingListener> listeners) {
        this.trackerPos = this.trackerNext;
        //this.trackerNext = Convert.toMinecraft(this.getPhysicsLocation(new com.jme3.math.Vector3f(), 1));
        //rotationLeft = Convert.toMinecraft(this.getPhysicsRotation(new Quaternion(), 1));
        var pos = this.getPos();
        translation = new Vector3f(-0.5f, -0.5f, -0.5f).rotate(rotationLeft)
                .add(trackerNext).add((float) -pos.x, (float) -pos.y, (float) -pos.z);
        this.dataTracker.set(DIRTY_MARKER, this.dataTracker.get(DIRTY_MARKER) + 1);
    }

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
        return EntityType.BLOCK_DISPLAY;
    }

    @Override
    public void modifyRawTrackedData(List<DataTracker.SerializedEntry<?>> data, ServerPlayerEntity player, boolean initial) {
        data.add(DataTracker.SerializedEntry.of(INTER_START, -1l));//player.world.getTime()));
        data.add(DataTracker.SerializedEntry.of(INTER_DUR, 4));
        data.add(DataTracker.SerializedEntry.of(TRANSLATION, this.translation));
        data.add(DataTracker.SerializedEntry.of(SCALE, this.scale));
        data.add(DataTracker.SerializedEntry.of(BLOCK_STATE, this.blockState));
        data.add(DataTracker.SerializedEntry.of(ROTATION_LEFT, this.rotationLeft));
        data.add(DataTracker.SerializedEntry.of(ROTATION_RIGHT, this.rotationRight));
        data.add(DataTracker.SerializedEntry.of(LIGHT, new Brightness(Math.max(player.world.getLightLevel(LightType.BLOCK, this.getBlockPos().up()), this.blockState.getLuminance()), player.world.getLightLevel(LightType.SKY, this.getBlockPos().up())).pack()));
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
