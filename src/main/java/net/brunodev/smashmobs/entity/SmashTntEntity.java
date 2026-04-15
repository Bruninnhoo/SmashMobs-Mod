package net.brunodev.smashmobs.entity;

import net.brunodev.smashmobs.SmashMobs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;

public class SmashTntEntity extends PrimedTnt {

    public SmashTntEntity(EntityType<? extends SmashTntEntity> type, Level level) {
        super(type, level);
        // Define o tempo para explodir (40 ticks = 2 segundos)
        this.setFuse(40);
    }

    // Criamos esse construtor para facilitar o spawn no Item
    public SmashTntEntity(Level level, double x, double y, double z) {
        this(SmashMobs.SMASH_TNT.get(), level);
        this.setPos(x, y, z);
        this.setFuse(30);
    }

    // O SEGREDO: Na 1.21, o método que faz o "BOOM" é o explode()
    @Override
    protected void explode() {
        if (!this.level().isClientSide()) {
            float radius = 4.0F; // Raio da explosão visual/dano
            float knockbackStrength = 10F; // O "multiplicador" de força da repulsão

            // 1. Faz a explosão padrão (Dano e Som)
            this.level().explode(
                    this,
                    this.getX(), this.getY(), this.getZ(),
                    radius,
                    false,
                    Level.ExplosionInteraction.NONE
            );

            // 2. O PULO DO GATO: Procurar entidades próximas para dar o "empurrão" extra
            // Criamos uma área de busca ao redor da TNT
            var area = this.getBoundingBox().inflate(radius);
            var nearbyEntities = this.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, area);

            for (net.minecraft.world.entity.LivingEntity entity : nearbyEntities) {
                // Não queremos empurrar a própria TNT se ela ainda existir

                // Calculamos a direção: (Posição da Entidade - Posição da TNT)
                double dx = entity.getX() - this.getX();
                double dy = entity.getY() - this.getY();
                double dz = entity.getZ() - this.getZ();
                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

                if (distance > 0) {
                    // Normalizamos o vetor e multiplicamos pela nossa força
                    dx /= distance;
                    dz /= distance;

                    // Aplicamos a velocidade (dx e dz para os lados, e um valor fixo para cima)
                    entity.addDeltaMovement(new net.minecraft.world.phys.Vec3(
                            dx * knockbackStrength,
                            0.8, // Força fixa para cima (estilo Smash Bros)
                            dz * knockbackStrength
                    ));

                    // Avisa o servidor que a física da entidade mudou e precisa ser sincronizada
                    entity.hurtMarked = true;
                }
            }

            this.discard();
        }
    }
}