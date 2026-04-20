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

public class ChickenTrapItem extends Item {
    public ChickenTrapItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {

        if (!level.isClientSide()) {
            // Cria um "falso" ovo no chão
            Vec3 pos = player.position().add(player.getLookAngle().scale(1.5)); // Dropar um pouquinho na frente

            ItemEntity eggMine = new ItemEntity(level, pos.x, pos.y, pos.z, new ItemStack(Items.EGG));
            eggMine.setPickUpDelay(32767); // Nunca pode ser pego
            eggMine.setNoGravity(false);
            eggMine.setInvulnerable(true); // Indestrutível
            // Fazemos ele quicar como se tivesse sido jogado
            eggMine.setDeltaMovement(player.getLookAngle().scale(0.3));

            level.addFreshEntity(eggMine);

            // Som de "botou o ovo"
            level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.CHICKEN_EGG,
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.5F);

            // Registra a mina armada no servidor
            AbilityEvents.CHICKEN_MINES.put(eggMine, player.getUUID());

            // Cooldown de 5 segundos
            player.getCooldowns().addCooldown(player.getItemInHand(hand), 100);
        }

        return InteractionResult.SUCCESS;
    }
}
