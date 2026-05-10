package net.brunodev.smashmobs.item;
 
import net.brunodev.smashmobs.server.AbilityEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
 
public class SkeletonBoomerangItem extends SmashMobItemBase {
    public SkeletonBoomerangItem(Properties properties) {
        super(properties);
    }
 
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            AbilityEvents.spawnBoomerangBone(player);
 
            level.playSound(null, player.blockPosition(), SoundEvents.SKELETON_SHOOT,
                    SoundSource.PLAYERS, 1.0F, 1.5F);
 
            player.getCooldowns().addCooldown(player.getItemInHand(hand), 60);
        }
        return InteractionResult.SUCCESS;
    }
}

