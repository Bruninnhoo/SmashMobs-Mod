package net.brunodev.smashmobs.mobs;

import net.brunodev.smashmobs.SmashMobs;
import net.brunodev.smashmobs.registration.ModAttachments;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class IronGolemClass implements ISmashMob {

    @Override
    public void equip(Player player) {
        player.getInventory().clearContent();

        // Slot 0: Puxão Magnético
        //player.getInventory().add(0, new ItemStack(SmashMobs.GOLEM_PULL.get()));

        // Slot 1: Tacar Bigorna
        player.getInventory().add(0, new ItemStack(SmashMobs.GOLEM_THROW_ANVIL.get()));
        player.getInventory().add(1, new ItemStack(SmashMobs.GOLEM_GRAB.get()));
        player.getInventory().add(2, new ItemStack(SmashMobs.GOLEM_SUPREME.get()));

        player.setData(ModAttachments.MORPH_DATA, "minecraft:iron_golem");

        // Toca um som de metal pesadão quando vira Golem
        player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.IRON_GOLEM_REPAIR, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.5F);
    }

    @Override
    public void unequip(Player player) {
        player.getInventory().clearContent();
    }
}