package net.brunodev.smashmobs.item;
 
import net.brunodev.smashmobs.server.AbilityEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
 
public class GoatSwallowItem extends Item {
    public GoatSwallowItem(Properties properties) {
        super(properties);
    }
 
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            if (AbilityEvents.GOAT_SWALLOWED.containsKey(player.getUUID())) {
                AbilityEvents.spitSwallowedEntity(player);
                return InteractionResult.SUCCESS;
            }
 
            level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_BURP,
                    SoundSource.PLAYERS, 1.0F, 1.5F);
            level.playSound(null, player.blockPosition(), SoundEvents.LLAMA_EAT,
                    SoundSource.PLAYERS, 1.5F, 1.0F);
 
            Vec3 eyePos = player.getEyePosition();
            Vec3 look = player.getLookAngle();
            double reach = 3.0; 
 
            AABB eatBox = player.getBoundingBox().inflate(0.5).expandTowards(look.scale(reach));
            var targets = level.getEntitiesOfClass(LivingEntity.class, eatBox, e -> e != player && e.isAlive());
 
            LivingEntity closestTarget = null;
            double closestDistanceSq = reach * reach;
 
            for (LivingEntity entity : targets) {
                AABB entityBox = entity.getBoundingBox().inflate(0.5);
                var hit = entityBox.clip(eyePos, eyePos.add(look.scale(reach)));
                if (hit.isPresent()) {
                    double distSq = eyePos.distanceToSqr(hit.get());
                    if (distSq < closestDistanceSq) {
                        closestDistanceSq = distSq;
                        closestTarget = entity;
                    }
                }
            }
 
            if (closestTarget != null) {
                AbilityEvents.GOAT_SWALLOWED.put(player.getUUID(), closestTarget.getUUID());
                AbilityEvents.GOAT_SWALLOWED_TIMERS.put(player.getUUID(), 60); 
 
                closestTarget.hurt(closestTarget.damageSources().mobAttack(player), 2.0F);
 
                level.playSound(null, closestTarget.blockPosition(), SoundEvents.SLIME_SQUISH,
                        SoundSource.PLAYERS, 2.0F, 0.5F);
            } else {
                player.getCooldowns().addCooldown(player.getItemInHand(hand), 20);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
