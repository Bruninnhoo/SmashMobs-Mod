package net.brunodev.smashmobs.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;
import java.util.List;

public class GolemTrainEntity extends Projectile implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int lifeTime = 0;
    private final List<LivingEntity> draggedVictims = new ArrayList<>();

    public GolemTrainEntity(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();

        // ==========================================
        // 1. O SEGREDO DA SUAVIDADE (INTERPOLAÇÃO)
        // Salva a posição antiga para o Minecraft gerar os frames lisos!
        // ==========================================
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        this.xOld = this.getX();
        this.yOld = this.getY();
        this.zOld = this.getZ();

        // ==========================================
        // 2. PARTÍCULAS (Roda só no Visual)
        // ==========================================
        if (this.level().isClientSide()) {
            this.level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, this.getX(), this.getY() + 2, this.getZ(), 0,
                    0.2, 0);
            // APAGAMOS O "return;" QUE ESTAVA AQUI!
        }

        // ==========================================
        // 3. FÍSICA PARA AMBOS (Cliente e Servidor movem juntos em tempo real)
        // ==========================================
        this.lifeTime++;
        Vec3 move = this.getDeltaMovement();

        // Se quiser que ele comece um pouco mais devagar e acelere,
        // descomente a linha abaixo (Fica com mais cara de trem pesado ganhando
        // velocidade!):
        // this.setDeltaMovement(move.scale(1.05));

        this.setPos(this.getX() + move.x, this.getY() + move.y, this.getZ() + move.z);

        // ==========================================
        // 4. COLISÃO E DANO (SÓ NO SERVIDOR para evitar bugs)
        // ==========================================
        if (!this.level().isClientSide()) {
            var hitBox = this.getBoundingBox().inflate(1.5);
            var targets = this.level().getEntitiesOfClass(LivingEntity.class, hitBox, e -> e != this.getOwner());

            for (LivingEntity target : targets) {
                if (!draggedVictims.contains(target)) {
                    draggedVictims.add(target);
                    
                    // Dano MASSIVO imediato que gera a repulsão natural do Minecraft
                    target.hurt(this.damageSources().mobAttack((LivingEntity) this.getOwner()), 30.0F); 
                    target.hurtMarked = true;
                    
                    // Som de impacto
                    this.level().playSound(null, this.blockPosition(),
                            SoundEvents.GENERIC_EXPLODE.value(),
                            SoundSource.PLAYERS, 1.5F, 0.7F);
                    this.level().playSound(null, this.blockPosition(),
                            SoundEvents.ZOMBIE_ATTACK_IRON_DOOR,
                            SoundSource.PLAYERS, 2.0F, 0.5F);
                }
            }

            // Simplesmente remove a entidade quando o tempo de vida acaba sem arrastar ninguém
            if (this.lifeTime >= 40) {
                this.discard();
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    // Animação do Trem (Para o Blockbench)
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}