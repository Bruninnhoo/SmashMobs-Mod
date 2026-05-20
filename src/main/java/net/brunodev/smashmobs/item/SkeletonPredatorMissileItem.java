package net.brunodev.smashmobs.item;

import net.brunodev.smashmobs.server.AbilityEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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

import java.util.function.Consumer;

public class SkeletonPredatorMissileItem extends SmashMobItemBase implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public SkeletonPredatorMissileItem(Properties properties) {
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
            private GeoItemRenderer<SkeletonPredatorMissileItem> renderer;

            @Override
            public GeoItemRenderer<SkeletonPredatorMissileItem> getGeoItemRenderer() {
                if (this.renderer == null) {
                    var customModel = new DefaultedItemGeoModel<SkeletonPredatorMissileItem>(Identifier.parse("smashmobs:notebook")) {
                        @Override
                        public Identifier getTextureResource(software.bernie.geckolib.renderer.base.GeoRenderState renderState) {
                            return Identifier.parse("smashmobs:textures/item/notebook.png");
                        }
                    };
                    this.renderer = new GeoItemRenderer<>(customModel);
                }
                return this.renderer;
            }
        });
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            AbilityEvents.shootPredatorMissile(player);
            stack.consume(1, player);
            player.getCooldowns().addCooldown(stack, 60);
        }
        return InteractionResult.SUCCESS;
    }
}

