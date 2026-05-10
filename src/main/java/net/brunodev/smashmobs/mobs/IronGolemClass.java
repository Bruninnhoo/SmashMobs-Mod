package net.brunodev.smashmobs.mobs;
 
import net.brunodev.smashmobs.SmashMobs;
import net.brunodev.smashmobs.registration.ModAttachments;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
 
public class IronGolemClass implements ISmashMob {
 
    @Override
    public void equip(Player player) {
        player.getInventory().clearContent();
 
        player.getInventory().add(0, new ItemStack(SmashMobs.GOLEM_THROW_ANVIL.get()));
        player.getInventory().add(1, new ItemStack(SmashMobs.GOLEM_GRAB.get()));
        player.getInventory().add(2, new ItemStack(SmashMobs.GOLEM_SUPREME.get()));
 
        player.setData(ModAttachments.MORPH_DATA, "minecraft:iron_golem");
 
        player.level().playSound(null, player.blockPosition(), SoundEvents.IRON_GOLEM_REPAIR, SoundSource.PLAYERS, 1.0F, 0.5F);
    }
 
    @Override
    public void unequip(Player player) {
        player.getInventory().clearContent();
    }
}