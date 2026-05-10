package net.brunodev.smashmobs.item;
 
import net.brunodev.smashmobs.server.AbilityEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.object.PlayState;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;
 
import java.util.Random;
import java.util.function.Consumer;
 
public class SkeletonUltItem extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
 
    public SkeletonUltItem(Properties properties) {
        super(properties);
    }
 
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("controller", 2, event -> PlayState.CONTINUE));
    }
 
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
 
    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private GeoItemRenderer<SkeletonUltItem> renderer;
 
            @Override
            public GeoItemRenderer<SkeletonUltItem> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new GeoItemRenderer<>(
                        new DefaultedItemGeoModel<>(Identifier.parse("smashmobs:notebook"))
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
                String[] testStreaks = {"air_strike", "sentry_gun", "predator_missile"};
                streak = testStreaks[new Random().nextInt(testStreaks.length)];
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
 
            AbilityEvents.SKELETON_KILLSTREAK.put(player.getUUID(), "arrow_storm");
            player.getCooldowns().addCooldown(player.getItemInHand(hand), 300);
        }
        return InteractionResult.SUCCESS;
    }
}
