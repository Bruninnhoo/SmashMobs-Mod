package net.brunodev.smashmobs.client;

import net.brunodev.smashmobs.registration.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

import java.util.UUID;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = "smashmobs", value = Dist.CLIENT)
public class ClientEvents {

    private static final WeakHashMap<UUID, LivingEntity> mobCache = new WeakHashMap<>();

    // =========================================================================
    // RENDERIZAÇÃO E MORPHS
    // =========================================================================
    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        AvatarRenderState avatarState = (AvatarRenderState) event.getRenderState();
        Player targetPlayer = null;

        if (Minecraft.getInstance().level != null) {
            if (avatarState.nameTag != null) {
                for (Player p : Minecraft.getInstance().level.players()) {
                    if (p.getName().getString().equals(avatarState.nameTag.getString())) {
                        targetPlayer = p;
                        break;
                    }
                }
            } else {
                targetPlayer = Minecraft.getInstance().player;
            }
        }

        if (targetPlayer != null) {
            String mobId = targetPlayer.getData(ModAttachments.MORPH_DATA);

            if (mobId != null && !mobId.equals("none")) {
                event.setCanceled(true); // Esconde o jogador original
                renderMobMorph(targetPlayer, mobId, event);
            } else {
                // Se o cara destransformou, garante que limpamos o holograma do mundo
                LivingEntity oldMob = mobCache.remove(targetPlayer.getUUID());
                if (oldMob != null) {
                    oldMob.discard();
                }
            }
        }
    }

    private static void renderMobMorph(Player player, String mobId, RenderPlayerEvent.Pre event) {
        var resource = net.minecraft.resources.Identifier.parse(mobId);
        var optionalHolder = BuiltInRegistries.ENTITY_TYPE.get(resource);

        // --- IRON GOLEM E ESQUELETO (GECKOLIB) ---
        if (mobId.equals("minecraft:iron_golem") || mobId.equals("minecraft:skeleton")) {
            LivingEntity cachedEntity = mobCache.get(player.getUUID());
            
            boolean isGolem = mobId.equals("minecraft:iron_golem");

            if (isGolem && !(cachedEntity instanceof net.brunodev.smashmobs.entity.IronGolemMorph)) {
                if (cachedEntity != null) cachedEntity.discard();
                cachedEntity = new net.brunodev.smashmobs.entity.IronGolemMorph(net.brunodev.smashmobs.SmashMobs.IRON_GOLEM_MORPH.get(), player.level());
                cachedEntity.setId(-Math.abs(player.getUUID().hashCode()));
                cachedEntity.setNoGravity(true);
                if (cachedEntity instanceof net.minecraft.world.entity.Mob mob) mob.setNoAi(true);
                Minecraft.getInstance().level.addEntity(cachedEntity);
                mobCache.put(player.getUUID(), cachedEntity);
            } else if (!isGolem && !(cachedEntity instanceof net.brunodev.smashmobs.entity.SkeletonMorph)) {
                if (cachedEntity != null) cachedEntity.discard();
                cachedEntity = new net.brunodev.smashmobs.entity.SkeletonMorph(net.brunodev.smashmobs.SmashMobs.SKELETON_MORPH.get(), player.level());
                cachedEntity.setId(-Math.abs(player.getUUID().hashCode()));
                cachedEntity.setNoGravity(true);
                if (cachedEntity instanceof net.minecraft.world.entity.Mob mob) mob.setNoAi(true);
                Minecraft.getInstance().level.addEntity(cachedEntity);
                mobCache.put(player.getUUID(), cachedEntity);
            }

            // Sincroniza posições físicas e de câmera
            cachedEntity.setPos(player.getX(), player.getY(), player.getZ());
            cachedEntity.xo = player.xo;
            cachedEntity.yo = player.yo;
            cachedEntity.zo = player.zo;
            cachedEntity.xOld = player.xOld;
            cachedEntity.yOld = player.yOld;
            cachedEntity.zOld = player.zOld;

            cachedEntity.setYRot(player.getYRot());
            cachedEntity.yRotO = player.yRotO;
            cachedEntity.setXRot(player.getXRot());
            cachedEntity.xRotO = player.xRotO;
            cachedEntity.setYHeadRot(player.getYHeadRot());
            cachedEntity.yHeadRotO = player.yHeadRotO;
            cachedEntity.setYBodyRot(player.yBodyRot);
            cachedEntity.yBodyRotO = player.yBodyRotO;
            cachedEntity.tickCount = player.tickCount;

            double dx = player.getX() - player.xo;
            double dz = player.getZ() - player.zo;
            boolean moving = (dx * dx + dz * dz) > 0.0001;

            if (cachedEntity instanceof net.brunodev.smashmobs.entity.IronGolemMorph golem) {
                golem.isPlayerMoving = moving;
            } else if (cachedEntity instanceof net.brunodev.smashmobs.entity.SkeletonMorph skeleton) {
                skeleton.isPlayerMoving = moving;
                skeleton.isHoldingAwp = player.getMainHandItem().is(net.brunodev.smashmobs.SmashMobs.SKELETON_SNIPER.get());
                
                // Faz com que o SkeletonMorph segure fisicamente o item na mão principal!
                skeleton.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, player.getMainHandItem());
            }

            // Descarta e remove se for a sua própria câmera em 1ª pessoa (evita cópias fantasmas!)
            if (player == Minecraft.getInstance().player && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                cachedEntity.discard();
                mobCache.remove(player.getUUID());
                return;
            } else {
                cachedEntity.setInvisible(false);
            }

            return; // O Golem/Esqueleto é desenhado pelo próprio Minecraft agora!
        }

        // --- MOBS VANILLA (Creeper, etc) ---
        // Se trocou do Golem pro Vanilla, limpa o Golem do mundo
        LivingEntity oldEntity = mobCache.get(player.getUUID());
        if (oldEntity instanceof net.brunodev.smashmobs.entity.IronGolemMorph || oldEntity instanceof net.brunodev.smashmobs.entity.SkeletonMorph) {
            oldEntity.discard();
            mobCache.remove(player.getUUID());
        }

        if (optionalHolder.isPresent()) {
            EntityType<?> entityType = optionalHolder.get().value();

            LivingEntity cachedMob = mobCache.computeIfAbsent(player.getUUID(), uuid -> {
                var entity = entityType.create(player.level(), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                return (entity instanceof LivingEntity) ? (LivingEntity) entity : null;
            });

            if (cachedMob != null && !cachedMob.getType().equals(entityType)) {
                var newEntity = entityType.create(player.level(), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                cachedMob = (newEntity instanceof LivingEntity) ? (LivingEntity) newEntity : null;
                mobCache.put(player.getUUID(), cachedMob);
            }

            if (cachedMob != null) {
                cachedMob.setPos(player.getX(), player.getY(), player.getZ());
                cachedMob.xo = player.xo;
                cachedMob.yo = player.yo;
                cachedMob.zo = player.zo;
                cachedMob.xOld = player.xOld;
                cachedMob.yOld = player.yOld;
                cachedMob.zOld = player.zOld;

                cachedMob.setYRot(player.getYRot());
                cachedMob.yRotO = player.yRotO;
                cachedMob.setXRot(player.getXRot());
                cachedMob.xRotO = player.xRotO;
                cachedMob.setYHeadRot(player.getYHeadRot());
                cachedMob.yHeadRotO = player.yHeadRotO;
                cachedMob.setYBodyRot(player.yBodyRot);
                cachedMob.yBodyRotO = player.yBodyRotO;

                cachedMob.tickCount = player.tickCount;
                cachedMob.swingTime = player.swingTime;
                cachedMob.setHealth(player.getHealth());
                cachedMob.hurtTime = player.hurtTime;
                cachedMob.hurtDuration = player.hurtDuration;
                cachedMob.deathTime = player.deathTime;

                var dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
                var genericRenderer = dispatcher.getRenderer(cachedMob);

                if (genericRenderer instanceof net.minecraft.client.renderer.entity.LivingEntityRenderer) {
                    @SuppressWarnings({ "rawtypes", "unchecked" })
                    net.minecraft.client.renderer.entity.LivingEntityRenderer renderer = (net.minecraft.client.renderer.entity.LivingEntityRenderer) genericRenderer;

                    net.minecraft.client.renderer.entity.state.LivingEntityRenderState mobState = (net.minecraft.client.renderer.entity.state.LivingEntityRenderState) renderer
                            .createRenderState();

                    renderer.extractRenderState(cachedMob, mobState, event.getPartialTick());
                    AvatarRenderState playerState = (AvatarRenderState) event.getRenderState();

                    mobState.walkAnimationPos = playerState.walkAnimationPos;
                    mobState.walkAnimationSpeed = playerState.walkAnimationSpeed;

                    renderer.submit(mobState, event.getPoseStack(), event.getSubmitNodeCollector(),
                            Minecraft.getInstance().gameRenderer.getLevelRenderState().cameraRenderState);
                }
            }
        }
    }

    // =========================================================================
    // INPUTS E UTILITÁRIOS
    // =========================================================================
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null)
            return;

        // =========================================================================
        // O CONSERTO DA PRIMEIRA PESSOA E COPIAS (Roda o tempo todo, nunca trava!)
        // =========================================================================
        String mobId = player.getData(ModAttachments.MORPH_DATA);
        if (mc.options.getCameraType().isFirstPerson() || mobId == null || mobId.equals("none")) {
            LivingEntity dummy = mobCache.remove(player.getUUID());
            if (dummy != null) {
                dummy.discard();
            }
        }

        // Menu de Seleção
        if (net.brunodev.smashmobs.client.KeyBindings.MORPH_MENU_KEY.consumeClick()) {
            mc.setScreen(new net.brunodev.smashmobs.client.MorphSelectionScreen());
        }


    }

    // Gatilho oficial do GeckoLib!
    public static void playAnvilAnimation(UUID playerId) {
        LivingEntity entity = mobCache.get(playerId);

        if (entity instanceof net.brunodev.smashmobs.entity.IronGolemMorph dummy) {
            System.out.println("LOG: O Gatilho oficial assumiu o controle!");
            dummy.triggerAnim("action", "throw");
        }
    }
}