package eu.pb4.polymer.virtualentity.api.tracker;

import eu.pb4.polymer.virtualentity.mixin.accessors.EntityAccessor;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.text.Text;

import java.util.Optional;

public class EntityTrackedData {
    public static final TrackedData<Byte> FLAGS = EntityAccessor.getFLAGS();
    public static final TrackedData<Integer> FROZEN_TICKS = EntityAccessor.getFROZEN_TICKS();
    public static final TrackedData<Boolean> NO_GRAVITY = EntityAccessor.getNO_GRAVITY();
    public static final TrackedData<EntityPose> POSE = EntityAccessor.getPOSE();
    public static final TrackedData<Integer> AIR = EntityAccessor.getAIR();
    public static final TrackedData<Optional<Text>> CUSTOM_NAME = EntityAccessor.getCUSTOM_NAME();
    public static final TrackedData<Boolean> NAME_VISIBLE = EntityAccessor.getNAME_VISIBLE();
    public static final TrackedData<Boolean> SILENT = EntityAccessor.getSILENT();

    public static final int ON_FIRE_FLAG_INDEX = EntityAccessor.getON_FIRE_FLAG_INDEX();
    public static final int SNEAKING_FLAG_INDEX = EntityAccessor.getSNEAKING_FLAG_INDEX();
    public static final int SPRINTING_FLAG_INDEX = EntityAccessor.getSPRINTING_FLAG_INDEX();
    public static final int SWIMMING_FLAG_INDEX = EntityAccessor.getSWIMMING_FLAG_INDEX();
    public static final int INVISIBLE_FLAG_INDEX = EntityAccessor.getINVISIBLE_FLAG_INDEX();
    public static final int GLOWING_FLAG_INDEX = EntityAccessor.getGLOWING_FLAG_INDEX();
    public static final int FALL_FLYING_FLAG_INDEX = EntityAccessor.getFALL_FLYING_FLAG_INDEX();
}
