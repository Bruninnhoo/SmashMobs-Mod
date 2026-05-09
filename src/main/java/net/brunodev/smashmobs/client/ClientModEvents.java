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

        event.registerEntityRenderer(
                SmashMobs.SKELETON_MORPH.get(),
                context -> {
                    var model = new software.bernie.geckolib.model.DefaultedEntityGeoModel<net.brunodev.smashmobs.entity.SkeletonMorph>(
                        net.minecraft.resources.Identifier.parse("smashmobs:skeleton_morph"),
                        "head"
                    );
                    var renderer = new software.bernie.geckolib.renderer.GeoEntityRenderer<>(context, model);
                    return renderer.withRenderLayer(new software.bernie.geckolib.renderer.layer.builtin.ItemInHandGeoLayer<>(renderer, "rightItem", "leftItem"));
                }
        );

        event.registerEntityRenderer(SmashMobs.BULLET.get(), context -> 
            new software.bernie.geckolib.renderer.GeoEntityRenderer<>(context, 
                new software.bernie.geckolib.model.DefaultedEntityGeoModel<>(net.minecraft.resources.Identifier.parse("smashmobs:bullet")))
        );

        event.registerEntityRenderer(SmashMobs.PREDATOR_MISSILE.get(), context -> 
            new software.bernie.geckolib.renderer.GeoEntityRenderer<>(context, 
                new software.bernie.geckolib.model.DefaultedEntityGeoModel<>(net.minecraft.resources.Identifier.parse("smashmobs:predator_missle")))
        );

        event.registerEntityRenderer(SmashMobs.AIRSTRIKE_JET.get(), context -> 
            new software.bernie.geckolib.renderer.GeoEntityRenderer<>(context, 
                new software.bernie.geckolib.model.DefaultedEntityGeoModel<>(net.minecraft.resources.Identifier.parse("smashmobs:airstrike_jet")))
        );

        event.registerEntityRenderer(SmashMobs.SENTRY_GUN.get(), context -> 
            new software.bernie.geckolib.renderer.GeoEntityRenderer<>(context, 
                new software.bernie.geckolib.model.DefaultedEntityGeoModel<net.brunodev.smashmobs.entity.SentryGunEntity>(
                    net.minecraft.resources.Identifier.parse("smashmobs:sentry_gun"), "head")) // Defines 'head' as the bone that follows looking direction!
        );
    }
}