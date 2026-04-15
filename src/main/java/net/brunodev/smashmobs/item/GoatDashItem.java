package net.brunodev.smashmobs.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class GoatDashItem extends Item {
    public GoatDashItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            // 1. Toca o som característico da cabra gritando/preparando
            level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.GOAT_PREPARE_RAM, net.minecraft.sounds.SoundSource.PLAYERS, 1.5F, 1.0F);

            // 2. Coloca o jogador no modo "Cabeçada" por 12 ticks (um pouco mais de meio segundo de corrida)
            net.brunodev.smashmobs.server.AbilityEvents.DASHING_GOATS.put(player.getUUID(), 12);

            // 3. Cooldown rápido, já que é uma habilidade de movimento/ataque normal
            player.getCooldowns().addCooldown(player.getItemInHand(hand), 100); // 5 segundos
        }
        return InteractionResult.SUCCESS;
    }
}