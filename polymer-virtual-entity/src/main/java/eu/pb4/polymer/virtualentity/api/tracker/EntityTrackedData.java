package eu.pb4.polymer.virtualentity.api.tracker;

import eu.pb4.polymer.virtualentity.mixin.accessors.EntityAccessor;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Pose;

public class EntityTrackedData {
    public static final EntityDataAccessor<Byte> FLAGS = EntityAccessor.getDATA_SHARED_FLAGS_ID();
    public static final EntityDataAccessor<Integer> FROZEN_TICKS = EntityAccessor.getDATA_TICKS_FROZEN();
    public static final EntityDataAccessor<Boolean> NO_GRAVITY = EntityAccessor.getDATA_NO_GRAVITY();
    public static final EntityDataAccessor<Pose> POSE = EntityAccessor.getDATA_POSE();
    public static final EntityDataAccessor<Integer> AIR = EntityAccessor.getDATA_AIR_SUPPLY_ID();
    public static final EntityDataAccessor<Optional<Component>> CUSTOM_NAME = EntityAccessor.getDATA_CUSTOM_NAME();
    public static final EntityDataAccessor<Boolean> NAME_VISIBLE = EntityAccessor.getDATA_CUSTOM_NAME_VISIBLE();
    public static final EntityDataAccessor<Boolean> SILENT = EntityAccessor.getDATA_SILENT();

    public static final int ON_FIRE_FLAG_INDEX = EntityAccessor.getFLAG_ONFIRE();
    public static final int SNEAKING_FLAG_INDEX = EntityAccessor.getFLAG_SHIFT_KEY_DOWN();
    public static final int SPRINTING_FLAG_INDEX = EntityAccessor.getFLAG_SPRINTING();
    public static final int SWIMMING_FLAG_INDEX = EntityAccessor.getFLAG_SWIMMING();
    public static final int INVISIBLE_FLAG_INDEX = EntityAccessor.getFLAG_INVISIBLE();
    public static final int GLOWING_FLAG_INDEX = EntityAccessor.getFLAG_GLOWING();
    public static final int GLIDING_FLAG_INDEX = EntityAccessor.getFLAG_FALL_FLYING();
}
