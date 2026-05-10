package net.brunodev.smashmobs.client;
 
import net.brunodev.smashmobs.SmashMobs;
import net.brunodev.smashmobs.entity.SkeletonMorph;
import net.brunodev.smashmobs.entity.SentryGunEntity;
import net.minecraft.client.renderer.entity.TntRenderer;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.layer.builtin.ItemInHandGeoLayer;
 
@EventBusSubscriber(modid = SmashMobs.MODID, value = Dist.CLIENT)
public class ClientModEvents {
 
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(SmashMobs.SMASH_TNT.get(), TntRenderer::new);
 
        event.registerEntityRenderer(
                SmashMobs.IRON_GOLEM_MORPH.get(),
                context -> new GeoEntityRenderer<>(context, SmashMobs.IRON_GOLEM_MORPH.get())
        );
 
        event.registerEntityRenderer(
                SmashMobs.GOLEM_TRAIN.get(),
                context -> new GeoEntityRenderer<>(context, SmashMobs.GOLEM_TRAIN.get())
        );
 
        event.registerEntityRenderer(
                SmashMobs.SKELETON_MORPH.get(),
                context -> {
                    var model = new DefaultedEntityGeoModel<SkeletonMorph>(
                        Identifier.parse("smashmobs:skeleton_morph"),
                        "head"
                    );
                    var renderer = new GeoEntityRenderer<>(context, model);
                    return renderer.withRenderLayer(new ItemInHandGeoLayer<>(renderer, "rightItem", "leftItem"));
                }
        );
 
        event.registerEntityRenderer(SmashMobs.BULLET.get(), context -> 
            new GeoEntityRenderer<>(context, 
                new DefaultedEntityGeoModel<>(Identifier.parse("smashmobs:bullet")))
        );
 
        event.registerEntityRenderer(SmashMobs.PREDATOR_MISSILE.get(), context -> 
            new GeoEntityRenderer<>(context, 
                new DefaultedEntityGeoModel<>(Identifier.parse("smashmobs:predator_missle")))
        );
 
        event.registerEntityRenderer(SmashMobs.AIRSTRIKE_JET.get(), context -> 
            new GeoEntityRenderer<>(context, 
                new DefaultedEntityGeoModel<>(Identifier.parse("smashmobs:airstrike_jet")))
        );
 
        event.registerEntityRenderer(SmashMobs.SENTRY_GUN.get(), context -> 
            new GeoEntityRenderer<>(context, 
                new DefaultedEntityGeoModel<SentryGunEntity>(
                    Identifier.parse("smashmobs:sentry_gun"), "head")) // Defines 'head' as the bone that follows looking direction!
        );
    }
}