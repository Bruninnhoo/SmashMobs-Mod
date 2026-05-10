package net.brunodev.smashmobs.entity;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.EntityHitResult;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.world.entity.projectile.ProjectileUtil;

public class BulletEntity extends Projectile implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int lifeTime = 0;
    private double damage = 10.0;

    public BulletEntity(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
    }

    public void setDamage(double dmg) {
        this.damage = dmg;
    }

    @Override
    public void tick() {
        super.tick();
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        this.xOld = this.getX();
        this.yOld = this.getY();
        this.zOld = this.getZ();

        this.lifeTime++;
        if (this.lifeTime > 100) {
            this.discard();
            return;
        }

        Vec3 move = this.getDeltaMovement();
        Vec3 nextPos = this.position().add(move);

        // Simple manual collision raycast for entities along current move vector
        var hitResult = ProjectileUtil.getHitResultOnMoveVector(this, e -> !e.isSpectator() && e.isAlive() && e != this.getOwner());
        if (hitResult.getType() != HitResult.Type.MISS) {
            this.onHit(hitResult);
        }

        this.setPos(nextPos);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide()) {
            var target = result.getEntity();
            if (target instanceof LivingEntity le) {
                var owner = this.getOwner();
                if (owner instanceof LivingEntity shooter) {
                    le.hurt(this.damageSources().mobAttack(shooter), (float) damage);
                } else {
                    le.hurt(this.damageSources().generic(), (float) damage);
                }
            }
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (result.getType() == HitResult.Type.BLOCK && !this.level().isClientSide()) {
            this.discard();
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
