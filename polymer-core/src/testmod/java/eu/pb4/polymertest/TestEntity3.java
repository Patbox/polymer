package eu.pb4.polymertest;

import com.mojang.math.Axis;
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
import org.joml.Matrix4x3fStack;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TestEntity3 extends Creeper implements PolymerEntity {
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

    public TestEntity3(EntityType<TestEntity3> entityEntityType, Level world) {
        super(entityEntityType, world);
        this.holder = new ElementHolder() {
            @Override
            protected void notifyElementsOfPositionUpdate(Vec3 newPos, Vec3 delta) {
                TestEntity3.this.rideAnchor.notifyMove(this.currentPos, newPos, delta);
            }

            @Override
            public Vec3 getPos() {
                return this.getAttachment().getPos();
            }
        };
        this.rideAnchor.setOffset(new Vec3(0, 1.3f, 0));

        leftLeg.setInterpolationDuration(2);
        leftLeg.ignorePositionUpdates();
        rightLeg.setInterpolationDuration(2);
        rightLeg.ignorePositionUpdates();
        torso.setInterpolationDuration(2);
        torso.ignorePositionUpdates();
        leftLeg.setItemDisplayContext(ItemDisplayContext.FIXED);
        rightLeg.setItemDisplayContext(ItemDisplayContext.FIXED);
        torso.setItemDisplayContext(ItemDisplayContext.FIXED);
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

        if (this.level().getGameTime() % 2 == 1) {
            return;
        }

        this.calculateEntityAnimation(false);
        this.updateAnimation();

        this.holder.tick();
    }

    private void updateAnimation() {
        var speed = this.walkAnimation.speed();
        var limbPos = this.walkAnimation.position();
        float f = ((float)this.deathTime) / 20.0F * 1.6F;
        f = Mth.sqrt(f);
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
        stack.rotateY((float) Math.toRadians(- Mth.rotLerp(0.5f, this.yRotO, this.getYRot())) + (float) (0.00001f * Math.random()));
        if (this.deathTime > 0) {
            stack.rotate(Axis.ZP.rotation(f * Mth.HALF_PI));
        }
        stack.scale(2);
        stack.pushMatrix();

        stack.translate(0, 0.5f, 0);
        torso.setTransformation(stack);

        stack.popMatrix();

        stack.pushMatrix();
        stack.translate(0.15f, 0.4f, 0).rotateX(Mth.cos(limbPos * 0.6662F) * 1.4F * speed).translate(0, -0.125f, 0).scale(0.5f, 0.8f, 0.5f);
        leftLeg.setTransformation(stack);
        stack.popMatrix();

        stack.pushMatrix();
        stack.translate(-0.15f, 0.4f, 0).rotateX(Mth.cos(limbPos * 0.6662F + 3.1415927F) * 1.4F * speed).translate(0, -0.125f, 0).scale(0.5f, 0.8f, 0.5f);
        rightLeg.setTransformation(stack);
        stack.popMatrix();
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.ARMOR_STAND;
    }

    @Override
    public void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {
        data.add(SynchedEntityData.DataValue.create(EntityTrackedData.FLAGS, (byte) (1 << EntityTrackedData.INVISIBLE_FLAG_INDEX)));
        data.add(new SynchedEntityData.DataValue(EntityAccessor.getDATA_NO_GRAVITY().id(), EntityAccessor.getDATA_NO_GRAVITY().serializer(), true));
        data.add(SynchedEntityData.DataValue.create(ArmorStand.DATA_CLIENT_FLAGS, (byte) (ArmorStand.CLIENT_FLAG_SMALL | ArmorStand.CLIENT_FLAG_MARKER)));
    }
}
