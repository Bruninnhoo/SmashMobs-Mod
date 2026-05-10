package net.brunodev.smashmobs.item;
 
import net.brunodev.smashmobs.SmashMobs;
import net.brunodev.smashmobs.entity.BulletEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.object.PlayState;
import software.bernie.geckolib.animation.state.BoneSnapshot;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.RenderPassInfo;
import software.bernie.geckolib.util.GeckoLibUtil;
 
import java.util.function.Consumer;
 
public class SkeletonSniperItem extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
 
    public SkeletonSniperItem(Properties properties) {
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
    public boolean isPerspectiveAware() {
        return true;
    }
 
    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private GeoItemRenderer<SkeletonSniperItem> renderer;
 
            @Override
            public GeoItemRenderer<SkeletonSniperItem> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new GeoItemRenderer<>(new DefaultedItemGeoModel<>(Identifier.parse("smashmobs:awp"))) {
                        @Override
                        public void adjustRenderPose(RenderPassInfo<GeoRenderState> info) {
                            super.adjustRenderPose(info);
                            ItemDisplayContext perspective = info.getGeckolibData(DataTickets.ITEM_RENDER_PERSPECTIVE);
                            boolean isFirstPerson = perspective == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || perspective == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
                            
                            info.model().getBone("rightArm").ifPresentOrElse(bone -> {
                                if (bone.frameSnapshot == null) {
                                    bone.frameSnapshot = BoneSnapshot.create(bone);
                                }
                                bone.frameSnapshot.skipRender(!isFirstPerson);
                                bone.frameSnapshot.skipChildrenRender(!isFirstPerson);
                                if (!isFirstPerson) {
                                    bone.frameSnapshot.setScale(0, 0, 0);
                                } else {
                                    bone.frameSnapshot.setScale(1, 1, 1);
                                }
                            }, () -> {
                                // bone not found
                            });
                            
                            info.model().getBone("leftArm").ifPresentOrElse(bone -> {
                                if (bone.frameSnapshot == null) {
                                    bone.frameSnapshot = BoneSnapshot.create(bone);
                                }
                                bone.frameSnapshot.skipRender(!isFirstPerson);
                                bone.frameSnapshot.skipChildrenRender(!isFirstPerson);
                                if (!isFirstPerson) {
                                    bone.frameSnapshot.setScale(0, 0, 0);
                                } else {
                                    bone.frameSnapshot.setScale(1, 1, 1);
                                }
                            }, () -> {
                                // bone not found
                            });
                        }
                    };
                }
                return this.renderer;
            }
        });
    }
 
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
 
        if (!level.isClientSide()) {
            BulletEntity bullet = new BulletEntity(SmashMobs.BULLET.get(), level);
            bullet.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            bullet.setOwner(player);
            
            Vec3 look = player.getLookAngle();
            bullet.setDeltaMovement(look.scale(4.0));
            bullet.setDamage(12.0);
            
            level.addFreshEntity(bullet);
 
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SKELETON_SHOOT, SoundSource.PLAYERS, 1.5F, 0.5F); 
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SmashMobs.SKELETON_SHOOT_SOUND.get(), SoundSource.PLAYERS, 1.0F, 1.5F);
 
            player.getCooldowns().addCooldown(itemstack, 80);
        }
 
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }
}
