package net.brunodev.smashmobs.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.brunodev.smashmobs.registration.ModAttachments;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = "smashmobs")
public class MorphCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("morph")
                // A SOLUÇÃO: greedyString() captura o "minecraft:creeper" inteiro sem quebrar no ':'
                .then(Commands.argument("mob", StringArgumentType.greedyString())
                        .executes(context -> {
                            Player player = context.getSource().getPlayerOrException();
                            String mobId = StringArgumentType.getString(context, "mob");

                            // Salva a string completa no seu Data Attachment
                            player.setData(ModAttachments.MORPH_DATA, mobId);

                            context.getSource().sendSuccess(() -> Component.literal("Morph alterado para: " + mobId), false);
                            return 1;
                        })
                )
                // Rota 2: /morph (Sem argumentos, limpa o morph)
                .executes(context -> {
                    Player player = context.getSource().getPlayerOrException();
                    player.setData(ModAttachments.MORPH_DATA, "none");
                    context.getSource().sendSuccess(() -> Component.literal("Morph removido!"), false);
                    return 1;
                })
        );
    }
}