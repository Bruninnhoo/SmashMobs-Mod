package net.brunodev.smashmobs.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class CreeperThrowTntItem extends Item {

    public CreeperThrowTntItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            // 1. Cria a TNT na posição do player
            var tnt = new net.brunodev.smashmobs.entity.SmashTntEntity(level, player.getX(), player.getEyeY(), player.getZ());

            // 2. O EMPURRÃO: Pega a direção do olhar e aplica força
            var look = player.getLookAngle();
            tnt.setDeltaMovement(look.x * 1.2, look.y * 1.2, look.z * 1.2);

            // 3. Spawna no mundo
            level.addFreshEntity(tnt);

            player.getCooldowns().addCooldown(player.getItemInHand(hand), 40);
        }
        return InteractionResult.SUCCESS;
    }
}