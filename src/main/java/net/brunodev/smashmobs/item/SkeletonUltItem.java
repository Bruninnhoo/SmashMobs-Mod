package net.brunodev.smashmobs.item;

import net.brunodev.smashmobs.server.AbilityEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SkeletonUltItem extends Item implements software.bernie.geckolib.animatable.GeoItem {
    private final software.bernie.geckolib.animatable.instance.AnimatableInstanceCache cache = software.bernie.geckolib.util.GeckoLibUtil.createInstanceCache(this);

    public SkeletonUltItem(Properties properties) {
        super(properties);
    }

    @Override
    public void registerControllers(software.bernie.geckolib.animatable.manager.AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new software.bernie.geckolib.animation.AnimationController<>("controller", 2, event -> software.bernie.geckolib.animation.object.PlayState.CONTINUE));
    }

    @Override
    public software.bernie.geckolib.animatable.instance.AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void createGeoRenderer(java.util.function.Consumer<software.bernie.geckolib.animatable.client.GeoRenderProvider> consumer) {
        consumer.accept(new software.bernie.geckolib.animatable.client.GeoRenderProvider() {
            private software.bernie.geckolib.renderer.GeoItemRenderer<net.brunodev.smashmobs.item.SkeletonUltItem> renderer;

            @Override
            public software.bernie.geckolib.renderer.GeoItemRenderer<net.brunodev.smashmobs.item.SkeletonUltItem> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new software.bernie.geckolib.renderer.GeoItemRenderer<>(
                        new software.bernie.geckolib.model.DefaultedItemGeoModel<>(net.minecraft.resources.Identifier.parse("smashmobs:notebook"))
                    );
                }
                return this.renderer;
            }
        });
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            String streak = AbilityEvents.SKELETON_KILLSTREAK.getOrDefault(player.getUUID(), "arrow_storm");
            
            if ("arrow_storm".equals(streak)) {
                // Para fase de testes, escolhe aleatoriamente um dos 3 COD MW2 Ultimates!
                String[] testStreaks = {"air_strike", "sentry_gun", "predator_missile"};
                streak = testStreaks[new java.util.Random().nextInt(testStreaks.length)];
            } 
            
            if ("air_strike".equals(streak)) {
                AbilityEvents.startAirStrike(player);
            } 
            else if ("sentry_gun".equals(streak)) {
                AbilityEvents.spawnSentryGun(player);
            } 
            else if ("predator_missile".equals(streak)) {
                AbilityEvents.shootPredatorMissile(player);
            }

            // Consome a killstreak e volta para a padrão (Arrow Storm)
            AbilityEvents.SKELETON_KILLSTREAK.put(player.getUUID(), "arrow_storm");

            // Cooldown de 15 segundos (300 ticks) para poder usar a padrão ou outra ganha
            player.getCooldowns().addCooldown(player.getItemInHand(hand), 300);
        }
        return InteractionResult.SUCCESS;
    }
}
