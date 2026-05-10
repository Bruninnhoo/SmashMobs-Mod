package net.brunodev.smashmobs.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class IronGolemMorph extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // ========================================================
    // O SEGREDO ESTÁ AQUI: A variável precisa existir no topo!
    // ========================================================
    public boolean isPlayerMoving = false;
    public int attackTick = 0;

    public IronGolemMorph(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

        // CÉREBRO 1: Movimento (Não mexe, tá perfeito!)
        controllers.add(new AnimationController<>("movement", 5, event -> {
            if (isPlayerMoving) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("walk"));
            }
            return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }));

        // ========================================================
        // CÉREBRO 2: Ação via Estado (Substitua tudo por este!)
        // ========================================================
        controllers.add(new AnimationController<>("action", 2, event -> PlayState.STOP)
                .triggerableAnim("throw", RawAnimation.begin().thenPlay("throw_anvil")));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // ========================================================
    // Mantenha os seus métodos de isPushable() e canBeCollidedWith() aqui embaixo!
    // ========================================================
    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false; // Desliga a seleção
    }

    @Override
    public boolean isAttackable() {
        return false; // Impede o jogo de registrar ataques diretamente nele
    }

    @Override
    public boolean canBeCollidedWith(Entity entity) {
        return false; // Remove colisão com projéteis e jogadores
    }

    @Override
    public boolean skipAttackInteraction(Entity entity) {
        return true; // Pula qualquer processamento de hit local
    }

    @Override
    protected void doPush(Entity entity) {
    }
}