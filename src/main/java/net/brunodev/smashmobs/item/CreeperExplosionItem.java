package net.brunodev.smashmobs.item;

import net.brunodev.smashmobs.SmashMobs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CreeperExplosionItem extends Item {

    public CreeperExplosionItem(Properties properties) {
        super(properties);
    }

    // Esse método é ativado sempre que o jogador clica com o botão direito (ou segura a tela no mobile)
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide()) {

            // 1. Pega a direção que o jogador está olhando
            var look = player.getLookAngle();

            level.playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    SmashMobs.CREEPER_LAUNCH_SOUND.get(),
                    net.minecraft.sounds.SoundSource.PLAYERS,
                    0.8F, // Volume
                    1.0F  // Pitch
            );

            // 2. O Lançamento! (Multiplica o X e Z para ir pra frente, e o Y para ir pra cima)
            // Sinta-se à vontade para mudar esses números (1.5 e 1.2) para deixar o pulo mais forte ou mais fraco
            player.setDeltaMovement(look.x * 1.5, 1.2, look.z * 1.5);
            player.hurtMarked = true; // Avisa o jogo para atualizar o seu movimento na tela

            // 3. Arma a bomba! Coloca o jogador na lista de explosão
            net.brunodev.smashmobs.server.AbilityEvents.CREEPER_ARMED_PLAYERS.add(player.getUUID());

            // 4. Inicia o Cooldown
            player.getCooldowns().addCooldown(itemStack, 100);
        }

        return InteractionResult.SUCCESS;
    }
}