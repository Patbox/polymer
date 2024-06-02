package eu.pb4.polymer.common.impl.entity;

import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.common.impl.FakeWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
class FakeEntity extends Entity {
    public static final Entity INSTANCE;
    private FakeEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {}

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {

    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {

    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entityTrackerEntry) {
        return super.createSpawnPacket(entityTrackerEntry);
    }

    static {
        FakeEntity entity;
        try {
            entity = new FakeEntity(EntityType.PIG, FakeWorld.INSTANCE_UNSAFE);
        } catch (Throwable e1) {
            CommonImpl.LOGGER.error("Couldn't initiate base template entity... trying again with a different method.", e1);
            try {
                entity = new FakeEntity(EntityType.PIG, FakeWorld.INSTANCE_REGULAR);
            } catch (Throwable e2) {
                CommonImpl.LOGGER.error("Couldn't initiate base template entity! It's super bad and it might crash soon!", e2);
                entity = null;
            }
        }
        INSTANCE = entity;
    }
}
