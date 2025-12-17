package eu.pb4.polymer.common.impl.entity;

import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.common.impl.FakeWorld;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
class FakeEntity extends Entity {
    public static final Entity INSTANCE;
    private FakeEntity(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

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
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entityTrackerEntry) {
        return super.getAddEntityPacket(entityTrackerEntry);
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
