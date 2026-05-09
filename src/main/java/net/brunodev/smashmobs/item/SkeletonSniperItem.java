package net.brunodev.smashmobs.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.brunodev.smashmobs.SmashMobs;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class SkeletonSniperItem extends Item implements software.bernie.geckolib.animatable.GeoItem {
    private final software.bernie.geckolib.animatable.instance.AnimatableInstanceCache cache = software.bernie.geckolib.util.GeckoLibUtil.createInstanceCache(this);

    public SkeletonSniperItem(Properties properties) {
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
    public boolean isPerspectiveAware() {
        return true;
    }

    @Override
    public void createGeoRenderer(java.util.function.Consumer<software.bernie.geckolib.animatable.client.GeoRenderProvider> consumer) {
        consumer.accept(new software.bernie.geckolib.animatable.client.GeoRenderProvider() {
            private software.bernie.geckolib.renderer.GeoItemRenderer<net.brunodev.smashmobs.item.SkeletonSniperItem> renderer;

            @Override
            public software.bernie.geckolib.renderer.GeoItemRenderer<net.brunodev.smashmobs.item.SkeletonSniperItem> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new software.bernie.geckolib.renderer.GeoItemRenderer<>(new software.bernie.geckolib.model.DefaultedItemGeoModel<>(net.minecraft.resources.Identifier.parse("smashmobs:awp"))) {
                        @Override
                        public void adjustRenderPose(software.bernie.geckolib.renderer.base.RenderPassInfo<software.bernie.geckolib.renderer.base.GeoRenderState> info) {
                            super.adjustRenderPose(info);
                            net.minecraft.world.item.ItemDisplayContext perspective = info.getGeckolibData(software.bernie.geckolib.constant.DataTickets.ITEM_RENDER_PERSPECTIVE);
                            boolean isFirstPerson = perspective == net.minecraft.world.item.ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || perspective == net.minecraft.world.item.ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
                            

                            info.model().getBone("rightArm").ifPresentOrElse(bone -> {
                                if (bone.frameSnapshot == null) {
                                    bone.frameSnapshot = software.bernie.geckolib.animation.state.BoneSnapshot.create(bone);
                                }
                                bone.frameSnapshot.skipRender(!isFirstPerson);
                                bone.frameSnapshot.skipChildrenRender(!isFirstPerson);
                                if (!isFirstPerson) {
                                    bone.frameSnapshot.setScale(0, 0, 0);
                                } else {
                                    bone.frameSnapshot.setScale(1, 1, 1);
                                }
                            }, () -> {
                                System.out.println("[SMASHMOBS DEBUG] rightArm bone NOT found in model!");
                            });
                            
                            info.model().getBone("leftArm").ifPresentOrElse(bone -> {
                                if (bone.frameSnapshot == null) {
                                    bone.frameSnapshot = software.bernie.geckolib.animation.state.BoneSnapshot.create(bone);
                                }
                                bone.frameSnapshot.skipRender(!isFirstPerson);
                                bone.frameSnapshot.skipChildrenRender(!isFirstPerson);
                                if (!isFirstPerson) {
                                    bone.frameSnapshot.setScale(0, 0, 0);
                                } else {
                                    bone.frameSnapshot.setScale(1, 1, 1);
                                }
                            }, () -> {
                                System.out.println("[SMASHMOBS DEBUG] leftArm bone NOT found in model!");
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
            net.brunodev.smashmobs.entity.BulletEntity bullet = new net.brunodev.smashmobs.entity.BulletEntity(SmashMobs.BULLET.get(), level);
            bullet.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            bullet.setOwner(player);
            
            // High-speed linear ray tracing shooting method from look angle
            net.minecraft.world.phys.Vec3 look = player.getLookAngle();
            bullet.setDeltaMovement(look.scale(4.0));
            bullet.setDamage(12.0);
            
            level.addFreshEntity(bullet);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SKELETON_SHOOT, SoundSource.PLAYERS, 1.5F, 0.5F); 
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SmashMobs.SKELETON_SHOOT_SOUND.get(), SoundSource.PLAYERS, 1.0F, 1.5F);

            player.getCooldowns().addCooldown(itemstack, 80);
        }

        player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }
}
