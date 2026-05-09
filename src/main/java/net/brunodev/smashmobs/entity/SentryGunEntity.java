package net.brunodev.smashmobs.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.brunodev.smashmobs.SmashMobs;

import java.util.UUID;

public class SentryGunEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int lifeTicks = 200; // 10 seconds
    private UUID ownerUUID = null;

    public SentryGunEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setNoGravity(false); // Standard gravity so it lands on ground
    }

    public void setOwner(Player player) {
        this.ownerUUID = player.getUUID();
    }

    @Override
    public void aiStep() {
        // Overriding to block automatic movement AI but keeping other standard tick events.
        super.aiStep();
    }

    @Override
    public void tick() {
        super.tick();
        
        if (!this.level().isClientSide()) {
            this.lifeTicks--;
            
            // Prevent external movement drift
            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(0, motion.y, 0); 

            if (this.lifeTicks <= 0) {
                this.discard();
                return;
            }

            // Find target
            var targetCandidate = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(16.0),
                e -> e != this && e.isAlive() && !e.getUUID().equals(ownerUUID)
            ).stream().min(java.util.Comparator.comparingDouble(e -> e.distanceTo(this)));

            if (targetCandidate.isPresent()) {
                LivingEntity target = targetCandidate.get();
                
                // Rotate body toward target smoothly
                this.getLookControl().setLookAt(target, 30.0F, 30.0F);

                // Shoot loop
                if (this.tickCount % 5 == 0) {
                    fireBulletAt(target);
                }
            }
        }
    }

    private void fireBulletAt(LivingEntity target) {
        // Hardcoded height matching the model's barrel/head (~0.7 blocks high)
        double barrelY = this.getY() + 0.7;
        Vec3 barrelPos = new Vec3(this.getX(), barrelY, this.getZ());
        
        Vec3 targetEye = target.getEyePosition();
        Vec3 dir = targetEye.subtract(barrelPos).normalize();

        BulletEntity bullet = new BulletEntity(SmashMobs.BULLET.get(), this.level());
        bullet.setPos(barrelPos.x, barrelPos.y, barrelPos.z);
        bullet.setDeltaMovement(dir.scale(2.5));
        // If owner is still online, set owner
        if (this.ownerUUID != null) {
            Player p = this.level().getPlayerByUUID(this.ownerUUID);
            if (p != null) bullet.setOwner(p);
        }
        this.level().addFreshEntity(bullet);
        this.level().playSound(null, this.blockPosition(), net.minecraft.sounds.SoundEvents.ARROW_SHOOT, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.5F);
    }

    @Override
    public void push(net.minecraft.world.entity.Entity entityIn) {
        // Do not allow other entities to push this sentry gun away
    }

    @Override
    protected void doPush(net.minecraft.world.entity.Entity entityIn) {
        // Do not allow this entity to push other entities
    }

    @Override
    public void push(double x, double y, double z) {
        // Do not absorb knockback velocity
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void travel(Vec3 travelVector) {
        // Stationary movement logic. Only process natural falling/gravity.
        if (this.isEffectiveAi()) {
            this.move(net.minecraft.world.entity.MoverType.SELF, new Vec3(0, this.getDeltaMovement().y, 0));
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
