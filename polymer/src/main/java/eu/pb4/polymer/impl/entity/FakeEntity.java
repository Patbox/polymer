package eu.pb4.polymer.impl.entity;

import eu.pb4.polymer.impl.PolymerImpl;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
class FakeEntity extends Entity {
    public static final Entity INSTANCE;
    private FakeEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    protected void initDataTracker() {

    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {

    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {

    }

    @Override
    public Packet<?> createSpawnPacket() {
        return null;
    }

    static {
        try {
            INSTANCE = new FakeEntity(EntityType.PIG, eu.pb4.polymer.impl.other.FakeWorld.INSTANCE);
        } catch (Exception e1) {
            PolymerImpl.LOGGER.error("Couldn't initiate base template entity! See logs below!");
            throw e1;
        }
    }
}
