package net.brunodev.smashmobs.item;

import net.brunodev.smashmobs.server.AbilityEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class SkeletonBoomerangItem extends Item {
    public SkeletonBoomerangItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            AbilityEvents.spawnBoomerangBone(player);

            // Som de lançamento
            level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.SKELETON_SHOOT,
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.5F);

            // Cooldown de 3 segundos
            player.getCooldowns().addCooldown(player.getItemInHand(hand), 60);
        }
        return InteractionResult.SUCCESS;
    }
}
