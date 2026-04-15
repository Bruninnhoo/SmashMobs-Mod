package net.brunodev.smashmobs.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class GolemGrabItem extends Item {

    public GolemGrabItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            // Chamamos o nosso "Gerenciador de Habilidades" para processar a física do agarrão
            net.brunodev.smashmobs.server.AbilityEvents.handleGolemGrab(player);

            // Inicia um cooldown visual rápido (meio segundo) só para não spammar o clique
            player.getCooldowns().addCooldown(player.getItemInHand(hand), 10);
        }
        return InteractionResult.SUCCESS;
    }
}