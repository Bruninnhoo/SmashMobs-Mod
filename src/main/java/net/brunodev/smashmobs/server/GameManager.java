package net.brunodev.smashmobs.server;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameManager {

    // O status do jogo (se está rolando ou na tela de espera)
    public static boolean isGameRunning = false;

    // Um "caderninho" que anota o ID de cada jogador e quantas vidas ele tem
    public static Map<UUID, Integer> playerLives = new HashMap<>();

    // Método para dar a largada!
    public static void startGame(Iterable<ServerPlayer> players) {
        isGameRunning = true;
        playerLives.clear();

        for (ServerPlayer player : players) {
            // Dá 3 vidas para todo mundo que estiver no servidor
            playerLives.put(player.getUUID(), 3);
            player.sendSystemMessage(Component.literal("§a⚔ O SMASH MOBS COMEÇOU! Você tem 3 vidas. Lute! ⚔"));

            // Zera a vida e a fome de todo mundo pro jogo ser justo
            player.setHealth(player.getMaxHealth());
            player.getFoodData().setFoodLevel(20);
        }
    }

    // Método para finalizar o jogo
    public static void endGame() {
        isGameRunning = false;
        playerLives.clear();
        // A lógica do ranking virá aqui!
    }
}