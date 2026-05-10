package net.brunodev.smashmobs.mobs;

import net.brunodev.smashmobs.SmashMobs;
import net.brunodev.smashmobs.registration.ModAttachments;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class ChickenClass implements ISmashMob {

    public void equip(Player player) {
        player.getInventory().clearContent();
        player.getInventory().add(0, new ItemStack(SmashMobs.CHICKEN_TRAP.get()));
        player.getInventory().add(1, new ItemStack(SmashMobs.CHICKEN_MACHINE_GUN.get()));
        player.getInventory().add(8, new ItemStack(SmashMobs.CHICKEN_SUPREME.get()));
        player.setData(ModAttachments.MORPH_DATA, "minecraft:chicken");

        // ESCALA NATIVA 1.21: Diminui o hitbox real do Player para parecer uma galinha!
        var scaleAttr = player.getAttribute(Attributes.SCALE);
        if (scaleAttr != null) {
            scaleAttr.setBaseValue(0.6D); // Pequenininha, mas clicável!
        }

        player.level().playSound(null, player.blockPosition(), SoundEvents.CHICKEN_AMBIENT, SoundSource.PLAYERS, 1.0F,
                1.0F);
    }

    @Override
    public void unequip(Player player) {
        player.getInventory().clearContent();

        // Reseta para o tamanho humano padrão
        var scaleAttr = player.getAttribute(Attributes.SCALE);
        if (scaleAttr != null) {
            scaleAttr.setBaseValue(1.0D);
        }
    }
}
