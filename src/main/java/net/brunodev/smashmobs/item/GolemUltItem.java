package net.brunodev.smashmobs.item;
 
import net.brunodev.smashmobs.server.AbilityEvents;
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
            level.playSound(null, player.blockPosition(), SoundEvents.RAID_HORN.value(), SoundSource.PLAYERS, 4.0F, 0.8F);
 
            AbilityEvents.PENDING_TRAINS.put(player.getUUID(), 30);
 
            player.getCooldowns().addCooldown(player.getItemInHand(hand), 600);
        }
        return InteractionResult.SUCCESS;
    }
}