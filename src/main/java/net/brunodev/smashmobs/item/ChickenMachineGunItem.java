package net.brunodev.smashmobs.item;

import net.brunodev.smashmobs.server.AbilityEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;

public class ChickenMachineGunItem extends Item {
    public ChickenMachineGunItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            // Se já não estiver disparando, começa o burst de 40 ticks (20 ovos)
            if (!AbilityEvents.CHICKEN_MACHINE_GUN_ACTIVE.containsKey(player.getUUID())) {
                AbilityEvents.CHICKEN_MACHINE_GUN_ACTIVE.put(player.getUUID(), 40);

                // Som de ativação inicial (opcional, dá um feedback imediato)
                level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.CHICKEN_EGG,
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.5F);

                // Adiciona cooldown para não spammar bursts
                player.getCooldowns().addCooldown(player.getItemInHand(hand), 100); // 5 segundos
            }
        }
        return InteractionResult.SUCCESS;
    }
}
