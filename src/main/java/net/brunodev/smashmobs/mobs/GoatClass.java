package net.brunodev.smashmobs.mobs;

import net.brunodev.smashmobs.SmashMobs;
import net.brunodev.smashmobs.registration.ModAttachments;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class GoatClass {

    public void equip(Player player) {
        // 1. Limpa as habilidades do mob anterior
        player.getInventory().clearContent();

        // 2. Equipa a Cabeçada (Dash) no Slot 0
        // Certifique-se de que GOAT_DASH está registrado no seu SmashMobs.java!
        player.getInventory().add(0, new ItemStack(SmashMobs.GOAT_DASH.get()));
        player.getInventory().add(1, new ItemStack(SmashMobs.GOAT_TONGUE.get()));

        // TODO no futuro: Adicionar a Habilidade 2 no slot 1
        // player.getInventory().add(1, new ItemStack(SmashMobs.GOAT_JUMP.get()));

        // 3. Define a "Capa" visual da Cabra
        player.setData(ModAttachments.MORPH_DATA, "minecraft:goat");

        // 4. Toca o som característico para confirmar a morfagem
        player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.GOAT_AMBIENT, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}