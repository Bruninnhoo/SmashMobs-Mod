package net.brunodev.smashmobs.mobs;
 
import net.brunodev.smashmobs.SmashMobs;
import net.brunodev.smashmobs.registration.ModAttachments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
 
public class SkeletonClass {
    public void equip(Player player) {
        player.getInventory().clearContent();
 
        player.getInventory().setItem(0, new ItemStack(SmashMobs.SKELETON_SNIPER.get()));
        player.getInventory().setItem(1, new ItemStack(SmashMobs.SKELETON_ULTIMATE.get()));
 
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.SKELETON_SKULL));
 
        player.setData(ModAttachments.MORPH_DATA, "minecraft:skeleton");
 
        player.level().playSound(null, player.blockPosition(), SoundEvents.SKELETON_AMBIENT, 
            SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
