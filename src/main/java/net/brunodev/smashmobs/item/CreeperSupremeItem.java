package net.brunodev.smashmobs.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CreeperSupremeItem extends Item {

    public CreeperSupremeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // Verifica se o cara já não está usando a suprema
            if (!net.brunodev.smashmobs.server.AbilityEvents.CREEPER_SUPREMES.containsKey(player.getUUID())) {

                // 1. Arma a suprema por 60 Ticks (3 segundos puxando os caras)
                net.brunodev.smashmobs.server.AbilityEvents.CREEPER_SUPREMES.put(player.getUUID(), 60);

                // 2. Toca um som inicial (aquele chiado clássico)
                level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.CREEPER_PRIMED, net.minecraft.sounds.SoundSource.PLAYERS, 2.0F, 0.5F);

                // 3. Dá um cooldown gigante, porque é uma suprema (ex: 20 segundos = 400 ticks)
                player.getCooldowns().addCooldown(itemStack, 400);
            }
        }

        return InteractionResult.SUCCESS;
    }
}