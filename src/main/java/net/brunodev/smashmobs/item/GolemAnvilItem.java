package net.brunodev.smashmobs.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class GolemAnvilItem extends Item {

    public GolemAnvilItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            // 1. Inicia o Cooldown
            player.getCooldowns().addCooldown(player.getItemInHand(hand), 100);

            // 2. Agenda o arremesso no servidor (30 ticks = ~1.5 segundos)
            net.brunodev.smashmobs.server.AbilityEvents.PENDING_ANVILS.put(player.getUUID(), 35);

            // 3. Toca o som da bigorna "surgindo" (metal rangendo ou chiado)
            level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.IRON_GOLEM_REPAIR, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.8F);

            // 4. Manda o aviso da animação para todos (Isso faz o braço começar a ir para trás AGORA)
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntity(
                    player,
                    new net.brunodev.smashmobs.network.AnvilAnimPayload(player.getUUID())
            );
        } else {
            // Instant-cast visual para o próprio jogador
            net.brunodev.smashmobs.client.ClientEvents.playAnvilAnimation(player.getUUID());
        }

        return InteractionResult.SUCCESS;
    }
}