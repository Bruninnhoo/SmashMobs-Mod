package net.brunodev.smashmobs.entity;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AirstrikeJetEntity extends Projectile implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int bombTicks = 0;
    private final int totalBombs = 8;
    private int bombsDropped = 0;

    public AirstrikeJetEntity(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();

        Vec3 move = this.getDeltaMovement();
        this.setPos(this.position().add(move));

        if (!this.level().isClientSide()) {
            this.bombTicks++;
            if (this.bombTicks % 6 == 0 && this.bombsDropped < this.totalBombs) {
                this.dropBomb();
                this.bombsDropped++;
            }

            if (this.tickCount > 100) {
                this.discard();
            }
        }
    }

    private void dropBomb() {
        // Spawns a line of explosions right below the jet, on the ground level.
        // We do this by sending a raycast downwards or just scanning for top block.
        net.minecraft.core.BlockPos top = this.level().getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, this.blockPosition());
        this.level().explode(this.getOwner(), top.getX(), top.getY(), top.getZ(), 3.5F, false, Level.ExplosionInteraction.NONE);
        
        if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER, top.getX(), top.getY(), top.getZ(), 1, 0, 0, 0, 0.0);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
