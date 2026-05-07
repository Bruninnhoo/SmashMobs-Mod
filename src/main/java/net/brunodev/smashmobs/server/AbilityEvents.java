package net.brunodev.smashmobs.server;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;

import static net.brunodev.smashmobs.SmashMobs.GOLEM_SUPREME_SOUND;
import static net.brunodev.smashmobs.SmashMobs.GOLEM_THROW_ANVIL_SOUND;

@EventBusSubscriber(modid = "smashmobs")
public class AbilityEvents {

    // ====================================
    // -------------- IRON GOLEM -----------
    // ====================================
    public static final java.util.Map<UUID, Integer> PENDING_ANVILS = new java.util.HashMap<>();
    public static final Map<FallingBlockEntity, UUID> FLYING_ANVILS = new HashMap<>();

    // Variáveis do Puxão e Agarrão
    public static final Map<UUID, UUID> GRABBED_ENTITIES = new HashMap<>();
    public static final Map<UUID, Integer> GRAB_TIMERS = new HashMap<>();
    public static final Map<UUID, UUID> PULLING_ENTITIES = new HashMap<>();
    public static final Map<UUID, Integer> PENDING_TRAINS = new HashMap<>();

    // CLASSE DO SKILLSHOT (O Gancho Fantasma)
    public static class GolemHook {
        public Player owner;
        public net.minecraft.world.phys.Vec3 pos;
        public net.minecraft.world.phys.Vec3 direction;
        public double distance;

        public GolemHook(Player o, net.minecraft.world.phys.Vec3 p, net.minecraft.world.phys.Vec3 d) {
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
    public static final Map<net.minecraft.world.entity.item.ItemEntity, UUID> CHICKEN_MINES = new HashMap<>();
    public static final Map<UUID, Integer> CHICKEN_KNOCKBACK_VULNERABILITY = new HashMap<>();
    public static final Map<UUID, Integer> CHICKEN_BOMBERS = new HashMap<>();
    public static final java.util.Set<net.minecraft.world.entity.projectile.Projectile> CHICKEN_BOMBER_EGGS = new HashSet<>();
    public static final Map<UUID, Integer> CHICKEN_BOMBER_COOLDOWN = new HashMap<>();
    public static final Map<UUID, Integer> CHICKEN_MACHINE_GUN_ACTIVE = new HashMap<>();

    public static void spitSwallowedEntity(Player goat) {
        UUID goatId = goat.getUUID();
        if (!GOAT_SWALLOWED.containsKey(goatId))
            return;

        if (goat.level() instanceof net.minecraft.server.level.ServerLevel sl) {
            net.minecraft.world.entity.Entity targetEntity = sl.getEntity(GOAT_SWALLOWED.get(goatId));

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

                goat.level().playSound(null, goat.blockPosition(), net.minecraft.sounds.SoundEvents.LLAMA_SPIT,
                        net.minecraft.sounds.SoundSource.PLAYERS, 2.0F, 1.0F);

                if (victim instanceof Player victimPlayer) {
                    String morph = victimPlayer.getData(net.brunodev.smashmobs.registration.ModAttachments.MORPH_DATA);
                    net.minecraft.world.item.Item stolenItem = null;
                    if (morph.equals("minecraft:creeper"))
                        stolenItem = net.brunodev.smashmobs.SmashMobs.CREEPER_EXPLOSION.get();
                    else if (morph.equals("minecraft:iron_golem"))
                        stolenItem = net.brunodev.smashmobs.SmashMobs.GOLEM_THROW_ANVIL.get();
                    else if (morph.equals("minecraft:goat"))
                        stolenItem = net.brunodev.smashmobs.SmashMobs.GOAT_DASH.get();

                    if (stolenItem != null && goat.getInventory().contains(new net.minecraft.world.item.ItemStack(
                            net.brunodev.smashmobs.SmashMobs.GOAT_SWALLOW.get()))) {
                        goat.getInventory().setItem(2, new net.minecraft.world.item.ItemStack(stolenItem));
                        GOAT_STOLEN_TIMERS.put(goatId, 300); // 15 Segundos
                    }
                }
            }
        }

        GOAT_SWALLOWED.remove(goatId);
        GOAT_SWALLOWED_TIMERS.remove(goatId);
        goat.getCooldowns().addCooldown(
                new net.minecraft.world.item.ItemStack(net.brunodev.smashmobs.SmashMobs.GOAT_SWALLOW.get()), 100);
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
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                anvil.level().playSound(null, anvil.blockPosition(), net.minecraft.sounds.SoundEvents.IRON_GOLEM_DAMAGE,
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.5F);

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

                eggMine.level().playSound(null, eggMine.blockPosition(), net.minecraft.sounds.SoundEvents.CHICKEN_EGG,
                        net.minecraft.sounds.SoundSource.PLAYERS, 2.0F, 0.5F);
                eggMine.level().playSound(null, eggMine.blockPosition(),
                        net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value(),
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, 1.5F);

                if (eggMine.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                    sl.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION, eggMine.getX(),
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
            net.minecraft.world.phys.Vec3 nextPos = hook.pos.add(hook.direction.scale(hookSpeed));

            // Verifica se o gancho bateu numa parede!
            var clipResult = hook.owner.level().clip(new net.minecraft.world.level.ClipContext(
                    hook.pos, nextPos, net.minecraft.world.level.ClipContext.Block.COLLIDER,
                    net.minecraft.world.level.ClipContext.Fluid.NONE, hook.owner));

            if (clipResult.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
                // Errou! Bateu na parede. Toca um som de ferro batendo em pedra e apaga.
                hook.owner.level().playSound(null, net.minecraft.core.BlockPos.containing(clipResult.getLocation()),
                        net.minecraft.sounds.SoundEvents.ANVIL_PLACE, net.minecraft.sounds.SoundSource.PLAYERS, 0.5F,
                        2.0F);
                hookIterator.remove();
                continue;
            }

            // Move o gancho pra frente
            hook.pos = nextPos;
            hook.distance += hookSpeed;

            // RASTRO VISUAL: Desenha partículas pra todo mundo ver o gancho voando!
            if (hook.owner.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                // Fumaça larga e partícula de crítico pra simular um soco de ar
                sl.sendParticles(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE, hook.pos.x, hook.pos.y,
                        hook.pos.z, 2, 0.1, 0.1, 0.1, 0.0);
                sl.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT, hook.pos.x, hook.pos.y, hook.pos.z, 1,
                        0.0, 0.0, 0.0, 0.0);
            }

            // Verifica se a "ponta" do gancho encostou em alguém
            var hitBox = new net.minecraft.world.phys.AABB(hook.pos.x - 0.5, hook.pos.y - 0.5, hook.pos.z - 0.5,
                    hook.pos.x + 0.5, hook.pos.y + 0.5, hook.pos.z + 0.5);
            var targets = hook.owner.level().getEntitiesOfClass(LivingEntity.class, hitBox,
                    e -> e != hook.owner && e.isAlive());

            if (!targets.isEmpty()) {
                // ACERTOU O SKILLSHOT!
                LivingEntity hitTarget = targets.get(0);
                PULLING_ENTITIES.put(hook.owner.getUUID(), hitTarget.getUUID());

                // Som de agarrão de metal
                hook.owner.level().playSound(null, hitTarget.blockPosition(),
                        net.minecraft.sounds.SoundEvents.IRON_GOLEM_REPAIR, net.minecraft.sounds.SoundSource.PLAYERS,
                        1.0F, 1.5F);
                hookIterator.remove(); // O gancho some e o alvo começa a ser puxado
                continue;
            }

            // Se o gancho viajou mais de 12 blocos e não pegou nada, ele some no ar
            if (hook.distance >= 12.0) {
                hookIterator.remove();
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

            if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                net.minecraft.world.entity.Entity target = serverLevel.getEntity(targetId);

                if (target instanceof LivingEntity livingTarget) {
                    var look = golemPlayer.getLookAngle();
                    livingTarget.setDeltaMovement(look.scale(2.5).add(0, 0.8, 0));
                    livingTarget.hurt(livingTarget.damageSources().mobAttack(golemPlayer), 8.0F);
                    livingTarget.hurtMarked = true;
                    level.playSound(null, golemPlayer.blockPosition(),
                            net.minecraft.sounds.SoundEvents.IRON_GOLEM_ATTACK,
                            net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.8F);
                }
            }
            GRABBED_ENTITIES.remove(golemId);
            GRAB_TIMERS.remove(golemId);
            golemPlayer.getCooldowns()
                    .addCooldown(golemPlayer.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND), 100);

        }
        // CENA 2: ATIRAR O GANCHO! (Skillshot)
        else if (!PULLING_ENTITIES.containsKey(golemId)) {

            // Verifica se o jogador já não atirou um gancho que ainda está voando
            boolean alreadyHooking = FLYING_HOOKS.stream().anyMatch(h -> h.owner.getUUID().equals(golemId));

            if (!alreadyHooking) {
                net.minecraft.world.phys.Vec3 eyePos = golemPlayer.getEyePosition();
                net.minecraft.world.phys.Vec3 dir = golemPlayer.getLookAngle();

                // Cria o gancho e coloca na pista!
                FLYING_HOOKS.add(new GolemHook(golemPlayer, eyePos, dir));

                // Som de lançamento no ar
                level.playSound(null, golemPlayer.blockPosition(),
                        net.minecraft.sounds.SoundEvents.FISHING_BOBBER_THROW, net.minecraft.sounds.SoundSource.PLAYERS,
                        1.5F, 0.5F);

                // TODO: Chamar o pacote de rede para esticar o braço visualmente
            }
        }
    }

    // ========================================================
    // TICK DO JOGADOR
    // ========================================================
    @SubscribeEvent
    public static void onPlayerTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
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
            if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                net.minecraft.world.entity.Entity target = serverLevel
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
                                net.minecraft.sounds.SoundEvents.IRON_GOLEM_REPAIR,
                                net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 2.0F);
                    }
                } else {
                    PULLING_ENTITIES.remove(player.getUUID());
                }
            }
        }

        // --- LÓGICA DO GRAB FIXO (MANTÉM NA MÃO) ---
        if (GRABBED_ENTITIES.containsKey(player.getUUID())) {
            int timer = GRAB_TIMERS.getOrDefault(player.getUUID(), 0);

            if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                net.minecraft.world.entity.Entity target = serverLevel
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
                            .addCooldown(player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND), 100);
                }
            }
        }

        // --- CREEPER SUPREME LÓGICA ---
        if (CREEPER_SUPREMES.containsKey(player.getUUID())) {
            int ticksLeft = CREEPER_SUPREMES.get(player.getUUID());

            if (ticksLeft > 0) {
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        MobEffects.SLOWNESS, 2, 255, false, false, false));

                double radius = 8.0;
                var area = player.getBoundingBox().inflate(radius);
                var targets = player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, area,
                        e -> e != player);

                for (var target : targets) {
                    double dx = player.getX() - target.getX();
                    double dy = player.getY() - target.getY();
                    double dz = player.getZ() - target.getZ();

                    net.minecraft.world.phys.Vec3 pull = new net.minecraft.world.phys.Vec3(dx, dy, dz).normalize()
                            .scale(0.08);

                    target.setDeltaMovement(target.getDeltaMovement().add(pull));
                    target.hurtMarked = true;
                }

                if (ticksLeft % 5 == 0) {
                    player.level().playSound(null, player.blockPosition(),
                            net.minecraft.sounds.SoundEvents.CREEPER_PRIMED, net.minecraft.sounds.SoundSource.PLAYERS,
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
                if (player.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                    sl.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD, player.getX(), player.getY(),
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
                            net.minecraft.sounds.SoundEvents.GOAT_RAM_IMPACT, net.minecraft.sounds.SoundSource.PLAYERS,
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
                player.getInventory().setItem(2, net.minecraft.world.item.ItemStack.EMPTY);
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

                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.SLOW_FALLING, 5, 0, false, false, false));

                if (ticksLeft > 140) {
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.LEVITATION, 5, 8, false, false, false));
                }

                CHICKEN_BOMBERS.put(player.getUUID(), ticksLeft - 1);
            } else {
                CHICKEN_BOMBERS.remove(player.getUUID());
                player.getCooldowns().addCooldown(
                        new net.minecraft.world.item.ItemStack(net.brunodev.smashmobs.SmashMobs.CHICKEN_SUPREME.get()),
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

            if (player.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                net.minecraft.world.entity.Entity targetEntity = sl.getEntity(GOAT_SWALLOWED.get(player.getUUID()));

                if (targetEntity instanceof LivingEntity victim && victim.isAlive() && ticksLeft > 0) {

                    // Manda a vítima lá para o alto para ela não conseguir bater na cabra
                    victim.teleportTo(player.getX(), player.getY() + 100, player.getZ());
                    victim.setDeltaMovement(0, 0, 0);
                    victim.fallDistance = 0;
                    
                    victim.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.INVISIBILITY, 10, 0, false, false, false));
                    victim.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.BLINDNESS, 10, 0, false, false, false));
                    victim.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.LEVITATION, 10, 255, false, false, false)); // Mantém ela parada no ar

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

                if (player.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                    sl.sendParticles(net.minecraft.core.particles.ParticleTypes.SWEEP_ATTACK, player.getX(),
                            player.getY() + 1.0, player.getZ(), 3, 1.5, 0.5, 1.5, 0.0);
                }

                if (phaseTicks % 5 == 0) {
                    player.level().playSound(null, player.blockPosition(),
                            net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP,
                            net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
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
                player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.GOAT_LONG_JUMP,
                        net.minecraft.sounds.SoundSource.PLAYERS, 2.0F, 1.0F);
                GOAT_AVALANCHES.put(player.getUUID(), -1);
            } else if (phaseTicks == -1) {
                // FASE 3: A QUEDA
                if (player.onGround() && player.fallDistance > 0.5) {
                    player.level().playSound(null, player.blockPosition(),
                            net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value(),
                            net.minecraft.sounds.SoundSource.PLAYERS, 2.0F, 0.8F);

                    if (player.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                        sl.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER, player.getX(),
                                player.getY(), player.getZ(), 1, 0, 0, 0, 0.0);
                    }

                    var area = player.getBoundingBox().inflate(6.0);
                    var targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                            e -> e != player && e.isAlive());
                    for (var t : targets) {
                        t.hurt(t.damageSources().mobAttack(player), 15.0F);
                        t.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.SLOWNESS, 60, 4));

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
    public static void onLivingKnockback(net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent event) {
        if (CHICKEN_KNOCKBACK_VULNERABILITY.containsKey(event.getEntity().getUUID())) {
            event.setStrength(event.getStrength() * 2.0F); // Dobra o repuxo
        }
    }

    // ========================================================
    // LÓGICA DE DANO TIPO SMASH BROS (PORCENTAGEM)
    // ========================================================
    @SubscribeEvent
    public static void onPlayerDamage(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            // 1. TRATAMENTO DE QUEDA NO VOID (Morte do Smash)
            if (event.getSource().is(net.minecraft.world.damagesource.DamageTypes.FELL_OUT_OF_WORLD)) {
                event.setNewDamage(0.0f); // Evita a morte padrão do Minecraft (cancela o dano real)

                int lives = player.getData(net.brunodev.smashmobs.registration.ModAttachments.PLAYER_LIVES);
                lives--; // Perde uma vida

                player.setData(net.brunodev.smashmobs.registration.ModAttachments.PLAYER_LIVES, lives);
                player.setData(net.brunodev.smashmobs.registration.ModAttachments.DAMAGE_PERCENT, 0.0f); // Reseta porcentagem
                
                // ATUALIZA O SCOREBOARD PARA 0
                if (player.level().getServer() != null) {
                    net.minecraft.world.scores.Scoreboard scoreboard = player.level().getServer().getScoreboard();
                    net.minecraft.world.scores.Objective obj = scoreboard.getObjective("smash_percent");
                    if (obj != null) {
                        scoreboard.getOrCreatePlayerScore(player, obj).set(0);
                    }
                }

                if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    if (lives > 0) {
                        serverPlayer.sendSystemMessage(
                                net.minecraft.network.chat.Component.literal("§cVocê caiu! Vidas restantes: " + lives));
                        serverPlayer.teleportTo(0, 100, 0);
                        serverPlayer.setDeltaMovement(0, 0, 0);
                        serverPlayer.fallDistance = 0;
                        serverPlayer.setHealth(serverPlayer.getMaxHealth());
                    } else {
                        serverPlayer.sendSystemMessage(
                                net.minecraft.network.chat.Component.literal("§4Você foi eliminado!"));
                        serverPlayer.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
                    }
                }
                return;
            }

            // Ignora dano de queda (já tem lógica de cancelar queda no mod)
            if (event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FALL)) {
                return;
            }

            // Pega a porcentagem atual do jogador
            float currentPercent = player.getData(net.brunodev.smashmobs.registration.ModAttachments.DAMAGE_PERCENT);

            // Aumenta a porcentagem baseado no dano original
            float newPercent = currentPercent + (event.getOriginalDamage() * 3.0f); // Cada 1 de dano dá 3%
            player.setData(net.brunodev.smashmobs.registration.ModAttachments.DAMAGE_PERCENT, newPercent);
            
            // ATUALIZA O SCOREBOARD VISUAL
            if (player.level().getServer() != null) {
                net.minecraft.world.scores.Scoreboard scoreboard = player.level().getServer().getScoreboard();
                net.minecraft.world.scores.Objective obj = scoreboard.getObjective("smash_percent");
                if (obj != null) {
                    scoreboard.getOrCreatePlayerScore(player, obj).set((int) newPercent);
                }
            }

            // Cancela o dano para o jogador não morrer
            event.setNewDamage(0.001f); // Dano quase nulo para tocar som e animação de piscar vermelho

            // Aplica repulsão extra (Knockback)
            net.minecraft.world.entity.Entity attacker = event.getSource().getEntity();
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
            player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_CRIT,
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.2F);
        }
    }

    @SubscribeEvent
    public static void onProjectileHit(net.neoforged.neoforge.event.entity.ProjectileImpactEvent event) {
        if (event.getProjectile().getType() == net.minecraft.world.entity.EntityType.EGG) {
            net.minecraft.world.entity.projectile.Projectile egg = event.getProjectile();
            if (CHICKEN_BOMBER_EGGS.contains(egg)) {
                egg.level().explode(egg, egg.getX(), egg.getY(), egg.getZ(), 3.0F, false,
                        Level.ExplosionInteraction.NONE);
                egg.level().playSound(null, egg.blockPosition(), net.minecraft.sounds.SoundEvents.CHICKEN_HURT,
                        net.minecraft.sounds.SoundSource.PLAYERS, 2.0F, 0.5F);

                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        if (Math.random() > 0.5) {
                            net.minecraft.core.BlockPos pos = egg.blockPosition().offset(x, 0, z);
                            if (egg.level().getBlockState(pos).isAir()
                                    && egg.level().getBlockState(pos.below()).isSolidRender()) {
                                egg.level().setBlock(pos,
                                        net.minecraft.world.level.block.Blocks.FIRE.defaultBlockState(), 3);
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
                if (hitResult.getType() == net.minecraft.world.phys.HitResult.Type.ENTITY) {
                    net.minecraft.world.phys.EntityHitResult entityHit = (net.minecraft.world.phys.EntityHitResult) hitResult;
                    if (entityHit.getEntity() instanceof net.minecraft.world.entity.LivingEntity victim && victim != owner) {
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
    public static void onLivingJump(net.neoforged.neoforge.event.entity.living.LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            // PULO EXTRA DA GALINHA (FLAP)
            if (CHICKEN_BOMBERS.containsKey(player.getUUID())) {
                player.setDeltaMovement(player.getDeltaMovement().add(0, 0.8, 0));
                player.hurtMarked = true;
                player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.BAT_TAKEOFF,
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            spitSwallowedEntity(player); // Se o jogador engoliu alguém e morreu, ele cospe a vítima
        }
    }

    // ========================================================
    // CRIADOR DO PROJÉTIL DA BIGORNA
    // ========================================================
    private static void spawnAnvilProjectile(Player player) {
        var level = player.level();
        var look = player.getLookAngle();
        var spawnPos = net.minecraft.core.BlockPos.containing(player.getX(), player.getEyeY() + 0.5D, player.getZ());
        var oldState = level.getBlockState(spawnPos);

        FallingBlockEntity anvil = FallingBlockEntity.fall(level, spawnPos,
                net.minecraft.world.level.block.Blocks.ANVIL.defaultBlockState());

        level.setBlock(spawnPos, oldState, 3);
        anvil.setHurtsEntities(2.0F, 20);
        anvil.time = 1;

        anvil.setDeltaMovement(look.scale(2.5D).add(0, 0.2D, 0));
        FLYING_ANVILS.put(anvil, player.getUUID());
        level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.ANVIL_DESTROY,
                net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.5F);
    }

    private static void spawnTrainUltimate(Player player) {
        var level = player.level();
        var look = player.getLookAngle();

        // CÁLCULO DE POSIÇÃO: 5 blocos ATRÁS do jogador!
        // Subtraímos o vetor de visão em vez de somar.
        double spawnX = player.getX() - (look.x * 5);
        double spawnY = player.getY();
        double spawnZ = player.getZ() - (look.z * 5);

        net.brunodev.smashmobs.entity.GolemTrainEntity train = new net.brunodev.smashmobs.entity.GolemTrainEntity(
                net.brunodev.smashmobs.SmashMobs.GOLEM_TRAIN.get(), level);

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
                net.minecraft.sounds.SoundSource.PLAYERS, 2.0F, 0.9F);
    }

    private static void spawnMachineGunEgg(Player player) {
        Level level = player.level();
        if (!level.isClientSide()) {
            level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.CHICKEN_EGG,
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.5F);

            net.minecraft.world.entity.projectile.ThrowableProjectile egg = (net.minecraft.world.entity.projectile.ThrowableProjectile) net.minecraft.world.entity.EntityType.EGG
                    .create(level, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);

            if (egg != null) {
                egg.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
                egg.setOwner(player);
                egg.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.0F, 14.0F);
                level.addFreshEntity(egg);

                level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.EGG_THROW,
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.5F,
                        1.2F + (level.getRandom().nextFloat() * 0.5f));
            }
        }
    }
}