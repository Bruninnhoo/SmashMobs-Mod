package net.brunodev.smashmobs.server;

import net.minecraft.sounds.SoundEvents;
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

    //====================================
    //-------------- IRON GOLEM -----------
    //====================================
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
            owner = o; pos = p; direction = d; distance = 0;
        }
    }
    public static final List<GolemHook> FLYING_HOOKS = new ArrayList<>();

    //====================================
    //-------------- CREEPER --------------
    //====================================
    public static final Set<UUID> CREEPER_ARMED_PLAYERS = new HashSet<>();
    public static final java.util.Map<UUID, Integer> CREEPER_SUPREMES = new java.util.HashMap<>();

    //====================================
    //-------------- GOAT ----------------
    //====================================
    public static final Map<UUID, Integer> DASHING_GOATS = new HashMap<>();
    public static final Map<UUID, UUID> GOAT_LEASHES = new HashMap<>();
    public static final Map<UUID, Integer> GOAT_LEASH_TIMERS = new HashMap<>();


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
            var targets = anvil.level().getEntitiesOfClass(LivingEntity.class, hitBox, e -> !e.getUUID().equals(throwerId));

            if (!targets.isEmpty()) {
                LivingEntity target = targets.get(0);
                target.hurt(target.damageSources().anvil(anvil), 12.0F);

                double dx = target.getX() - anvil.getX();
                double dz = target.getZ() - anvil.getZ();
                target.knockback(1.5, -dx, -dz);

                anvil.level().playSound(null, anvil.blockPosition(), GOLEM_THROW_ANVIL_SOUND.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                anvil.level().playSound(null, anvil.blockPosition(), net.minecraft.sounds.SoundEvents.IRON_GOLEM_DAMAGE, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.5F);

                anvil.discard();
                anvilIterator.remove();
            }
        }

        // 2. O RADAR DO SKILLSHOT (O Puxão do Blitzcrank)
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
                    hook.pos, nextPos, net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, hook.owner
            ));

            if (clipResult.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
                // Errou! Bateu na parede. Toca um som de ferro batendo em pedra e apaga.
                hook.owner.level().playSound(null, net.minecraft.core.BlockPos.containing(clipResult.getLocation()), net.minecraft.sounds.SoundEvents.ANVIL_PLACE, net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 2.0F);
                hookIterator.remove();
                continue;
            }

            // Move o gancho pra frente
            hook.pos = nextPos;
            hook.distance += hookSpeed;

            // RASTRO VISUAL: Desenha partículas pra todo mundo ver o gancho voando!
            if (hook.owner.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                // Fumaça larga e partícula de crítico pra simular um soco de ar
                sl.sendParticles(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE, hook.pos.x, hook.pos.y, hook.pos.z, 2, 0.1, 0.1, 0.1, 0.0);
                sl.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT, hook.pos.x, hook.pos.y, hook.pos.z, 1, 0.0, 0.0, 0.0, 0.0);
            }

            // Verifica se a "ponta" do gancho encostou em alguém
            var hitBox = new net.minecraft.world.phys.AABB(hook.pos.x - 0.5, hook.pos.y - 0.5, hook.pos.z - 0.5, hook.pos.x + 0.5, hook.pos.y + 0.5, hook.pos.z + 0.5);
            var targets = hook.owner.level().getEntitiesOfClass(LivingEntity.class, hitBox, e -> e != hook.owner && e.isAlive());

            if (!targets.isEmpty()) {
                // ACERTOU O SKILLSHOT!
                LivingEntity hitTarget = targets.get(0);
                PULLING_ENTITIES.put(hook.owner.getUUID(), hitTarget.getUUID());

                // Som de agarrão de metal
                hook.owner.level().playSound(null, hitTarget.blockPosition(), net.minecraft.sounds.SoundEvents.IRON_GOLEM_REPAIR, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.5F);
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
                    level.playSound(null, golemPlayer.blockPosition(), net.minecraft.sounds.SoundEvents.IRON_GOLEM_ATTACK, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.8F);
                }
            }
            GRABBED_ENTITIES.remove(golemId);
            GRAB_TIMERS.remove(golemId);
            golemPlayer.getCooldowns().addCooldown(golemPlayer.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND), 100);

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
                level.playSound(null, golemPlayer.blockPosition(), net.minecraft.sounds.SoundEvents.FISHING_BOBBER_THROW, net.minecraft.sounds.SoundSource.PLAYERS, 1.5F, 0.5F);

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
        if (player.level().isClientSide()) return;

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
                net.minecraft.world.entity.Entity target = serverLevel.getEntity(PULLING_ENTITIES.get(player.getUUID()));

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
                        player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.IRON_GOLEM_REPAIR, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 2.0F);
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
                net.minecraft.world.entity.Entity target = serverLevel.getEntity(GRABBED_ENTITIES.get(player.getUUID()));

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
                    player.getCooldowns().addCooldown(player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND), 100);
                }
            }
        }

        // --- CREEPER SUPREME LÓGICA ---
        if (CREEPER_SUPREMES.containsKey(player.getUUID())) {
            int ticksLeft = CREEPER_SUPREMES.get(player.getUUID());

            if (ticksLeft > 0) {
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        MobEffects.SLOWNESS, 2, 255, false, false, false
                ));

                double radius = 8.0;
                var area = player.getBoundingBox().inflate(radius);
                var targets = player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, area, e -> e != player);

                for (var target : targets) {
                    double dx = player.getX() - target.getX();
                    double dy = player.getY() - target.getY();
                    double dz = player.getZ() - target.getZ();

                    net.minecraft.world.phys.Vec3 pull = new net.minecraft.world.phys.Vec3(dx, dy, dz).normalize().scale(0.08);

                    target.setDeltaMovement(target.getDeltaMovement().add(pull));
                    target.hurtMarked = true;
                }

                if (ticksLeft % 5 == 0) {
                    player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.CREEPER_PRIMED, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.5F);
                }

                CREEPER_SUPREMES.put(player.getUUID(), ticksLeft - 1);
            } else {
                CREEPER_SUPREMES.remove(player.getUUID());
                player.level().explode(player, player.getX(), player.getY() + 1, player.getZ(), 6.0F, false, Level.ExplosionInteraction.NONE);
            }
        }

        // --- LÓGICA DA CABEÇADA (GOAT DASH) ---
        if (DASHING_GOATS.containsKey(player.getUUID())) {
            int ticksLeft = DASHING_GOATS.get(player.getUUID());

            if (ticksLeft > 0) {
                var look = player.getLookAngle();

                // 1. O IMPULSO: Joga o jogador para frente muito rápido!
                // Ignoramos a altura (Y) para o dash ser retinho no chão, mas você pode pular e dar o dash no ar!
                player.setDeltaMovement(look.x * 1.8, player.getDeltaMovement().y, look.z * 1.8);
                player.hurtMarked = true;

                // Partículas de poeira levantando do chão
                if (player.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                    sl.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD, player.getX(), player.getY(), player.getZ(), 2, 0.2, 0.0, 0.2, 0.0);
                }

                // 2. RADAR DE COLISÃO: Bateu em alguém?
                var hitBox = player.getBoundingBox().inflate(0.5).expandTowards(look.x, 0, look.z);
                var targets = player.level().getEntitiesOfClass(LivingEntity.class, hitBox, e -> e != player && e.isAlive());

                if (!targets.isEmpty()) {
                    // ACERTOU!
                    LivingEntity target = targets.get(0);

                    // Som de impacto bruto
                    player.level().playSound(null, target.blockPosition(), net.minecraft.sounds.SoundEvents.GOAT_RAM_IMPACT, net.minecraft.sounds.SoundSource.PLAYERS, 2.0F, 1.0F);

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

        // --- LÓGICA DA LÍNGUA ELÁSTICA (GOAT SIMULATOR) ---
        if (GOAT_LEASHES.containsKey(player.getUUID())) {
            int ticksLeft = GOAT_LEASH_TIMERS.getOrDefault(player.getUUID(), 0);

            if (player.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                net.minecraft.world.entity.Entity targetEntity = sl.getEntity(GOAT_LEASHES.get(player.getUUID()));

                if (targetEntity instanceof LivingEntity victim && victim.isAlive() && ticksLeft > 0) {

                    double distance = victim.distanceTo(player);

                    // ==========================================
                    // O DESENHO DA LÍNGUA COM PARTÍCULAS
                    // Traça uma linha de partículas rosa/gosmentas da cabra até a vítima
                    // ==========================================
                    double steps = distance * 2; // Quantidade de partículas baseada na distância
                    double dx = (victim.getX() - player.getX()) / steps;
                    double dy = ((victim.getY() + 1.0) - (player.getY() + 1.0)) / steps;
                    double dz = (victim.getZ() - player.getZ()) / steps;

                    for (int i = 0; i < steps; i++) {
                        sl.sendParticles(net.minecraft.core.particles.ParticleTypes.DRIPPING_HONEY,
                                player.getX() + (dx * i), (player.getY() + 1.0) + (dy * i), player.getZ() + (dz * i),
                                1, 0, 0, 0, 0);
                    }

                    // ==========================================
                    // A FÍSICA DO ELÁSTICO
                    // ==========================================
                    // Se o cara estiver a mais de 3 blocos de distância, a corda estica e puxa ele!
                    if (distance > 3.0) {
                        var pullVector = player.position().subtract(victim.position()).normalize();

                        // Quanto mais longe, mais forte puxa (Efeito elástico!)
                        double tension = Math.min(distance * 0.15, 2.0);

                        // Adicionamos um pouco de movimento Y (0.2) para ele sair quicando e não travar nos blocos do chão
                        victim.setDeltaMovement(pullVector.x * tension, pullVector.y * tension + 0.2, pullVector.z * tension);
                        victim.hurtMarked = true;
                    }

                    GOAT_LEASH_TIMERS.put(player.getUUID(), ticksLeft - 1);
                } else {
                    // O tempo acabou ou o inimigo morreu. Quebra a língua!
                    GOAT_LEASHES.remove(player.getUUID());
                    GOAT_LEASH_TIMERS.remove(player.getUUID());

                    // Som de chicote/elástico quebrando
                    player.level().playSound(null, player.blockPosition(), SoundEvents.BEEHIVE_DRIP, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 2.0F);
                }
            }
        }
    }


    // ========================================================
    // QUEDA DO CREEPER
    // ========================================================
    @SubscribeEvent
    public static void onPlayerFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            if (CREEPER_ARMED_PLAYERS.contains(player.getUUID())) {
                CREEPER_ARMED_PLAYERS.remove(player.getUUID());
                event.setCanceled(true);
                player.level().explode(player, player.getX(), player.getY(), player.getZ(), 3.0F, false, Level.ExplosionInteraction.NONE);
            }
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

        FallingBlockEntity anvil = FallingBlockEntity.fall(level, spawnPos, net.minecraft.world.level.block.Blocks.ANVIL.defaultBlockState());

        level.setBlock(spawnPos, oldState, 3);
        anvil.setHurtsEntities(2.0F, 20);
        anvil.time = 1;

        anvil.setDeltaMovement(look.scale(2.5D).add(0, 0.2D, 0));
        FLYING_ANVILS.put(anvil, player.getUUID());
        level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.ANVIL_DESTROY, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.5F);
    }

    private static void spawnTrainUltimate(Player player) {
        var level = player.level();
        var look = player.getLookAngle();

        // CÁLCULO DE POSIÇÃO: 5 blocos ATRÁS do jogador!
        // Subtraímos o vetor de visão em vez de somar.
        double spawnX = player.getX() - (look.x * 5);
        double spawnY = player.getY();
        double spawnZ = player.getZ() - (look.z * 5);

        net.brunodev.smashmobs.entity.GolemTrainEntity train = new net.brunodev.smashmobs.entity.GolemTrainEntity(net.brunodev.smashmobs.SmashMobs.GOLEM_TRAIN.get(), level);

        // Posição inicial nas costas
        train.setPos(spawnX, spawnY, spawnZ);

        // Vai em alta velocidade na direção que o Golem estava olhando (Velocidade 2.0 = Muito rápido!)
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

        level.playSound(null, player.blockPosition(), GOLEM_SUPREME_SOUND.get(), net.minecraft.sounds.SoundSource.PLAYERS, 2.0F, 0.9F);
    }
}