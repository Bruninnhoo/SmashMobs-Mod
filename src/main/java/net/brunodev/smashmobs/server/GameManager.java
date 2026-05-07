package net.brunodev.smashmobs.server;

import net.brunodev.smashmobs.registration.ModAttachments;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameType;

public class GameManager {

    // O status do jogo (se está rolando ou na tela de espera)
    public static boolean isGameRunning = false;

    // Método para dar a largada!
    public static void startGame(Iterable<ServerPlayer> players) {
        isGameRunning = true;



        for (ServerPlayer player : players) {
            // Seta os status de todos
            player.setData(ModAttachments.PLAYER_LIVES, 3);
            player.setData(ModAttachments.DAMAGE_PERCENT, 0.0f);

            player.sendSystemMessage(Component.literal("§a⚔ O SMASH MOBS COMEÇOU! Você tem 3 vidas. Lute! ⚔"));

            // Zera a vida e a fome de todo mundo pro jogo ser justo
            player.setHealth(player.getMaxHealth());
            player.getFoodData().setFoodLevel(20);

            // Tira do modo espectador se estivesse morto
            if (player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
                player.setGameMode(GameType.SURVIVAL); // ou ADVENTURE
            }

            player.teleportTo(0, 100, 0);
        }
    }

    // Método para finalizar o jogo
    public static void endGame(MinecraftServer server) {
        isGameRunning = false;

        server.getPlayerList().broadcastSystemMessage(
                Component.literal("§c🛑 A partida foi encerrada pelo Administrador!"), false);

        // Pode colocar todos em modo aventura ou resetar algo
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.setData(ModAttachments.PLAYER_LIVES, 0);
            player.setData(ModAttachments.DAMAGE_PERCENT, 0.0f);
        }
    }
}