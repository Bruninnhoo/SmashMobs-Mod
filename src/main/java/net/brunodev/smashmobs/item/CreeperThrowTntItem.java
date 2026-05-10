package net.brunodev.smashmobs.item;
 
import net.brunodev.smashmobs.entity.SmashTntEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
 
public class CreeperThrowTntItem extends SmashMobItemBase {
 
    public CreeperThrowTntItem(Properties properties) {
        super(properties);
    }
 
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            SmashTntEntity tnt = new SmashTntEntity(level, player.getX(), player.getEyeY(), player.getZ());
 
            var look = player.getLookAngle();
            tnt.setDeltaMovement(look.x * 1.2, look.y * 1.2, look.z * 1.2);
            
            // CORREÇÃO: Atribuir o dono para o sistema de Smash saber de QUEM foi a explosão!
            tnt.setOwner(player);
 
            level.addFreshEntity(tnt);
 
            player.getCooldowns().addCooldown(player.getItemInHand(hand), 40);
        }
        return InteractionResult.SUCCESS;
    }
}
