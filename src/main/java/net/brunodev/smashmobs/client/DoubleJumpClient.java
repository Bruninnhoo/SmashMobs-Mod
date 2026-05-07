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

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null)
            return;

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
                // --- O PULO DUPLO SÓ ACONTECE NO SEGUNDO APERTO ---

                Vec3 currentMotion = player.getDeltaMovement();
                // 0.5D é uma altura ótima para double jump (nem muito alto, nem muito baixo)
                player.setDeltaMovement(currentMotion.x * 1.4, 1.5D, currentMotion.z * 1.4);

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

        wasJumping = isJumping;
    }
}