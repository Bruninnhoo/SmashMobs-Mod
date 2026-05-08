package net.brunodev.smashmobs.server;

import net.brunodev.smashmobs.registration.ModAttachments;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraft.world.scores.DisplaySlot;

public class GameManager {

    // O status do jogo (se está rolando ou na tela de espera)
    public static boolean isGameRunning = false;

    // Método para dar a largada!
    public static void startGame(Iterable<ServerPlayer> players) {
        isGameRunning = true;
        
        MinecraftServer server = null;

        for (ServerPlayer player : players) {
            if (server == null) {
                server = player.level().getServer();
            }
            
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
        
        if (server != null) {
            Scoreboard scoreboard = server.getScoreboard();
            Objective objective = scoreboard.getObjective("smash_percent");
            if (objective == null) {
                objective = scoreboard.addObjective("smash_percent", ObjectiveCriteria.DUMMY, Component.literal("Dano %"), ObjectiveCriteria.RenderType.INTEGER, true, null);
            }
            scoreboard.setDisplayObjective(DisplaySlot.BELOW_NAME, objective);
            
            // Reseta a pontuação visual de todo mundo
            for (ServerPlayer player : players) {
                scoreboard.getOrCreatePlayerScore(player, objective).set(0);
            }
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

        // Limpa as minas da galinha
        for (net.minecraft.world.entity.item.ItemEntity mine : net.brunodev.smashmobs.server.AbilityEvents.CHICKEN_MINES.keySet()) {
            if (mine != null && mine.isAlive()) {
                mine.discard(); // Deleta a mina do mundo
            }
        }
        net.brunodev.smashmobs.server.AbilityEvents.CHICKEN_MINES.clear();
        
        // Limpa bigornas
        for (net.minecraft.world.entity.item.FallingBlockEntity anvil : net.brunodev.smashmobs.server.AbilityEvents.FLYING_ANVILS.keySet()) {
            if (anvil != null && anvil.isAlive()) {
                anvil.discard();
            }
        }
        net.brunodev.smashmobs.server.AbilityEvents.FLYING_ANVILS.clear();
        
        // Limpa poderes do esqueleto
        net.brunodev.smashmobs.server.AbilityEvents.FLYING_BONES.clear();
        net.brunodev.smashmobs.server.AbilityEvents.ARROW_STORMS.clear();
    }
}