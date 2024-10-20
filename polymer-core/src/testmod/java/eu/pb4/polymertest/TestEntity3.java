package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.MobAnchorElement;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import eu.pb4.polymertest.mixin.EntityAccessor;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.item.Items;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4x3fStack;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.function.Consumer;

public class TestEntity3 extends CreeperEntity implements PolymerEntity {
    private final ElementHolder holder;
    private final EntityAttachment attachment;
    private final ItemDisplayElement leftLeg = new ItemDisplayElement(Items.RED_CONCRETE);
    private final ItemDisplayElement rightLeg = new ItemDisplayElement(Items.RED_CONCRETE);
    private final ItemDisplayElement torso = new ItemDisplayElement(PolymerUtils.createPlayerHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjYyYzQ4NWIxODg2ZGJjZTZjMWNhZDE0MGMwZWY4NzYzNTU5ZDQzYTc4NTY0NDY2NGM2ZDVmMzZlMjc1NGVlOCJ9fX0="));
    private final InteractionElement interaction = InteractionElement.redirect(this);
    private final MobAnchorElement rideAnchor = new MobAnchorElement();

    private Matrix4x3fStack stack = new Matrix4x3fStack(8);
    private float previousSpeed = Float.MIN_NORMAL;
    private float previousLimbPos = Float.MIN_NORMAL;
    private float deathAngle;

    public TestEntity3(EntityType<TestEntity3> entityEntityType, World world) {
        super(entityEntityType, world);
        this.holder = new ElementHolder() {
            @Override
            protected void notifyElementsOfPositionUpdate(Vec3d newPos, Vec3d delta) {
                TestEntity3.this.rideAnchor.notifyMove(this.currentPos, newPos, delta);
            }

            @Override
            public Vec3d getPos() {
                return this.getAttachment().getPos();
            }
        };
        this.rideAnchor.setOffset(new Vec3d(0, 1.3f, 0));

        leftLeg.setInterpolationDuration(2);
        leftLeg.ignorePositionUpdates();
        rightLeg.setInterpolationDuration(2);
        rightLeg.ignorePositionUpdates();
        torso.setInterpolationDuration(2);
        torso.ignorePositionUpdates();
        leftLeg.setModelTransformation(ModelTransformationMode.FIXED);
        rightLeg.setModelTransformation(ModelTransformationMode.FIXED);
        torso.setModelTransformation(ModelTransformationMode.FIXED);
        this.interaction.setSize(1.1f, 1.5f);
        this.interaction.ignorePositionUpdates();
        this.rideAnchor.ignorePositionUpdates();
        this.updateAnimation();

        this.holder.addPassengerElement(interaction);
        this.holder.addPassengerElement(leftLeg);
        this.holder.addPassengerElement(rightLeg);
        this.holder.addPassengerElement(torso);
        this.holder.addElement(rideAnchor);
        this.attachment = new EntityAttachment(this.holder, this, false);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getWorld().getTime() % 2 == 1) {
            return;
        }

        this.updateAnimation();

        this.holder.tick();
    }

    private void updateAnimation() {
        var speed = this.limbAnimator.getSpeed();
        var limbPos = this.limbAnimator.getPos();
        float f = ((float)this.deathTime) / 20.0F * 1.6F;
        f = MathHelper.sqrt(f);
        if (f > 1.0F) {
            f = 1.0F;
        }
        if (this.deathAngle == f && speed == this.previousSpeed && limbPos == this.previousLimbPos) {
            return;
        }



        this.deathAngle = f;
        this.previousSpeed = speed;
        this.previousLimbPos = limbPos;

        this.leftLeg.startInterpolation();
        this.rightLeg.startInterpolation();
        this.torso.startInterpolation();

        stack.clear();
        stack.translate(0, -0.2f, 0);
        stack.rotateY((float) Math.toRadians(- MathHelper.lerpAngleDegrees(0.5f, this.prevBodyYaw, this.bodyYaw)) + (float) (0.00001f * Math.random()));
        if (this.deathTime > 0) {
            stack.rotate(RotationAxis.POSITIVE_Z.rotation(f * MathHelper.HALF_PI));
        }
        stack.scale(2);
        stack.pushMatrix();

        stack.translate(0, 0.5f, 0);
        torso.setTransformation(stack);

        stack.popMatrix();

        stack.pushMatrix();
        stack.translate(0.15f, 0.4f, 0).rotateX(MathHelper.cos(limbPos * 0.6662F) * 1.4F * speed).translate(0, -0.125f, 0).scale(0.5f, 0.8f, 0.5f);
        leftLeg.setTransformation(stack);
        stack.popMatrix();

        stack.pushMatrix();
        stack.translate(-0.15f, 0.4f, 0).rotateX(MathHelper.cos(limbPos * 0.6662F + 3.1415927F) * 1.4F * speed).translate(0, -0.125f, 0).scale(0.5f, 0.8f, 0.5f);
        rightLeg.setTransformation(stack);
        stack.popMatrix();
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.ARMOR_STAND;
    }

    @Override
    public void modifyRawTrackedData(List<DataTracker.SerializedEntry<?>> data, ServerPlayerEntity player, boolean initial) {
        data.add(DataTracker.SerializedEntry.of(EntityTrackedData.FLAGS, (byte) (1 << EntityTrackedData.INVISIBLE_FLAG_INDEX)));
        data.add(new DataTracker.SerializedEntry(EntityAccessor.getNO_GRAVITY().id(), EntityAccessor.getNO_GRAVITY().dataType(), true));
        data.add(DataTracker.SerializedEntry.of(ArmorStandEntity.ARMOR_STAND_FLAGS, (byte) (ArmorStandEntity.SMALL_FLAG | ArmorStandEntity.MARKER_FLAG)));
    }
}
