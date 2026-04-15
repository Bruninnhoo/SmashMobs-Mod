package net.brunodev.smashmobs.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class GoatTongueItem extends Item {
    public GoatTongueItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {

            // Som de cuspe/língua (Slime esticando)
            level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.SLIME_BLOCK_PLACE, net.minecraft.sounds.SoundSource.PLAYERS, 1.5F, 0.5F);

            Vec3 eyePos = player.getEyePosition();
            Vec3 look = player.getLookAngle();
            double reach = 10.0; // Alcance da língua

            // Cria uma caixa de colisão esticada para frente para "pescar" o inimigo
            AABB pullBox = player.getBoundingBox().inflate(1.0).expandTowards(look.scale(reach));
            var targets = level.getEntitiesOfClass(LivingEntity.class, pullBox, e -> e != player && e.isAlive());

            LivingEntity closestTarget = null;
            double closestDistanceSq = reach * reach;

            // Filtro Sniper (pega o cara exato na mira)
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
                // ACERTOU A LÍNGUA! Gruda ele!
                // Dá um tempinho de 4 segundos (80 ticks) de humilhação
                net.brunodev.smashmobs.server.AbilityEvents.GOAT_LEASHES.put(player.getUUID(), closestTarget.getUUID());
                net.brunodev.smashmobs.server.AbilityEvents.GOAT_LEASH_TIMERS.put(player.getUUID(), 80);

                // Som de "Grudou"
                level.playSound(null, closestTarget.blockPosition(), net.minecraft.sounds.SoundEvents.HONEY_BLOCK_FALL, net.minecraft.sounds.SoundSource.PLAYERS, 2.0F, 1.5F);
            }

            player.getCooldowns().addCooldown(player.getItemInHand(hand), 200); // 10 segundos de cooldown
        }
        return InteractionResult.SUCCESS;
    }
}