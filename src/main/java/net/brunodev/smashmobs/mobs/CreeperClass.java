package net.brunodev.smashmobs.mobs;

import net.brunodev.smashmobs.SmashMobs;
import net.brunodev.smashmobs.registration.ModAttachments;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CreeperClass implements ISmashMob {

    @Override
    public void equip(Player player) {
        // 1. Limpa o inventário antigo do jogador (tira itens de outros mobs)
        player.getInventory().clearContent();

        // 2. Entrega as habilidades na ordem certa da Hotbar!
        // Slot 0 (Primeiro quadrado): A explosão/pulo
        player.getInventory().add(0, new ItemStack(SmashMobs.CREEPER_EXPLOSION.get()));

        // Slot 1 (Segundo quadrado): Arremessar TNT
        player.getInventory().add(1, new ItemStack(SmashMobs.CREEPER_THROW_TNT.get()));

        // Slot 2 (Terceiro quadrado): A Suprema!
        player.getInventory().add(2, new ItemStack(SmashMobs.CREEPER_SUPREME.get()));

        player.setData(ModAttachments.MORPH_DATA, "minecraft:creeper");

        player.level().playSound( null,
                player.getX(), player.getY(), player.getZ(),
                SmashMobs.PICK_MOB_SOUND.get(),
                net.minecraft.sounds.SoundSource.PLAYERS,
                1.0F, // Volume
                1.0F  // Pitch);
        );
    }

    @Override
    public void unequip(Player player) {
        // Quando ele deixar de ser Creeper, limpamos os itens e efeitos
        player.getInventory().clearContent();
        net.brunodev.smashmobs.server.AbilityEvents.CREEPER_SUPREMES.remove(player.getUUID());
    }
}