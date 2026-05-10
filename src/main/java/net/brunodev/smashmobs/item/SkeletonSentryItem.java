package net.brunodev.smashmobs.item;
 
import net.brunodev.smashmobs.server.AbilityEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
 
public class SkeletonSentryItem extends SmashMobItemBase {
    public SkeletonSentryItem(Properties properties) {
        super(properties);
    }
 
    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // Brilho encantado
    }
 
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            AbilityEvents.spawnSentryGun(player);
            stack.consume(1, player);
            player.getCooldowns().addCooldown(stack, 60);
        }
        return InteractionResult.SUCCESS;
    }
}

