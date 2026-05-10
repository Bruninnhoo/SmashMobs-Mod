package net.brunodev.smashmobs.item;
 
import net.brunodev.smashmobs.server.AbilityEvents;
import net.brunodev.smashmobs.network.AnvilAnimPayload;
import net.brunodev.smashmobs.client.ClientEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.PacketDistributor;
 
public class GolemAnvilItem extends SmashMobItemBase {
 
    public GolemAnvilItem(Properties properties) {
        super(properties);
    }
 
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            player.getCooldowns().addCooldown(player.getItemInHand(hand), 100);
 
            AbilityEvents.PENDING_ANVILS.put(player.getUUID(), 35);
 
            level.playSound(null, player.blockPosition(), SoundEvents.IRON_GOLEM_REPAIR,
                    SoundSource.PLAYERS, 1.0F, 0.8F);
 
            PacketDistributor.sendToPlayersTrackingEntity(
                    player,
                    new AnvilAnimPayload(player.getUUID()));
        } else {
            ClientEvents.playAnvilAnimation(player.getUUID());
        }
 
        return InteractionResult.SUCCESS;
    }
}
