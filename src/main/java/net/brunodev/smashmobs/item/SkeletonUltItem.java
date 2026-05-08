package net.brunodev.smashmobs.item;

import net.brunodev.smashmobs.server.AbilityEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SkeletonUltItem extends Item {
    public SkeletonUltItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            Vec3 look = player.getLookAngle();
            // Posição alvo (10 blocos na frente)
            Vec3 target = player.position().add(look.scale(10.0));

            // Inicia a tempestade!
            AbilityEvents.ARROW_STORMS.add(new AbilityEvents.ArrowStorm(player, target, 100)); // 5 segundos de
                                                                                               // tempestade

            // Toca som de invocação
            level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.WITHER_SPAWN,
                    net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 1.5F);

            // Cooldown GIGANTE (Últimate)
            player.getCooldowns().addCooldown(player.getItemInHand(hand), 600); // 30 segundos
        }
        return InteractionResult.SUCCESS;
    }
}
