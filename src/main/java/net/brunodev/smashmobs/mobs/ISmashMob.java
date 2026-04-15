package net.brunodev.smashmobs.mobs;

import net.minecraft.world.entity.player.Player;

public interface ISmashMob {
    // Método que vai limpar o inventário e dar as habilidades
    void equip(Player player);

    // Método para quando a partida acabar ou ele trocar de mob
    void unequip(Player player);
}