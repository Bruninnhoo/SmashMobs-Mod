package net.brunodev.smashmobs.server;

import net.brunodev.smashmobs.SmashMobs;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;

@EventBusSubscriber(modid = SmashMobs.MODID)
public class GameRulesEvents {

    // --- REGRA 1: SEM DANO DE QUEDA ---
    @SubscribeEvent
    public static void onPlayerFall(LivingFallEvent event) {
        // Se for um jogador E o jogo estiver rodando...
        if (event.getEntity() instanceof Player && GameManager.isGameRunning) {
            // Anula o evento de queda! O jogador bate no chão como uma pluma.
            event.setCanceled(true);
        }
    }
}