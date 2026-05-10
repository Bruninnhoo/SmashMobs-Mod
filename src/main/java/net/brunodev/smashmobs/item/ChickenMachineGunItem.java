package net.brunodev.smashmobs.item;
 
import net.brunodev.smashmobs.server.AbilityEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
 
public class ChickenMachineGunItem extends Item {
    public ChickenMachineGunItem(Properties properties) {
        super(properties);
    }
 
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            if (!AbilityEvents.CHICKEN_MACHINE_GUN_ACTIVE.containsKey(player.getUUID())) {
                AbilityEvents.CHICKEN_MACHINE_GUN_ACTIVE.put(player.getUUID(), 40);
 
                level.playSound(null, player.blockPosition(), SoundEvents.CHICKEN_EGG,
                        SoundSource.PLAYERS, 1.0F, 1.5F);
 
                player.getCooldowns().addCooldown(player.getItemInHand(hand), 100); 
            }
        }
        return InteractionResult.SUCCESS;
    }
}
