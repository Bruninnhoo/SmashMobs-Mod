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
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
 
public class ChickenUltItem extends Item {
    public ChickenUltItem(Properties properties) {
        super(properties);
    }
 
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            if (!AbilityEvents.CHICKEN_BOMBERS.containsKey(player.getUUID())) {
                AbilityEvents.CHICKEN_BOMBERS.put(player.getUUID(), 160); 
 
                player.setDeltaMovement(0, 1.2, 0);
                player.hurtMarked = true;
 
                level.playSound(null, player.blockPosition(), SoundEvents.CHICKEN_HURT,
                        SoundSource.PLAYERS, 2.0F, 0.5F);
                level.playSound(null, player.blockPosition(), SoundEvents.BAT_TAKEOFF,
                        SoundSource.PLAYERS, 2.0F, 1.0F);
            } else {
                int cooldownTick = AbilityEvents.CHICKEN_BOMBER_COOLDOWN.getOrDefault(player.getUUID(), 0);
                if (cooldownTick == 0) {
                    Projectile bomberEgg = EntityType.EGG.create(level, EntitySpawnReason.TRIGGERED);
                    if (bomberEgg instanceof ThrowableProjectile throwProp) {
                        throwProp.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
                        throwProp.setOwner(player);
                        throwProp.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.8F, 0.0F);
                        level.addFreshEntity(throwProp);
                        AbilityEvents.CHICKEN_BOMBER_EGGS.add(throwProp);
                    }
 
                    level.playSound(null, player.blockPosition(),
                            SoundEvents.GENERIC_EXPLODE.value(),
                            SoundSource.PLAYERS, 0.5F, 2.0F);
                    level.playSound(null, player.blockPosition(), SoundEvents.CHICKEN_EGG,
                            SoundSource.PLAYERS, 1.5F, 0.5F);
 
                    AbilityEvents.CHICKEN_BOMBER_EGGS.add(bomberEgg);
                    AbilityEvents.CHICKEN_BOMBER_COOLDOWN.put(player.getUUID(), 10);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}
