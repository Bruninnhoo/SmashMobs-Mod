package net.brunodev.smashmobs.server;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.*;

import net.brunodev.smashmobs.SmashMobs;
import net.brunodev.smashmobs.entity.*;
import net.brunodev.smashmobs.registration.ModAttachments;

import static net.brunodev.smashmobs.SmashMobs.GOLEM_SUPREME_SOUND;
import static net.brunodev.smashmobs.SmashMobs.GOLEM_THROW_ANVIL_SOUND;

@EventBusSubscriber(modid = "smashmobs")
public class AbilityEvents {

    // ====================================
    // -------------- IRON GOLEM -----------
    // ====================================
    public static final Map<UUID, Integer> PENDING_ANVILS = new HashMap<>();
    public static final Map<FallingBlockEntity, UUID> FLYING_ANVILS = new HashMap<>();

    // Variáveis do Puxão e Agarrão
    public static final Map<UUID, UUID> GRABBED_ENTITIES = new HashMap<>();
    public static final Map<UUID, Integer> GRAB_TIMERS = new HashMap<>();
    public static final Map<UUID, UUID> PULLING_ENTITIES = new HashMap<>();
    public static final Map<UUID, Integer> PENDING_TRAINS = new HashMap<>();

    // CLASSE DO SKILLSHOT (O Gancho Fantasma)
    public static class GolemHook {
        public Player owner;
        public Vec3 pos;
        public Vec3 direction;
        public double distance;

        public GolemHook(Player o, Vec3 p, Vec3 d) {
            owner = o;
            pos = p;
            direction = d;
            distance = 0;
        }
    }

    public static final List<GolemHook> FLYING_HOOKS = new ArrayList<>();

    // ====================================
    // -------------- CREEPER --------------
    // ====================================
    public static final Set<UUID> CREEPER_ARMED_PLAYERS = new HashSet<>();
    public static final java.util.Map<UUID, Integer> CREEPER_SUPREMES = new java.util.HashMap<>();

    // ====================================
    // -------------- GOAT ----------------
    // ====================================
    public static final Map<UUID, Integer> DASHING_GOATS = new HashMap<>();
    public static final Map<UUID, UUID> GOAT_SWALLOWED = new HashMap<>();
    public static final Map<UUID, Integer> GOAT_SWALLOWED_TIMERS = new HashMap<>();
    public static final Map<UUID, Integer> GOAT_STOLEN_TIMERS = new HashMap<>();
    public static final Map<UUID, Integer> GOAT_AVALANCHES = new HashMap<>();

    // ====================================
    // ------------ CHICKEN ---------------
    // ====================================
    public static final Map<ItemEntity, UUID> CHICKEN_MINES = new HashMap<>();
    public static final Map<UUID, Integer> CHICKEN_KNOCKBACK_VULNERABILITY = new HashMap<>();
    public static final Map<UUID, Integer> CHICKEN_BOMBERS = new HashMap<>();
    public static final Set<Projectile> CHICKEN_BOMBER_EGGS = new HashSet<>();
    public static final Map<UUID, Integer> CHICKEN_BOMBER_COOLDOWN = new HashMap<>();
    public static final Map<UUID, Integer> CHICKEN_MACHINE_GUN_ACTIVE = new HashMap<>();

    // ====================================
    // ----------- SKELETON ---------------
    // ====================================
    public static class BoomerangBone {
        public Player owner;
        public Vec3 pos;
        public Vec3 direction;
        public int ticksAlive = 0;
        public boolean returning = false;

        public BoomerangBone(Player o, Vec3 p, Vec3 d) {
            owner = o;
            pos = p;
            direction = d;
        }
    }

    public static final List<BoomerangBone> FLYING_BONES = new ArrayList<>();

    public static class ArrowStorm {
        public Player owner;
        public Vec3 pos;
        public int ticksLeft;

        public ArrowStorm(Player o, Vec3 p, int ticks) {
            owner = o;
            pos = p;
            ticksLeft = ticks;
        }
    }

    public static final List<ArrowStorm> ARROW_STORMS = new ArrayList<>();

    // COD MW2 KILLSTREAK SYSTEM FOR SKELETON
    public static final Map<UUID, String> SKELETON_KILLSTREAK = new HashMap<>();

    public static void giveRandomKillstreak(Player player) {
        String[] streaks = { "air_strike", "sentry_gun", "predator_missile" };
        String chosen = streaks[new Random().nextInt(streaks.length)];
        SKELETON_KILLSTREAK.put(player.getUUID(), chosen);

        String displayName = "";
        if ("air_strike".equals(chosen))
            displayName = "§c§lAIRSTRIKE";
        else if ("sentry_gun".equals(chosen))
            displayName = "§e§lSENTRY GUN";
        else if ("predator_missile".equals(chosen))
            displayName = "§b§lPREDATOR MISSILE";

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(
                    Component.literal("§6§l[KILLSTREAK] §aVocê ganhou a Ultimate: " + displayName + " §a!"));
        }
        player.level().playSound(null, player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS,
                1.0F, 1.5F);
    }

    public static void startAirStrike(Player player) {
        Level level = player.level();
        // Spawn highly above
        Vec3 startPos = player.position().add(0, 30, 0).subtract(player.getLookAngle().scale(20.0));
        Vec3 dir = new Vec3(player.getLookAngle().x, 0, player.getLookAngle().z).normalize();

        AirstrikeJetEntity jet = new AirstrikeJetEntity(SmashMobs.AIRSTRIKE_JET.get(), level);
        jet.setPos(startPos);
        jet.setOwner(player);
        jet.setDeltaMovement(dir.scale(1.5)); // Gliding speed
        level.addFreshEntity(jet);

        player.level().playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS,
                2.0F, 0.5F);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.literal("§c§l[KILLSTREAK] §fAirstrike inbound!"));
        }
    }

    public static void spawnSentryGun(Player player) {
        Level level = player.level();
        SentryGunEntity sentry = new SentryGunEntity(SmashMobs.SENTRY_GUN.get(), level);
        sentry.setPos(player.getX(), player.getY(), player.getZ());
        sentry.setOwner(player);

        level.addFreshEntity(sentry);

        level.playSound(null, player.blockPosition(), SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.literal("§e§l[KILLSTREAK] §fSentry Gun implantada!"));
        }
    }

    public static void shootPredatorMissile(Player player) {
        Level level = player.level();
        // Raycast to find target ground
        HitResult hr = player.pick(60.0, 0.0F, false);
        Vec3 targetPos = hr.getLocation();

        // Spawn high above and offset diagonally
        Vec3 spawnPos = targetPos.add(20, 35, 20);

        PredatorMissileEntity missile = new PredatorMissileEntity(SmashMobs.PREDATOR_MISSILE.get(), level);
        missile.setPos(spawnPos);
        
        // Calcula a direcao diagonal ate o alvo
        Vec3 direction = targetPos.subtract(spawnPos).normalize();
        missile.setDeltaMovement(direction.scale(2.5)); // Mergulho diagonal rapido
        missile.setOwner(player);
        level.addFreshEntity(missile);

        level.playSound(null, player.blockPosition(), SoundEvents.GHAST_SHOOT, SoundSource.PLAYERS, 2.0F, 0.5F);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.literal("§b§l[KILLSTREAK] §fPredator Missile lançado!"));
        }
    }

    public static void spitSwallowedEntity(Player goat) {
        UUID goatId = goat.getUUID();
        if (!GOAT_SWALLOWED.containsKey(goatId))
            return;

        if (goat.level() instanceof ServerLevel sl) {
            Entity targetEntity = sl.getEntity(GOAT_SWALLOWED.get(goatId));

            if (targetEntity instanceof LivingEntity victim && victim.isAlive()) {
                // Teleporta de volta para a cabra antes de cuspir
                victim.teleportTo(goat.getX(), goat.getY() + 0.5, goat.getZ());

                var look = goat.getLookAngle();
                victim.setDeltaMovement(look.x * 2.0, 0.8, look.z * 2.0);
                victim.hurt(victim.damageSources().mobAttack(goat), 6.0F);
                victim.hurtMarked = true;

                victim.removeEffect(MobEffects.INVISIBILITY);
                victim.removeEffect(MobEffects.BLINDNESS);
                victim.removeEffect(MobEffects.LEVITATION);

                goat.level().playSound(null, goat.blockPosition(), SoundEvents.LLAMA_SPIT,
                        SoundSource.PLAYERS, 2.0F, 1.0F);

                if (victim instanceof Player victimPlayer) {
                    String morph = victimPlayer.getData(ModAttachments.MORPH_DATA);
                    Item stolenItem = null;
                    if (morph.equals("minecraft:creeper"))
                        stolenItem = SmashMobs.CREEPER_EXPLOSION.get();
                    else if (morph.equals("minecraft:iron_golem"))
                        stolenItem = SmashMobs.GOLEM_THROW_ANVIL.get();
                    else if (morph.equals("minecraft:goat"))
                        stolenItem = SmashMobs.GOAT_DASH.get();
                    else if (morph.equals("minecraft:chicken"))
                        stolenItem = SmashMobs.CHICKEN_MACHINE_GUN.get();
                    else if (morph.equals("minecraft:skeleton")) {
                        Item[] skeletonPool = {
                            SmashMobs.SKELETON_PREDATOR_MISSILE.get(),
                            SmashMobs.SKELETON_AIRSTRIKE.get(),
                            SmashMobs.SKELETON_SENTRY.get()
                        };
                        stolenItem = skeletonPool[new java.util.Random().nextInt(skeletonPool.length)];
                    }

                    if (stolenItem != null && goat.getInventory().contains(new ItemStack(
                            SmashMobs.GOAT_SWALLOW.get()))) {
                        goat.getInventory().setItem(2, new ItemStack(stolenItem));
                        GOAT_STOLEN_TIMERS.put(goatId, 300); // 15 Segundos
                    }
                }
            }
        }

        GOAT_SWALLOWED.remove(goatId);
        GOAT_SWALLOWED_TIMERS.remove(goatId);
        goat.getCooldowns().addCooldown(
                new ItemStack(SmashMobs.GOAT_SWALLOW.get()), 100);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {

        // 1. O RADAR DE COLISÃO DA BIGORNA
        var anvilIterator = FLYING_ANVILS.entrySet().iterator();
        while (anvilIterator.hasNext()) {
            var entry = anvilIterator.next();
            FallingBlockEntity anvil = entry.getKey();
            UUID throwerId = entry.getValue();

            if (!anvil.isAlive() || anvil.onGround()) {
                anvilIterator.remove();
                continue;
            }

            var hitBox = anvil.getBoundingBox().inflate(0.3);
            var targets = anvil.level().getEntitiesOfClass(LivingEntity.class, hitBox,
                    e -> !e.getUUID().equals(throwerId));

            if (!targets.isEmpty()) {
                LivingEntity target = targets.get(0);
                target.hurt(target.damageSources().anvil(anvil), 12.0F);

                double dx = target.getX() - anvil.getX();
                double dz = target.getZ() - anvil.getZ();
                target.knockback(1.5, -dx, -dz);

                anvil.level().playSound(null, anvil.blockPosition(), GOLEM_THROW_ANVIL_SOUND.get(),
                        SoundSource.PLAYERS, 1.0F, 1.0F);
                anvil.level().playSound(null, anvil.blockPosition(), SoundEvents.IRON_GOLEM_DAMAGE,
                        SoundSource.PLAYERS, 1.0F, 0.5F);

                anvil.discard();
                anvilIterator.remove();
            }
        }
        // 2. RADAR DE MINAS DA GALINHA
        var mineIterator = CHICKEN_MINES.entrySet().iterator();
        while (mineIterator.hasNext()) {
            var entry = mineIterator.next();
            net.minecraft.world.entity.item.ItemEntity eggMine = entry.getKey();
            UUID ownerId = entry.getValue();

            if (!eggMine.isAlive()) {
                mineIterator.remove();
                continue;
            }

            var hitBox = eggMine.getBoundingBox().inflate(1.5);
            var targets = eggMine.level().getEntitiesOfClass(LivingEntity.class, hitBox,
                    e -> !e.getUUID().equals(ownerId) && e.isAlive());

            if (!targets.isEmpty()) {
                LivingEntity victim = targets.get(0);

                double dx = victim.getX() - eggMine.getX();
                double dz = victim.getZ() - eggMine.getZ();
                victim.knockback(1.8, -dx, -dz);
                victim.hurt(victim.damageSources().magic(), 2.0F);

                eggMine.level().playSound(null, eggMine.blockPosition(), SoundEvents.CHICKEN_EGG,
                        SoundSource.PLAYERS, 2.0F, 0.5F);
                eggMine.level().playSound(null, eggMine.blockPosition(),
                        SoundEvents.GENERIC_EXPLODE.value(),
                        SoundSource.PLAYERS, 0.8F, 1.5F);

                if (eggMine.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.EXPLOSION, eggMine.getX(),
                            eggMine.getY(), eggMine.getZ(), 2, 0.2, 0.2, 0.2, 0.0);
                }

                CHICKEN_KNOCKBACK_VULNERABILITY.put(victim.getUUID(), 100);

                eggMine.discard();
                mineIterator.remove();
                continue;
            }
        }

        // 3. O RADAR DO SKILLSHOT (O Puxão do Blitzcrank)
        var hookIterator = FLYING_HOOKS.iterator();
        while (hookIterator.hasNext()) {
            GolemHook hook = hookIterator.next();

            // Se o Golem desconectou ou morreu, apaga o gancho
            if (!hook.owner.isAlive()) {
                hookIterator.remove();
                continue;
            }

            double hookSpeed = 1.2; // Velocidade do tiro (1.2 blocos por tick)
            Vec3 nextPos = hook.pos.add(hook.direction.scale(hookSpeed));

            // Verifica se o gancho bateu numa parede!
            var clipResult = hook.owner.level().clip(new ClipContext(
                    hook.pos, nextPos, ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE, hook.owner));

            if (clipResult.getType() == HitResult.Type.BLOCK) {
                // Errou! Bateu na parede. Toca um som de ferro batendo em pedra e apaga.
                hook.owner.level().playSound(null, BlockPos.containing(clipResult.getLocation()),
                        SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.5F,
                        2.0F);
                hookIterator.remove();
                continue;
            }

            // Move o gancho pra frente
            hook.pos = nextPos;
            hook.distance += hookSpeed;

            // RASTRO VISUAL: Desenha partículas pra todo mundo ver o gancho voando!
            if (hook.owner.level() instanceof ServerLevel sl) {
                // Fumaça larga e partícula de crítico pra simular um soco de ar
                sl.sendParticles(ParticleTypes.LARGE_SMOKE, hook.pos.x, hook.pos.y,
                        hook.pos.z, 2, 0.1, 0.1, 0.1, 0.0);
                sl.sendParticles(ParticleTypes.CRIT, hook.pos.x, hook.pos.y, hook.pos.z, 1,
                        0.0, 0.0, 0.0, 0.0);
            }

            // Verifica se a "ponta" do gancho encostou em alguém
            var hitBox = new AABB(hook.pos.x - 0.5, hook.pos.y - 0.5, hook.pos.z - 0.5,
                    hook.pos.x + 0.5, hook.pos.y + 0.5, hook.pos.z + 0.5);
            var targets = hook.owner.level().getEntitiesOfClass(LivingEntity.class, hitBox,
                    e -> e != hook.owner && e.isAlive());

            if (!targets.isEmpty()) {
                // ACERTOU O SKILLSHOT!
                LivingEntity hitTarget = targets.get(0);
                PULLING_ENTITIES.put(hook.owner.getUUID(), hitTarget.getUUID());

                // Som de agarrão de metal
                hook.owner.level().playSound(null, hitTarget.blockPosition(),
                        SoundEvents.IRON_GOLEM_REPAIR, SoundSource.PLAYERS,
                        1.0F, 1.5F);
                hookIterator.remove(); // O gancho some e o alvo começa a ser puxado
                continue;
            }

            // Se o gancho viajou mais de 12 blocos e não pegou nada, ele some no ar
            if (hook.distance >= 12.0) {
                hookIterator.remove();
            }
        }

        // 4. RADAR DO BOOMERANG (ESQUELETO)
        var boneIterator = FLYING_BONES.iterator();
        while (boneIterator.hasNext()) {
            BoomerangBone bone = boneIterator.next();
            if (!bone.owner.isAlive()) {
                boneIterator.remove();
                continue;
            }

            bone.ticksAlive++;
            double speed = 1.0;

            if (bone.ticksAlive > 15) {
                bone.returning = true;
            }

            if (bone.returning) {
                // Direciona o osso de volta pro dono
                Vec3 toOwner = bone.owner.getEyePosition().subtract(bone.pos).normalize();
                bone.direction = toOwner;
                speed = 1.2; // Volta mais rápido
            }

            Vec3 nextPos = bone.pos.add(bone.direction.scale(speed));

            // Verifica se voltou pro dono
            if (bone.returning && bone.pos.distanceTo(bone.owner.getEyePosition()) < 1.5) {
                bone.owner.level().playSound(null, bone.owner.blockPosition(), SoundEvents.ITEM_PICKUP,
                        SoundSource.PLAYERS, 0.5F, 2.0F);
                boneIterator.remove();
                continue;
            }

            // RASTRO VISUAL
            if (bone.owner.level() instanceof ServerLevel sl) {
                sl.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.BONE)),
                        bone.pos.x, bone.pos.y, bone.pos.z, 2, 0.1, 0.1, 0.1, 0.0);
            }

            var hitBox = new AABB(bone.pos.x - 0.5, bone.pos.y - 0.5, bone.pos.z - 0.5,
                    bone.pos.x + 0.5, bone.pos.y + 0.5, bone.pos.z + 0.5);
            var targets = bone.owner.level().getEntitiesOfClass(LivingEntity.class, hitBox,
                    e -> e != bone.owner && e.isAlive());

            if (!targets.isEmpty()) {
                LivingEntity hitTarget = targets.get(0);
                // Evita que hite o mesmo alvo 1000 vezes por tick se estiver parado nele
                if (bone.ticksAlive % 5 == 0) {
                    hitTarget.hurt(hitTarget.damageSources().mobAttack(bone.owner), 4.0F);
                    hitTarget.knockback(0.5, bone.owner.getX() - hitTarget.getX(),
                            bone.owner.getZ() - hitTarget.getZ());
                    bone.owner.level().playSound(null, hitTarget.blockPosition(), SoundEvents.SKELETON_HURT,
                            SoundSource.PLAYERS, 1.0F, 1.5F);
                }
            }

            bone.pos = nextPos;
        }

        // 5. RADAR DA CHUVA DE FLECHAS (ULTIMATE ESQUELETO)
        var stormIterator = ARROW_STORMS.iterator();
        while (stormIterator.hasNext()) {
            ArrowStorm storm = stormIterator.next();
            if (!storm.owner.isAlive() || storm.ticksLeft <= 0) {
                stormIterator.remove();
                continue;
            }

            storm.ticksLeft--;

            if (storm.ticksLeft % 3 == 0) { // Chove a cada 3 ticks
                if (storm.owner.level() instanceof ServerLevel sl) {
                    double offsetX = (Math.random() - 0.5) * 8.0;
                    double offsetZ = (Math.random() - 0.5) * 8.0;

                    Projectile arrow = (Projectile) EntityType.ARROW.create(sl, EntitySpawnReason.COMMAND);
                    if (arrow != null) {
                        arrow.setPos(storm.pos.x + offsetX, storm.pos.y + 12.0, storm.pos.z + offsetZ);
                        arrow.setDeltaMovement(0, -1.5, 0);
                        // We just rely on standard projectile methods
                        arrow.setOwner(storm.owner);
                        sl.addFreshEntity(arrow);
                    }
                }
            }

            // Efeitos de som
            if (storm.ticksLeft % 10 == 0) {
                storm.owner.level().playSound(null, BlockPos.containing(storm.pos),
                        SoundEvents.SKELETON_SHOOT, SoundSource.PLAYERS, 0.5F, 0.5F);
            }
        }
    }

    // ========================================================
    // AÇÃO DE CLIQUE: PUXÃO E ARREMESSO DO GOLEM
    // ========================================================
    public static void handleGolemGrab(Player golemPlayer) {
        UUID golemId = golemPlayer.getUUID();
        var level = golemPlayer.level();

        // CENA 1: ARREMESSAR (Se já estiver segurando alguém)
        if (GRABBED_ENTITIES.containsKey(golemId)) {
            UUID targetId = GRABBED_ENTITIES.get(golemId);

            if (level instanceof ServerLevel serverLevel) {
                Entity target = serverLevel.getEntity(targetId);

                if (target instanceof LivingEntity livingTarget) {
                    var look = golemPlayer.getLookAngle();
                    livingTarget.setDeltaMovement(look.scale(2.5).add(0, 0.8, 0));
                    livingTarget.hurt(livingTarget.damageSources().mobAttack(golemPlayer), 8.0F);
                    livingTarget.hurtMarked = true;
                    level.playSound(null, golemPlayer.blockPosition(),
                            SoundEvents.IRON_GOLEM_ATTACK,
                            SoundSource.PLAYERS, 1.0F, 0.8F);
                }
            }
            GRABBED_ENTITIES.remove(golemId);
            GRAB_TIMERS.remove(golemId);
            golemPlayer.getCooldowns()
                    .addCooldown(golemPlayer.getItemInHand(InteractionHand.MAIN_HAND), 100);

        }
        // CENA 2: ATIRAR O GANCHO! (Skillshot)
        else if (!PULLING_ENTITIES.containsKey(golemId)) {

            // Verifica se o jogador já não atirou um gancho que ainda está voando
            boolean alreadyHooking = FLYING_HOOKS.stream().anyMatch(h -> h.owner.getUUID().equals(golemId));

            if (!alreadyHooking) {
                Vec3 eyePos = golemPlayer.getEyePosition();
                Vec3 dir = golemPlayer.getLookAngle();

                // Cria o gancho e coloca na pista!
                FLYING_HOOKS.add(new GolemHook(golemPlayer, eyePos, dir));

                // Adiciona COOLDOWN DE LANÇAMENTO para impedir spam infinito se errar o gancho!
                golemPlayer.getCooldowns().addCooldown(golemPlayer.getItemInHand(InteractionHand.MAIN_HAND), 60); // 3 Segundos

                // Som de lançamento no ar
                level.playSound(null, golemPlayer.blockPosition(),
                        SoundEvents.FISHING_BOBBER_THROW, SoundSource.PLAYERS,
                        1.5F, 0.5F);

                // TODO: Chamar o pacote de rede para esticar o braço visualmente
            }
        }
    }

    // ========================================================
    // TICK DO JOGADOR
    // ========================================================
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide())
            return;

        if (PENDING_TRAINS.containsKey(player.getUUID())) {
            int ticksLeft = PENDING_TRAINS.get(player.getUUID());

            if (ticksLeft > 0) {
                PENDING_TRAINS.put(player.getUUID(), ticksLeft - 1);
            } else {
                // O AVISO ACABOU! INVOCA O TREM NAS COSTAS DO JOGADOR!
                PENDING_TRAINS.remove(player.getUUID());
                spawnTrainUltimate(player);
            }
        }

        // --- LÓGICA DA BIGORNA AGENDADA ---
        if (PENDING_ANVILS.containsKey(player.getUUID())) {
            int ticksLeft = PENDING_ANVILS.get(player.getUUID());

            if (ticksLeft > 0) {
                PENDING_ANVILS.put(player.getUUID(), ticksLeft - 1);
            } else {
                PENDING_ANVILS.remove(player.getUUID());
                spawnAnvilProjectile(player);
            }
        }

        // --- LÓGICA DO PUXÃO (TRAZENDO DE LONGE) ---
        if (PULLING_ENTITIES.containsKey(player.getUUID())) {
            if (player.level() instanceof ServerLevel serverLevel) {
                Entity target = serverLevel
                        .getEntity(PULLING_ENTITIES.get(player.getUUID()));

                if (target instanceof LivingEntity livingTarget && livingTarget.isAlive()) {
                    double dist = livingTarget.distanceTo(player);

                    if (dist > 1.8) {
                        var pullVector = player.position().subtract(livingTarget.position()).normalize();
                        livingTarget.setDeltaMovement(pullVector.x * 1.2, pullVector.y * 1.2 + 0.2, pullVector.z * 1.2);
                        livingTarget.hurtMarked = true;
                    } else {
                        PULLING_ENTITIES.remove(player.getUUID());
                        GRABBED_ENTITIES.put(player.getUUID(), livingTarget.getUUID());
                        GRAB_TIMERS.put(player.getUUID(), 60);
                        player.level().playSound(null, player.blockPosition(),
                                SoundEvents.IRON_GOLEM_REPAIR,
                                SoundSource.PLAYERS, 1.0F, 2.0F);
                    }
                } else {
                    PULLING_ENTITIES.remove(player.getUUID());
                }
            }
        }

        // --- LÓGICA DO GRAB FIXO (MANTÉM NA MÃO) ---
        if (GRABBED_ENTITIES.containsKey(player.getUUID())) {
            int timer = GRAB_TIMERS.getOrDefault(player.getUUID(), 0);

            if (player.level() instanceof ServerLevel serverLevel) {
                Entity target = serverLevel
                        .getEntity(GRABBED_ENTITIES.get(player.getUUID()));

                if (target instanceof LivingEntity livingTarget && livingTarget.isAlive() && timer > 0) {
                    var look = player.getLookAngle();
                    double holdX = player.getX() + (look.x * 1.5);
                    double holdY = player.getY() + 1.2;
                    double holdZ = player.getZ() + (look.z * 1.5);

                    livingTarget.teleportTo(holdX, holdY, holdZ);
                    livingTarget.setDeltaMovement(0, 0, 0);
                    livingTarget.fallDistance = 0;
                    livingTarget.hurtMarked = true;

                    GRAB_TIMERS.put(player.getUUID(), timer - 1);
                } else {
                    GRABBED_ENTITIES.remove(player.getUUID());
                    GRAB_TIMERS.remove(player.getUUID());
                    player.getCooldowns()
                            .addCooldown(player.getItemInHand(InteractionHand.MAIN_HAND), 100);
                }
            }
        }

        // --- CREEPER SUPREME LÓGICA ---
        if (CREEPER_SUPREMES.containsKey(player.getUUID())) {
            int ticksLeft = CREEPER_SUPREMES.get(player.getUUID());

            if (ticksLeft > 0) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.SLOWNESS, 2, 255, false, false, false));

                double radius = 8.0;
                var area = player.getBoundingBox().inflate(radius);
                var targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                        e -> e != player);

                for (var target : targets) {
                    double dx = player.getX() - target.getX();
                    double dy = player.getY() - target.getY();
                    double dz = player.getZ() - target.getZ();

                    Vec3 pull = new Vec3(dx, dy, dz).normalize()
                            .scale(0.08);

                    target.setDeltaMovement(target.getDeltaMovement().add(pull));
                    target.hurtMarked = true;
                }

                if (ticksLeft % 5 == 0) {
                    player.level().playSound(null, player.blockPosition(),
                            SoundEvents.CREEPER_PRIMED, SoundSource.PLAYERS,
                            1.0F, 0.5F);
                }

                CREEPER_SUPREMES.put(player.getUUID(), ticksLeft - 1);
            } else {
                CREEPER_SUPREMES.remove(player.getUUID());
                player.level().explode(player, player.getX(), player.getY() + 1, player.getZ(), 6.0F, false,
                        Level.ExplosionInteraction.NONE);
            }
        }

        // --- LÓGICA DA CABEÇADA (GOAT DASH) ---
        if (DASHING_GOATS.containsKey(player.getUUID())) {
            int ticksLeft = DASHING_GOATS.get(player.getUUID());

            if (ticksLeft > 0) {
                var look = player.getLookAngle();

                // 1. O IMPULSO: Joga o jogador para frente muito rápido!
                // Ignoramos a altura (Y) para o dash ser retinho no chão, mas você pode pular e
                // dar o dash no ar!
                player.setDeltaMovement(look.x * 1.8, player.getDeltaMovement().y, look.z * 1.8);
                player.hurtMarked = true;

                // Partículas de poeira levantando do chão
                if (player.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY(),
                            player.getZ(), 2, 0.2, 0.0, 0.2, 0.0);
                }

                // 2. RADAR DE COLISÃO: Bateu em alguém?
                var hitBox = player.getBoundingBox().inflate(0.5).expandTowards(look.x, 0, look.z);
                var targets = player.level().getEntitiesOfClass(LivingEntity.class, hitBox,
                        e -> e != player && e.isAlive());

                if (!targets.isEmpty()) {
                    // ACERTOU!
                    LivingEntity target = targets.get(0);

                    // Som de impacto bruto
                    player.level().playSound(null, target.blockPosition(),
                            SoundEvents.GOAT_RAM_IMPACT, SoundSource.PLAYERS,
                            2.0F, 1.0F);

                    // Dano e Knockback Supremo!
                    target.hurt(target.damageSources().mobAttack(player), 10.0F); // 5 corações

                    // O pulo do gato: Pega a direção do seu soco para lançar o alvo
                    double dx = target.getX() - player.getX();
                    double dz = target.getZ() - player.getZ();
                    target.knockback(2.0, -dx, -dz); // Força 2.0 é MUITO forte!

                    // Para o seu dash imediatamente após bater (igual Smash Bros)
                    player.setDeltaMovement(0, player.getDeltaMovement().y, 0);
                    DASHING_GOATS.remove(player.getUUID());
                } else {
                    // Continua correndo...
                    DASHING_GOATS.put(player.getUUID(), ticksLeft - 1);
                }
            } else {
                // O tempo do Dash acabou (Não bateu em ninguém)
                DASHING_GOATS.remove(player.getUUID());
            }
        }
        // --- LOGICA DE ROUBO DE HABILIDADE TEMPO ---
        if (GOAT_STOLEN_TIMERS.containsKey(player.getUUID())) {
            int ticksLeft = GOAT_STOLEN_TIMERS.get(player.getUUID());
            if (ticksLeft > 0) {
                GOAT_STOLEN_TIMERS.put(player.getUUID(), ticksLeft - 1);
            } else {
                GOAT_STOLEN_TIMERS.remove(player.getUUID());
                player.getInventory().setItem(2, ItemStack.EMPTY);
            }
        }
        // --- CHICKEN KNOCKBACK VULNERABILITY DEGRADE ---
        if (CHICKEN_KNOCKBACK_VULNERABILITY.containsKey(player.getUUID())) {
            int ticksLeft = CHICKEN_KNOCKBACK_VULNERABILITY.get(player.getUUID());
            if (ticksLeft > 0) {
                CHICKEN_KNOCKBACK_VULNERABILITY.put(player.getUUID(), ticksLeft - 1);
            } else {
                CHICKEN_KNOCKBACK_VULNERABILITY.remove(player.getUUID());
            }
        }

        // --- GALINHA BOMBARDEIRA (ULTIMATE) TICK ---
        if (CHICKEN_BOMBERS.containsKey(player.getUUID())) {
            int ticksLeft = CHICKEN_BOMBERS.get(player.getUUID());
            if (ticksLeft > 0) {
                if (CHICKEN_BOMBER_COOLDOWN.containsKey(player.getUUID())) {
                    int cd = CHICKEN_BOMBER_COOLDOWN.get(player.getUUID());
                    if (cd > 0)
                        CHICKEN_BOMBER_COOLDOWN.put(player.getUUID(), cd - 1);
                }

                player.addEffect(new MobEffectInstance(
                        MobEffects.SLOW_FALLING, 5, 0, false, false, false));

                if (ticksLeft > 140) {
                    player.addEffect(new MobEffectInstance(
                            MobEffects.LEVITATION, 5, 8, false, false, false));
                }

                CHICKEN_BOMBERS.put(player.getUUID(), ticksLeft - 1);
            } else {
                CHICKEN_BOMBERS.remove(player.getUUID());
                player.getCooldowns().addCooldown(
                        new ItemStack(SmashMobs.CHICKEN_SUPREME.get()),
                        1200);
            }
        }

        // --- METRALHADORA DE OVOS (BURST) ---
        if (CHICKEN_MACHINE_GUN_ACTIVE.containsKey(player.getUUID())) {
            int ticksLeft = CHICKEN_MACHINE_GUN_ACTIVE.get(player.getUUID());
            if (ticksLeft > 0) {
                if (ticksLeft % 2 == 0) {
                    spawnMachineGunEgg(player);
                }
                CHICKEN_MACHINE_GUN_ACTIVE.put(player.getUUID(), ticksLeft - 1);
            } else {
                CHICKEN_MACHINE_GUN_ACTIVE.remove(player.getUUID());
            }
        }

        // --- LÓGICA DE ENGOLIR (KIRBY) ---
        if (GOAT_SWALLOWED.containsKey(player.getUUID())) {
            int ticksLeft = GOAT_SWALLOWED_TIMERS.getOrDefault(player.getUUID(), 0);

            if (player.level() instanceof ServerLevel sl) {
                Entity targetEntity = sl.getEntity(GOAT_SWALLOWED.get(player.getUUID()));

                if (targetEntity instanceof LivingEntity victim && victim.isAlive() && ticksLeft > 0) {

                    // Manda a vítima lá para o alto para ela não conseguir bater na cabra
                    victim.teleportTo(player.getX(), player.getY() + 100, player.getZ());
                    victim.setDeltaMovement(0, 0, 0);
                    victim.fallDistance = 0;

                    victim.addEffect(new MobEffectInstance(
                            MobEffects.INVISIBILITY, 10, 0, false, false, false));
                    victim.addEffect(new MobEffectInstance(
                            MobEffects.BLINDNESS, 10, 0, false, false, false));
                    victim.addEffect(new MobEffectInstance(
                            MobEffects.LEVITATION, 10, 255, false, false, false)); // Mantém ela parada no ar

                    if (ticksLeft % 10 == 0) {
                        victim.hurt(victim.damageSources().magic(), 1.0F);
                    }

                    GOAT_SWALLOWED_TIMERS.put(player.getUUID(), ticksLeft - 1);
                } else {
                    spitSwallowedEntity(player);
                }
            }
        }

        // --- LÓGICA DA ULTIMATE DE CABRA (AVALANCHE) ---
        if (GOAT_AVALANCHES.containsKey(player.getUUID())) {
            int phaseTicks = GOAT_AVALANCHES.get(player.getUUID());

            if (phaseTicks > 0) { // FASE 1: RODÓPIO
                player.setDeltaMovement(0, 0, 0);
                player.hurtMarked = true;

                if (player.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.SWEEP_ATTACK, player.getX(),
                            player.getY() + 1.0, player.getZ(), 3, 1.5, 0.5, 1.5, 0.0);
                }

                if (phaseTicks % 5 == 0) {
                    player.level().playSound(null, player.blockPosition(),
                            SoundEvents.PLAYER_ATTACK_SWEEP,
                            SoundSource.PLAYERS, 1.0F, 1.0F);
                    var area = player.getBoundingBox().inflate(3.0);
                    var targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                            e -> e != player && e.isAlive());
                    for (var t : targets) {
                        t.hurt(t.damageSources().mobAttack(player), 3.0F);
                        var pull = player.position().subtract(t.position()).normalize().scale(0.1);
                        t.setDeltaMovement(t.getDeltaMovement().add(pull));
                    }
                }

                GOAT_AVALANCHES.put(player.getUUID(), phaseTicks - 1);
            } else if (phaseTicks == 0) { // FASE 2: O PULO
                player.setDeltaMovement(0, 1.5, 0);
                player.hurtMarked = true;
                player.level().playSound(null, player.blockPosition(), SoundEvents.GOAT_LONG_JUMP,
                        SoundSource.PLAYERS, 2.0F, 1.0F);
                GOAT_AVALANCHES.put(player.getUUID(), -1);
            } else if (phaseTicks == -1) {
                // FASE 3: A QUEDA
                if (player.onGround() && player.fallDistance > 0.5) {
                    player.level().playSound(null, player.blockPosition(),
                            SoundEvents.GENERIC_EXPLODE.value(),
                            SoundSource.PLAYERS, 2.0F, 0.8F);

                    if (player.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, player.getX(),
                                player.getY(), player.getZ(), 1, 0, 0, 0, 0.0);
                    }

                    var area = player.getBoundingBox().inflate(6.0);
                    var targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                            e -> e != player && e.isAlive());
                    for (var t : targets) {
                        t.hurt(t.damageSources().mobAttack(player), 15.0F);
                        t.addEffect(new MobEffectInstance(
                                MobEffects.SLOWNESS, 60, 4));

                        double dx = t.getX() - player.getX();
                        double dz = t.getZ() - player.getZ();
                        t.knockback(1.5, -dx, -dz);
                    }
                    GOAT_AVALANCHES.remove(player.getUUID()); // Fim da ultimate
                }
            }
        }
    }

    // ========================================================
    // QUEDA DO CREEPER E EVENTOS EXTRAS
    // ========================================================
    @SubscribeEvent
    public static void onLivingKnockback(LivingKnockBackEvent event) {
        if (CHICKEN_KNOCKBACK_VULNERABILITY.containsKey(event.getEntity().getUUID())) {
            event.setStrength(event.getStrength() * 2.0F); // Dobra o repuxo
        }
    }

    // ========================================================
    // LÓGICA DE DANO TIPO SMASH BROS (PORCENTAGEM)
    // ========================================================
    @SubscribeEvent
    public static void onPlayerDamage(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            // 1. TRATAMENTO DE QUEDA NO VOID (Morte do Smash)
            if (event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD)) {
                event.setNewDamage(0.0f); // Evita a morte padrão do Minecraft (cancela o dano real)

                // Se o jogo ainda não começou, só reseta a posição sem punir vidas!
                if (!GameManager.isGameRunning) {
                    if (player instanceof ServerPlayer serverPlayer) {
                        net.minecraft.world.phys.Vec3 arenaSpawn = SmashPositionManager.getArenaVec();
                        serverPlayer.teleportTo(arenaSpawn.x, arenaSpawn.y, arenaSpawn.z);
                        serverPlayer.setDeltaMovement(0, 0, 0);
                        serverPlayer.fallDistance = 0;
                        serverPlayer.setHealth(serverPlayer.getMaxHealth());
                    }
                    return;
                }

                int lives = player.getData(ModAttachments.PLAYER_LIVES);
                lives--; // Perde uma vida

                player.setData(ModAttachments.PLAYER_LIVES, lives);
                player.setData(ModAttachments.DAMAGE_PERCENT, 0.0f); // Reseta porcentagem
 
                // Recompensa: Sempre que alguém perde a vida, TODOS os Skeletons ganham uma ultimate!
                if (player.level().getServer() != null) {
                    giveSkeletonUltToAll(player.level().getServer());
                }

                // ATUALIZA O SCOREBOARD PARA 0
                if (player.level().getServer() != null) {
                    Scoreboard scoreboard = player.level().getServer().getScoreboard();
                    Objective obj = scoreboard.getObjective("smash_percent");
                    if (obj != null) {
                        scoreboard.getOrCreatePlayerScore(player, obj).set(0);
                    }
                }

                if (player instanceof ServerPlayer serverPlayer) {
                    net.minecraft.world.phys.Vec3 arenaSpawn = SmashPositionManager.getArenaVec();

                    if (lives > 0) {
                        serverPlayer.sendSystemMessage(
                                Component.literal("§cVocê caiu! Vidas restantes: " + lives));
                        serverPlayer.teleportTo(arenaSpawn.x, arenaSpawn.y, arenaSpawn.z);
                        serverPlayer.setDeltaMovement(0, 0, 0);
                        serverPlayer.fallDistance = 0;
                        serverPlayer.setHealth(serverPlayer.getMaxHealth());
                    } else {
                        serverPlayer.sendSystemMessage(
                                Component.literal("§4Você foi eliminado!"));
                        serverPlayer.setGameMode(GameType.SPECTATOR);
                        serverPlayer.teleportTo(arenaSpawn.x, arenaSpawn.y, arenaSpawn.z);
                        serverPlayer.setDeltaMovement(0, 0, 0);
                        serverPlayer.fallDistance = 0;

                        // CHECA SE O JOGO ACABOU AGORA QUE ALGUEM FOI ELIMINADO!
                        GameManager.checkWinCondition(player.level().getServer());
                    }
                }
                return;
            }

            // Ignora dano de queda (já tem lógica de cancelar queda no mod)
            if (event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FALL)) {
                return;
            }

            // Pega a porcentagem atual do jogador
            float currentPercent = player.getData(ModAttachments.DAMAGE_PERCENT);

            // Aumenta a porcentagem baseado no dano original
            float newPercent = currentPercent + (event.getOriginalDamage() * 3.0f); // Cada 1 de dano dá 3%
            player.setData(ModAttachments.DAMAGE_PERCENT, newPercent);

            // ATUALIZA O SCOREBOARD VISUAL
            if (player.level().getServer() != null) {
                Scoreboard scoreboard = player.level().getServer().getScoreboard();
                Objective obj = scoreboard.getObjective("smash_percent");
                if (obj != null) {
                    scoreboard.getOrCreatePlayerScore(player, obj).set((int) newPercent);
                }
            }

            // Cancela o dano para o jogador não morrer
            event.setNewDamage(0.001f); // Dano quase nulo para tocar som e animação de piscar vermelho

            // Aplica repulsão extra (Knockback)
            Entity attacker = event.getSource().getEntity();
            if (attacker != null) {
                double dx = player.getX() - attacker.getX();
                double dz = player.getZ() - attacker.getZ();

                // Quanto maior a porcentagem, maior a força (multiplicador)
                double knockbackStrength = 0.5 + (newPercent / 50.0);

                player.knockback(knockbackStrength, -dx, -dz);

                // Adiciona um empurrão para cima baseado na porcentagem (Smash Bros style)
                player.setDeltaMovement(player.getDeltaMovement().add(0, 0.1 + (newPercent / 300.0), 0));
                player.hurtMarked = true;
            }

            // Toca um som de pancada para avisar o acerto
            player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT,
                    SoundSource.PLAYERS, 1.0F, 1.2F);
        }
    }

    @SubscribeEvent
    public static void onProjectileHit(ProjectileImpactEvent event) {
        if (event.getProjectile().getType() == EntityType.EGG) {
            Projectile egg = event.getProjectile();
            if (CHICKEN_BOMBER_EGGS.contains(egg)) {
                egg.level().explode(egg, egg.getX(), egg.getY(), egg.getZ(), 3.0F, false,
                        Level.ExplosionInteraction.NONE);
                egg.level().playSound(null, egg.blockPosition(), SoundEvents.CHICKEN_HURT,
                        SoundSource.PLAYERS, 2.0F, 0.5F);

                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        if (Math.random() > 0.5) {
                            BlockPos pos = egg.blockPosition().offset(x, 0, z);
                            if (egg.level().getBlockState(pos).isAir()
                                    && egg.level().getBlockState(pos.below()).isSolidRender()) {
                                egg.level().setBlock(pos,
                                        Blocks.FIRE.defaultBlockState(), 3);
                            }
                        }
                    }
                }
                CHICKEN_BOMBER_EGGS.remove(egg);

                // Remove o ovo e cancela para NÃO nascer pintinhos
                egg.discard();
                event.setCanceled(true);
            } else if (egg.getOwner() instanceof Player owner) {
                // É um ovo atirado pela Machine Gun (ou jogado na mão)
                var hitResult = event.getRayTraceResult();
                if (hitResult.getType() == HitResult.Type.ENTITY) {
                    EntityHitResult entityHit = (EntityHitResult) hitResult;
                    if (entityHit.getEntity() instanceof LivingEntity victim && victim != owner) {
                        // Aplica o dano da metralhadora (1.0 = meio coração por ovo)
                        victim.hurt(victim.damageSources().thrown(egg, owner), 1.0F);
                    }
                }

                // Remove o ovo e cancela para NÃO nascer pintinhos
                egg.discard();
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            if (CREEPER_ARMED_PLAYERS.contains(player.getUUID()) || CHICKEN_BOMBERS.containsKey(player.getUUID())) {
                boolean wasCreeper = CREEPER_ARMED_PLAYERS.remove(player.getUUID());
                if (wasCreeper) {
                    player.level().explode(player, player.getX(), player.getY(), player.getZ(), 3.0F, false,
                            Level.ExplosionInteraction.NONE);
                }
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            // PULO EXTRA DA GALINHA (FLAP)
            if (CHICKEN_BOMBERS.containsKey(player.getUUID())) {
                player.setDeltaMovement(player.getDeltaMovement().add(0, 0.8, 0));
                player.hurtMarked = true;
                player.level().playSound(null, player.blockPosition(), SoundEvents.BAT_TAKEOFF,
                        SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            spitSwallowedEntity(player); // Se o jogador engoliu alguém e morreu, ele cospe a vítima

            // COD MW2 Killstreak award to active Skeletons
            if (!player.level().isClientSide()) {
                for (Player p : player.level().players()) {
                    String morph = p.getData(ModAttachments.MORPH_DATA);
                    if ("minecraft:skeleton".equals(morph)) {
                        giveRandomKillstreak(p);
                    }
                }
            }
        }
    }

    // ========================================================
    // CRIADOR DO PROJÉTIL DA BIGORNA
    // ========================================================
    private static void spawnAnvilProjectile(Player player) {
        var level = player.level();
        var look = player.getLookAngle();
        var spawnPos = BlockPos.containing(player.getX(), player.getEyeY() + 0.5D, player.getZ());
        var oldState = level.getBlockState(spawnPos);

        FallingBlockEntity anvil = FallingBlockEntity.fall(level, spawnPos,
                Blocks.ANVIL.defaultBlockState());

        level.setBlock(spawnPos, oldState, 3);
        anvil.setHurtsEntities(2.0F, 20);
        anvil.time = 1;

        anvil.setDeltaMovement(look.scale(2.5D).add(0, 0.2D, 0));
        FLYING_ANVILS.put(anvil, player.getUUID());
        level.playSound(null, player.blockPosition(), SoundEvents.ANVIL_DESTROY,
                SoundSource.PLAYERS, 1.0F, 1.5F);
    }

    private static void spawnTrainUltimate(Player player) {
        var level = player.level();
        var look = player.getLookAngle();

        // CÁLCULO DE POSIÇÃO: 5 blocos ATRÁS do jogador!
        // Subtraímos o vetor de visão em vez de somar.
        double spawnX = player.getX() - (look.x * 5);
        double spawnY = player.getY();
        double spawnZ = player.getZ() - (look.z * 5);

        GolemTrainEntity train = new GolemTrainEntity(
                SmashMobs.GOLEM_TRAIN.get(), level);

        // Posição inicial nas costas
        train.setPos(spawnX, spawnY, spawnZ);

        // Vai em alta velocidade na direção que o Golem estava olhando (Velocidade 2.0
        // = Muito rápido!)
        train.setDeltaMovement(look.x * 2.0, 0, look.z * 2.0);
        train.setOwner(player);

        // ===================================================
        // CORREÇÃO DE ROTAÇÃO (O fim do trem andado de lado)
        // ===================================================
        float playerRot = player.getYRot();

        // DICA DE OURO: Se o trem continuar andando de lado mesmo com isso,
        // significa que no Blockbench ele foi modelado no eixo X.
        // Basta mudar a linha abaixo para: float finalRot = playerRot + 90f; (ou - 90f)
        float finalRot = playerRot + 90f;

        train.setYRot(finalRot);
        train.yRotO = finalRot;
        train.setYBodyRot(finalRot); // Essencial para GeoEntities!

        level.addFreshEntity(train);

        level.playSound(null, player.blockPosition(), GOLEM_SUPREME_SOUND.get(),
                SoundSource.PLAYERS, 2.0F, 0.9F);
    }

    private static void spawnMachineGunEgg(Player player) {
        Level level = player.level();
        if (!level.isClientSide()) {
            level.playSound(null, player.blockPosition(), SoundEvents.CHICKEN_EGG,
                    SoundSource.PLAYERS, 1.0F, 1.5F);

            ThrowableProjectile egg = (ThrowableProjectile) EntityType.EGG
                    .create(level, EntitySpawnReason.TRIGGERED);

            if (egg != null) {
                egg.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
                egg.setOwner(player);
                egg.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.0F, 14.0F);
                level.addFreshEntity(egg);

                level.playSound(null, player.blockPosition(), SoundEvents.EGG_THROW,
                        SoundSource.PLAYERS, 0.5F,
                        1.2F + (level.getRandom().nextFloat() * 0.5f));
            }
        }
    }

    // ========================================================
    // SKELETON - BONE BOOMERANG E ULTIMATE
    // ========================================================
    public static void spawnBoomerangBone(Player player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 dir = player.getLookAngle();
        FLYING_BONES.add(new BoomerangBone(player, eyePos, dir));
    }
 
    // =================================================================
    // RECOMPENSA DE ULTIMATE RANDÔMICA PARA TODOS OS SKELETONS
    // =================================================================
    public static void giveSkeletonUltToAll(net.minecraft.server.MinecraftServer server) {
        if (server == null) return;
 
        net.minecraft.world.item.Item[] pool = {
            SmashMobs.SKELETON_PREDATOR_MISSILE.get(),
            SmashMobs.SKELETON_AIRSTRIKE.get(),
            SmashMobs.SKELETON_SENTRY.get()
        };
 
        java.util.Random random = new java.util.Random();
 
        for (net.minecraft.server.level.ServerPlayer player : server.getPlayerList().getPlayers()) {
            String morph = player.getData(ModAttachments.MORPH_DATA);
            if ("minecraft:skeleton".equals(morph)) {
                net.minecraft.world.item.Item selected = pool[random.nextInt(pool.length)];
                player.addItem(new net.minecraft.world.item.ItemStack(selected));
                player.containerMenu.broadcastChanges(); // Força sincronismo do inventário visual!
                player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("§6[Soldado] §eAlguém caiu! Você ganhou uma habilidade tática!")
                );
            }
        }
    }
    @SubscribeEvent
    public static void onMobMeleeDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof Player attacker && event.getSource().getDirectEntity() == attacker) {
            String morph = attacker.getData(ModAttachments.MORPH_DATA);
            if (morph == null || morph.isEmpty()) return;

            float baseAmount = event.getAmount();
            float customDamage = baseAmount;

            switch (morph) {
                case "minecraft:iron_golem":
                    customDamage = 7.0F; // 3.5 Corações
                    break;
                case "minecraft:goat":
                    customDamage = 6.0F; // 3 Corações
                    break;
                case "minecraft:creeper":
                    customDamage = 5.0F; // 2.5 Corações
                    break;
                case "minecraft:skeleton":
                    customDamage = 4.0F; // 2 Corações
                    break;
                case "minecraft:chicken":
                    customDamage = 2.5F; // 1.25 Corações
                    break;
            }

            if (customDamage != baseAmount) {
                event.setAmount(customDamage);
            }
        }
    }
}