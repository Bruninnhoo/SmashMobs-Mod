package net.brunodev.smashmobs.mobs;

import net.brunodev.smashmobs.SmashMobs;
import net.brunodev.smashmobs.registration.ModAttachments;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SkeletonClass {
    public void equip(Player player) {
        // Limpa o inventário
        player.getInventory().clearContent();

        // Equipa itens do esqueleto
        player.getInventory().setItem(0, new ItemStack(SmashMobs.SKELETON_SNIPER.get()));
        player.getInventory().setItem(1, new ItemStack(SmashMobs.SKELETON_BOOMERANG.get()));
        player.getInventory().setItem(2, new ItemStack(SmashMobs.SKELETON_ULTIMATE.get()));

        // Seta o capacete para não pegar fogo de dia (ou só por estética)
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, new ItemStack(Items.SKELETON_SKULL));

        // Define a tag de Morph
        player.setData(ModAttachments.MORPH_DATA, "minecraft:skeleton");

        // Som de escolha
        player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.SKELETON_AMBIENT, 
            net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
