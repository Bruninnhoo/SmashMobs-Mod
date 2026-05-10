package net.brunodev.smashmobs.mobs;
 
import net.brunodev.smashmobs.SmashMobs;
import net.brunodev.smashmobs.registration.ModAttachments;
import net.brunodev.smashmobs.server.AbilityEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundSource;
 
public class CreeperClass implements ISmashMob {
 
    @Override
    public void equip(Player player) {
        player.getInventory().clearContent();
 
        player.getInventory().add(0, new ItemStack(SmashMobs.CREEPER_EXPLOSION.get()));
        player.getInventory().add(1, new ItemStack(SmashMobs.CREEPER_THROW_TNT.get()));
        player.getInventory().add(2, new ItemStack(SmashMobs.CREEPER_SUPREME.get()));
 
        player.setData(ModAttachments.MORPH_DATA, "minecraft:creeper");
 
        player.level().playSound(null,
                player.getX(), player.getY(), player.getZ(),
                SmashMobs.PICK_MOB_SOUND.get(),
                SoundSource.PLAYERS,
                1.0F, 
                1.0F
        );
    }
 
    @Override
    public void unequip(Player player) {
        player.getInventory().clearContent();
        AbilityEvents.CREEPER_SUPREMES.remove(player.getUUID());
    }
}