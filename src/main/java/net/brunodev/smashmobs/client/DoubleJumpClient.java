package net.brunodev.smashmobs.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = "smashmobs", value = Dist.CLIENT)
public class DoubleJumpClient {

    private static int jumpCount = 0; // 0 = no chão, 1 = primeiro pulo, 2 = pulo duplo
    private static boolean wasJumping = false;
    private static float jumpTimer = 0.0F;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null)
            return;

        // Verifica se o jogo começou e o jogador está vivo (Vidas > 0)
        int lives = player.getData(net.brunodev.smashmobs.registration.ModAttachments.PLAYER_LIVES);
        if (lives <= 0) {
            return;
        }

        if (jumpTimer > 0) {
            jumpTimer--;
        }

        // 1. RESET: Se encostou no chão, o contador volta a zero
        if (player.onGround() || player.onClimbable()) {
            jumpCount = 0;
            wasJumping = mc.options.keyJump.isDown();
            return;
        }

        boolean isJumping = mc.options.keyJump.isDown();

        // 2. DETECÇÃO DE NOVO APERTO (Segunda Instância)
        if (isJumping && !wasJumping) {

            // Se o cara pulou do chão, ele agora está no ar (jumpCount vira 1)
            // Mas se ele apertar espaço DE NOVO enquanto está no ar (jumpCount vira 2)
            jumpCount++;

            if (jumpCount == 2) {
                if (jumpTimer > 0) {
                    jumpCount = 1; // Cancela o pulo duplo e permite tentar de novo se o tempo acabar ainda no ar
                } else {
                    // --- O PULO DUPLO SÓ ACONTECE NO SEGUNDO APERTO ---
                    jumpTimer = 20; // 1 segundo de cooldown após um duplo pulo (20 ticks)

                    Vec3 look = player.getLookAngle();
                    // Cria um dash para a direção horizontal onde o jogador está olhando!
                    // Aumentamos consideravelmente a propulsão horizontal para dar o feeling de "buff"
                    double dashPower = 1.1D; 
                    double jumpPower = 0.75D; // 0.75D é excelente para altura equilibrada com a distância horizontal

                    player.setDeltaMovement(look.x * dashPower, jumpPower, look.z * dashPower);

                    player.fallDistance = 0.0F;

                // Efeitos para dar o "feeling" de Smash
                player.level().playSound(player, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,
                        SoundSource.PLAYERS, 1.0F, 2.0F);

                for (int i = 0; i < 6; i++) {
                    player.level().addParticle(ParticleTypes.CLOUD,
                            player.getX() + (Math.random() - 0.5),
                            player.getY() + 0.1,
                            player.getZ() + (Math.random() - 0.5),
                            0, -0.05, 0);
                }
                }
            }
        }

        wasJumping = isJumping;
    }

}