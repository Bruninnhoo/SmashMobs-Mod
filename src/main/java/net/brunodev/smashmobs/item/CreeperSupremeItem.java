package net.brunodev.smashmobs.item;
 
import net.brunodev.smashmobs.server.AbilityEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
 
public class CreeperSupremeItem extends Item {
 
    public CreeperSupremeItem(Properties properties) {
        super(properties);
    }
 
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
 
        if (!level.isClientSide()) {
            if (!AbilityEvents.CREEPER_SUPREMES.containsKey(player.getUUID())) {
 
                AbilityEvents.CREEPER_SUPREMES.put(player.getUUID(), 60);
 
                level.playSound(null, player.blockPosition(), SoundEvents.CREEPER_PRIMED, SoundSource.PLAYERS, 2.0F, 0.5F);
 
                player.getCooldowns().addCooldown(itemStack, 400);
            }
        }
 
        return InteractionResult.SUCCESS;
    }
}