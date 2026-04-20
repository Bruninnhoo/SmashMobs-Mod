package net.brunodev.smashmobs.mobs;

import net.brunodev.smashmobs.SmashMobs;
import net.brunodev.smashmobs.registration.ModAttachments;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ChickenClass {

    public void equip(Player player) {
        // 1. Limpa as habilidades do mob anterior
        player.getInventory().clearContent();

        // 2. Equipa as Habilidades da Galinha
        player.getInventory().add(0, new ItemStack(SmashMobs.CHICKEN_TRAP.get())); // Mina Terrestre
        player.getInventory().add(1, new ItemStack(SmashMobs.CHICKEN_MACHINE_GUN.get())); // Metralhadora

        // 3. Equipa a Suprema (Bombardeira) no Slot 8
        player.getInventory().add(8, new ItemStack(SmashMobs.CHICKEN_SUPREME.get()));

        // 4. Define a "Capa" visual
        player.setData(ModAttachments.MORPH_DATA, "minecraft:chicken");

        // 5. Som de galinha ao morphar
        player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.CHICKEN_AMBIENT,
                net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
