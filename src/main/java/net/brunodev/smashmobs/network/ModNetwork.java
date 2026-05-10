package net.brunodev.smashmobs.network;
 
import net.brunodev.smashmobs.SmashMobs;
import net.brunodev.smashmobs.mobs.CreeperClass;
import net.brunodev.smashmobs.mobs.GoatClass;
import net.brunodev.smashmobs.mobs.IronGolemClass;
import net.brunodev.smashmobs.mobs.ChickenClass;
import net.brunodev.smashmobs.mobs.SkeletonClass;
import net.brunodev.smashmobs.server.GameManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
 
@EventBusSubscriber(modid = SmashMobs.MODID)
public class ModNetwork {
 
    @SubscribeEvent
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
 
        event.registrar(SmashMobs.MODID).playToServer(
                MorphPacket.TYPE,
                MorphPacket.CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        var player = (ServerPlayer) context.player();
 
                        if (GameManager.isGameRunning) {
                            player.sendSystemMessage(Component
                                    .literal("§cO jogo já começou! Você não pode mais trocar de personagem."));
                            return;
                        }
 
                        if ("creeper".equals(payload.mobType())) {
                            new CreeperClass().equip(player);
                        } else if ("iron_golem".equals(payload.mobType())) {
                            new IronGolemClass().equip(player);
                        } else if ("goat".equals(payload.mobType())) {
                            new GoatClass().equip(player);
                        } else if ("chicken".equals(payload.mobType())) {
                            new ChickenClass().equip(player);
                        } else if ("skeleton".equals(payload.mobType())) {
                            new SkeletonClass().equip(player);
                        }
                    });
                });
 
        event.registrar(SmashMobs.MODID).playToClient(
                AnvilAnimPayload.TYPE,
                AnvilAnimPayload.STREAM_CODEC,
                AnvilAnimPayload::handle);
    }
}