package net.brunodev.smashmobs.item;
 
import net.brunodev.smashmobs.server.AbilityEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
 
public class ChickenTrapItem extends Item {
    public ChickenTrapItem(Properties properties) {
        super(properties);
    }
 
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            Vec3 pos = player.position().add(player.getLookAngle().scale(1.5)); 
 
            ItemEntity eggMine = new ItemEntity(level, pos.x, pos.y, pos.z, new ItemStack(Items.EGG));
            eggMine.setPickUpDelay(32767);
            eggMine.setNoGravity(false);
            eggMine.setInvulnerable(true);
            eggMine.setDeltaMovement(player.getLookAngle().scale(0.3));
 
            level.addFreshEntity(eggMine);
 
            level.playSound(null, player.blockPosition(), SoundEvents.CHICKEN_EGG,
                    SoundSource.PLAYERS, 1.0F, 0.5F);
 
            AbilityEvents.CHICKEN_MINES.put(eggMine, player.getUUID());
            player.getCooldowns().addCooldown(player.getItemInHand(hand), 100);
        }
 
        return InteractionResult.SUCCESS;
    }
}
