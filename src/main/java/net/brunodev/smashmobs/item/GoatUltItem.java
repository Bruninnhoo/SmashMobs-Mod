package net.brunodev.smashmobs.item;

import net.brunodev.smashmobs.server.AbilityEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class GoatUltItem extends Item {
    public GoatUltItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {

            // Só ativa se não estiver voando/girando já
            if (!AbilityEvents.GOAT_AVALANCHES.containsKey(player.getUUID())) {
                AbilityEvents.GOAT_AVALANCHES.put(player.getUUID(), 60); // 3 Segundos de Ultimate (Fase de Rodopio)

                // Som de ativação e Grito Inicial
                level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.GOAT_SCREAMING_AMBIENT,
                        net.minecraft.sounds.SoundSource.PLAYERS, 2.0F, 1.0F);
                level.playSound(null, player.blockPosition(),
                        net.minecraft.sounds.SoundEvents.WIND_CHARGE_BURST.value(),
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.5F, 0.5F);

                player.getCooldowns().addCooldown(player.getItemInHand(hand), 1200); // 60 segundos de cooldown
            }
        }
        return InteractionResult.SUCCESS;
    }
}
