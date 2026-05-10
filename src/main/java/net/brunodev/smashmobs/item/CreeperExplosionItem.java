package net.brunodev.smashmobs.item;
 
import net.brunodev.smashmobs.SmashMobs;
import net.brunodev.smashmobs.server.AbilityEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundSource;
 
public class CreeperExplosionItem extends SmashMobItemBase {
 
    public CreeperExplosionItem(Properties properties) {
        super(properties);
    }
 
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
 
        if (!level.isClientSide()) {
            var look = player.getLookAngle();
 
            level.playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    SmashMobs.CREEPER_LAUNCH_SOUND.get(),
                    SoundSource.PLAYERS,
                    0.8F, 
                    1.0F 
            );
 
            player.setDeltaMovement(look.x * 1.5, 1.2, look.z * 1.5);
            player.hurtMarked = true; 
 
            AbilityEvents.CREEPER_ARMED_PLAYERS.add(player.getUUID());
            player.getCooldowns().addCooldown(itemStack, 100);
        }
 
        return InteractionResult.SUCCESS;
    }
}
