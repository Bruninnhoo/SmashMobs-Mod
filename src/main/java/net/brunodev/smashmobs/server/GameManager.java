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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;

public class GameManager {

    // O status do jogo (se está rolando ou na tela de espera)
    public static boolean isGameRunning = false;

    // Método para dar a largada!
    public static void startGame(Iterable<ServerPlayer> players) {
        isGameRunning = true;

        MinecraftServer server = null;
        net.minecraft.world.phys.Vec3 arenaSpawn = SmashPositionManager.getArenaVec();

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
                player.setGameMode(GameType.ADVENTURE); // ou ADVENTURE
            }

            if (arenaSpawn != null) {
                player.teleportTo(arenaSpawn.x, arenaSpawn.y, arenaSpawn.z);
            } else {
                player.teleportTo(0, 100, 0);
            }
        }

        if (server != null) {
            Scoreboard scoreboard = server.getScoreboard();
            Objective objective = scoreboard.getObjective("smash_percent");
            if (objective == null) {
                objective = scoreboard.addObjective("smash_percent", ObjectiveCriteria.DUMMY,
                        Component.literal("Dano %"), ObjectiveCriteria.RenderType.INTEGER, true, null);
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

        net.minecraft.world.phys.Vec3 lobbySpawn = SmashPositionManager.getLobbyVec();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.setData(ModAttachments.PLAYER_LIVES, 0);
            player.setData(ModAttachments.DAMAGE_PERCENT, 0.0f);
            // Teleporta geral de volta pro lobby quando o admin acaba a partida
            player.setGameMode(GameType.ADVENTURE); // Garante que ninguém fique como spec
            player.teleportTo(lobbySpawn.x, lobbySpawn.y, lobbySpawn.z);
            player.setDeltaMovement(0, 0, 0);
            player.fallDistance = 0;
            player.setHealth(player.getMaxHealth());
        }

        // Limpa as minas da galinha
        for (ItemEntity mine : AbilityEvents.CHICKEN_MINES.keySet()) {
            if (mine != null && mine.isAlive()) {
                mine.discard(); // Deleta a mina do mundo
            }
        }
        AbilityEvents.CHICKEN_MINES.clear();

        // Limpa bigornas
        for (FallingBlockEntity anvil : AbilityEvents.FLYING_ANVILS.keySet()) {
            if (anvil != null && anvil.isAlive()) {
                anvil.discard();
            }
        }
        AbilityEvents.FLYING_ANVILS.clear();

        // Limpa poderes do esqueleto
        AbilityEvents.FLYING_BONES.clear();
        AbilityEvents.ARROW_STORMS.clear();
    }

    // NOVA LÓGICA: VERIFICA SE ALGUEM VENCEU!
    public static void checkWinCondition(MinecraftServer server) {
        if (!isGameRunning) return;

        java.util.List<ServerPlayer> alivePlayers = new java.util.ArrayList<>();
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            Integer lives = p.getData(ModAttachments.PLAYER_LIVES);
            // Um jogador está ativo se tem vidas > 0 e não está espectador
            if (lives != null && lives > 0 && p.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
                alivePlayers.add(p);
            }
        }

        // Condição de vitória: Só sobrou 1!
        if (alivePlayers.size() == 1) {
            declareWinner(server, alivePlayers.get(0));
        } else if (alivePlayers.size() == 0) {
            // Prevenção de bugs se os dois últimos caírem juntos
            endGame(server);
        }
    }

    private static void declareWinner(MinecraftServer server, ServerPlayer winner) {
        isGameRunning = false; // Para a partida imediatamente

        server.getPlayerList().broadcastSystemMessage(Component.empty(), false);
        server.getPlayerList().broadcastSystemMessage(
                Component.literal("§6§l★====================================★"), false);
        server.getPlayerList().broadcastSystemMessage(
                Component.literal("§e§l      🏆 VITÓRIA DO SMASH MOBS 🏆"), false);
        server.getPlayerList().broadcastSystemMessage(
                Component.literal("§f           Vencedor: §d§l" + winner.getScoreboardName()), false);
        server.getPlayerList().broadcastSystemMessage(
                Component.literal("§6§l★====================================★"), false);
        server.getPlayerList().broadcastSystemMessage(Component.empty(), false);

        net.minecraft.world.phys.Vec3 lobbySpawn = SmashPositionManager.getLobbyVec();

        // Teleporta geral de volta, cura e toca som de festa!
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.setData(ModAttachments.PLAYER_LIVES, 0);
            p.setData(ModAttachments.DAMAGE_PERCENT, 0.0f);
            p.setGameMode(GameType.ADVENTURE); 
            p.teleportTo(lobbySpawn.x, lobbySpawn.y, lobbySpawn.z);
            p.setDeltaMovement(0, 0, 0);
            p.fallDistance = 0;
            p.setHealth(p.getMaxHealth());
            
            // Toca o som de desafio concluído para celebrar!
            p.level().playSound(null, p.blockPosition(), net.minecraft.sounds.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 
                               net.minecraft.sounds.SoundSource.MASTER, 1.0F, 1.0F);
        }

        // Limpezas padrões (cópia simplificada da endGame)
        for (ItemEntity mine : AbilityEvents.CHICKEN_MINES.keySet()) if (mine != null && mine.isAlive()) mine.discard();
        AbilityEvents.CHICKEN_MINES.clear();
        for (FallingBlockEntity anvil : AbilityEvents.FLYING_ANVILS.keySet()) if (anvil != null && anvil.isAlive()) anvil.discard();
        AbilityEvents.FLYING_ANVILS.clear();
        AbilityEvents.FLYING_BONES.clear();
        AbilityEvents.ARROW_STORMS.clear();
    }
}