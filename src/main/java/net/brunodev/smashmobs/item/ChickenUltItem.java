package net.brunodev.smashmobs.item;

import net.brunodev.smashmobs.server.AbilityEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.entity.EntityType;

public class ChickenUltItem extends Item {
    public ChickenUltItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {

        if (!level.isClientSide()) {

            // Se NÃO estiver na Ultimate, ativa a Ultimate (Começa a voar)
            if (!AbilityEvents.CHICKEN_BOMBERS.containsKey(player.getUUID())) {
                AbilityEvents.CHICKEN_BOMBERS.put(player.getUUID(), 160); // 8 segundos de ultimate

                // Pulo gigante ativador
                player.setDeltaMovement(0, 1.2, 0);
                player.hurtMarked = true;

                // Som de Galinha Maluca
                level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.CHICKEN_HURT,
                        net.minecraft.sounds.SoundSource.PLAYERS, 2.0F, 0.5F);
                level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.BAT_TAKEOFF,
                        net.minecraft.sounds.SoundSource.PLAYERS, 2.0F, 1.0F);

                // NOTA: Só damos o Cooldown no item no AbilityEvents QUANDO a ultimate acaba!
                // Assim o jogador consegue clicar o botão para atirar os ovos gigantes enquanto
                // está ativado.
            } else {
                // Se JÁ ESTIVER na ultimate, atirar um OVO BOMBARDEIRO!
                int cooldownTick = AbilityEvents.CHICKEN_BOMBER_COOLDOWN.getOrDefault(player.getUUID(), 0);
                if (cooldownTick == 0) {
                    Projectile bomberEgg = EntityType.EGG.create(level,
                            net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
                    if (bomberEgg instanceof ThrowableProjectile throwProp) {
                        throwProp.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
                        throwProp.setOwner(player);
                        throwProp.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.8F, 0.0F);
                        level.addFreshEntity(throwProp);
                        AbilityEvents.CHICKEN_BOMBER_EGGS.add(throwProp);
                    }

                    // Som de disparo pesado
                    level.playSound(null, player.blockPosition(),
                            net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value(),
                            net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 2.0F);
                    level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.CHICKEN_EGG,
                            net.minecraft.sounds.SoundSource.PLAYERS, 1.5F, 0.5F);

                    AbilityEvents.CHICKEN_BOMBER_EGGS.add(bomberEgg);
                    AbilityEvents.CHICKEN_BOMBER_COOLDOWN.put(player.getUUID(), 10); // 10 ticks de intervalo (Meio
                                                                                     // segundo) por bomba
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}
