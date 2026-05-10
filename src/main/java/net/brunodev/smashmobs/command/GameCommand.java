package net.brunodev.smashmobs.command;

import com.mojang.brigadier.CommandDispatcher;
import net.brunodev.smashmobs.server.GameManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

@EventBusSubscriber(modid = "smashmobs")
public class GameCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("smash")
                .then(Commands.literal("start")
                        .executes(context -> {
                            // Pega todos os jogadores do servidor
                            List<ServerPlayer> players = context.getSource().getServer().getPlayerList().getPlayers();
                            GameManager.startGame(players);
                            return 1;
                        })
                )
                .then(Commands.literal("stop")
                        .executes(context -> {
                            GameManager.endGame(context.getSource().getServer());
                            return 1;
                        })
                )
                .then(Commands.literal("setlobby")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            net.brunodev.smashmobs.server.SmashPositionManager.setLobbyPos(player.blockPosition());
                            context.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("§a[Smash] Lobby definido na sua posição!"), true);
                            return 1;
                        })
                )
                .then(Commands.literal("setarena")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            net.brunodev.smashmobs.server.SmashPositionManager.setArenaPos(player.blockPosition());
                            context.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("§6[Smash] Centro da Arena definido na sua posição!"), true);
                            return 1;
                        })
                )
        );
    }
}
