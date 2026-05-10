package net.brunodev.smashmobs.item;
 
import net.brunodev.smashmobs.server.AbilityEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
 
public class GoatUltItem extends Item {
    public GoatUltItem(Properties properties) {
        super(properties);
    }
 
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            if (!AbilityEvents.GOAT_AVALANCHES.containsKey(player.getUUID())) {
                AbilityEvents.GOAT_AVALANCHES.put(player.getUUID(), 60); 
 
                level.playSound(null, player.blockPosition(), SoundEvents.GOAT_SCREAMING_AMBIENT,
                        SoundSource.PLAYERS, 2.0F, 1.0F);
                level.playSound(null, player.blockPosition(),
                        SoundEvents.WIND_CHARGE_BURST.value(),
                        SoundSource.PLAYERS, 1.5F, 0.5F);
 
                player.getCooldowns().addCooldown(player.getItemInHand(hand), 1200); 
            }
        }
        return InteractionResult.SUCCESS;
    }
}
