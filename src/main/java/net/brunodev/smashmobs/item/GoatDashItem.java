package net.brunodev.smashmobs.item;
 
import net.brunodev.smashmobs.server.AbilityEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
 
public class GoatDashItem extends Item {
    public GoatDashItem(Properties properties) {
        super(properties);
    }
 
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            level.playSound(null, player.blockPosition(), SoundEvents.GOAT_PREPARE_RAM, SoundSource.PLAYERS, 1.5F, 1.0F);
 
            AbilityEvents.DASHING_GOATS.put(player.getUUID(), 12);
 
            player.getCooldowns().addCooldown(player.getItemInHand(hand), 100);
        }
        return InteractionResult.SUCCESS;
    }
}