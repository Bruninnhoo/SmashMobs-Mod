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

    // Rastreia se o projétil atingiu o alvo com sucesso
    private boolean hasHitTarget = false;

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide()) {
            var target = result.getEntity();
            if (target instanceof LivingEntity le) {
                var owner = this.getOwner();
                if (owner instanceof LivingEntity shooter) {
                    // ACERTOU! Marca como sucesso para pular a recarga penalizada
                    this.hasHitTarget = true;
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
        if (!this.level().isClientSide()) {
            // Se bater num bloco, é um ERRO (Miss!)
            if (result.getType() == HitResult.Type.BLOCK && !this.hasHitTarget) {
                triggerOwnerReload();
            }
            this.discard();
        }
    }
    
    @Override
    public void remove(RemovalReason reason) {
        // Se a bala sumiu e nunca acertou ninguém (Expirou por tempo), aciona a recarga do mesmo jeito!
        if (!this.level().isClientSide() && !this.hasHitTarget && reason == RemovalReason.DISCARDED) {
            triggerOwnerReload();
        }
        super.remove(reason);
    }

    private void triggerOwnerReload() {
        // Só deve disparar recarga UMA VEZ por bala
        this.hasHitTarget = true; 
        
        if (this.getOwner() instanceof net.minecraft.server.level.ServerPlayer player) {
            // Verifica o item que o jogador está segurando para garantir animação local
            net.minecraft.world.item.ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof net.brunodev.smashmobs.item.SkeletonSniperItem) {
                net.brunodev.smashmobs.item.SkeletonSniperItem.executeReload(player, stack);
            } else {
                // Se ele mudou de item, ainda aplica o cooldown na classe do item na mochila!
                player.getCooldowns().addCooldown(new net.minecraft.world.item.ItemStack(net.brunodev.smashmobs.SmashMobs.SKELETON_SNIPER.get()), 35);
            }
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
