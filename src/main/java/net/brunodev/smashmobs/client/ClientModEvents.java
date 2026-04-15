package net.brunodev.smashmobs.client;

import net.brunodev.smashmobs.SmashMobs;
import net.minecraft.client.renderer.entity.TntRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = SmashMobs.MODID, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(SmashMobs.SMASH_TNT.get(), TntRenderer::new);

        // VOLTAMOS AO PADRÃO LIMPO E OFICIAL!
        event.registerEntityRenderer(
                SmashMobs.IRON_GOLEM_MORPH.get(),
                context -> new software.bernie.geckolib.renderer.GeoEntityRenderer<>(context, SmashMobs.IRON_GOLEM_MORPH.get())
        );

        event.registerEntityRenderer(
                SmashMobs.GOLEM_TRAIN.get(),
                context -> new software.bernie.geckolib.renderer.GeoEntityRenderer<>(context, SmashMobs.GOLEM_TRAIN.get()) // Você precisará criar o Modelo GeoModel do trem igual fez com o Golem!
        );
    }
}