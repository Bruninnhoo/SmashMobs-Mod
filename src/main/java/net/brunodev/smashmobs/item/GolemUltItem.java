package net.brunodev.smashmobs.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class GolemUltItem extends Item {
    public GolemUltItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {

            // 1. Toca a Sirene de Aviso para todos ouvirem!
            level.playSound(null, player.blockPosition(), SoundEvents.RAID_HORN.value(), SoundSource.PLAYERS, 4.0F, 0.8F);

            // 2. Coloca o jogador na lista de espera. O AbilityEvents vai assumir a partir daqui!
            net.brunodev.smashmobs.server.AbilityEvents.PENDING_TRAINS.put(player.getUUID(), 30);

            // 3. Aplica o Cooldown
            player.getCooldowns().addCooldown(player.getItemInHand(hand), 600); // 30 segundos
        }
        return InteractionResult.SUCCESS;
    }
}