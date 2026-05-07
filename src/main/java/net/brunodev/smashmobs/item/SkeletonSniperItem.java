package net.brunodev.smashmobs.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.brunodev.smashmobs.SmashMobs;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class SkeletonSniperItem extends Item {
    public SkeletonSniperItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // Cria uma flecha super rápida e forte usando ArrowItem para evitar erro de
            // pacote
            net.minecraft.world.item.ArrowItem arrowItem = (net.minecraft.world.item.ArrowItem) net.minecraft.world.item.Items.ARROW;

            // createArrow(Level, ItemStack ammo, LivingEntity shooter, ItemStack weapon) -
            // no 1.21.1 pode ter mudado,
            // mas podemos testar o método padrão createArrow
            var arrow = arrowItem.createArrow(level, new ItemStack(net.minecraft.world.item.Items.ARROW), player,
                    itemstack);

            if (arrow != null) {
                arrow.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
                arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 5.0F, 0.1F); // 5.0F é MUITO
                                                                                                       // rápido

                // setBaseDamage() existe em AbstractArrow
                arrow.setBaseDamage(8.0); // Alto dano base!
                arrow.setCritArrow(true);

                // Atirar a flecha
                level.addFreshEntity(arrow);
            }

            // Som do tiro
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SKELETON_SHOOT, SoundSource.PLAYERS, 1.5F, 0.5F); // Som grave pra parecer um sniper
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SmashMobs.SKELETON_SHOOT_SOUND.get(), SoundSource.PLAYERS, 1.0F, 1.5F);

            // Cooldown de 4 segundos (80 ticks) para não metralhar
            player.getCooldowns().addCooldown(itemstack, 80);
        }

        player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }
}
