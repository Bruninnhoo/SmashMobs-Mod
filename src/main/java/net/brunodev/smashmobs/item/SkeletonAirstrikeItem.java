package net.brunodev.smashmobs.item;
 
import net.brunodev.smashmobs.server.AbilityEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
 
public class SkeletonAirstrikeItem extends SmashMobItemBase {
    public SkeletonAirstrikeItem(Properties properties) {
        super(properties);
    }
 
    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // Deixa encantado visualmente
    }
 
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            AbilityEvents.startAirStrike(player);
            stack.consume(1, player); // Metodo oficial 1.21 para consumir o item de forma sincronizada
            player.getCooldowns().addCooldown(stack, 60);
        }
        return InteractionResult.SUCCESS;
    }
}

