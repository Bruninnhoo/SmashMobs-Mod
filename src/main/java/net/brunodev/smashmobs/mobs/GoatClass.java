package net.brunodev.smashmobs.mobs;
 
import net.brunodev.smashmobs.SmashMobs;
import net.brunodev.smashmobs.registration.ModAttachments;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
 
public class GoatClass implements ISmashMob {
 
    @Override
    public void equip(Player player) {
        player.getInventory().clearContent();
 
        player.getInventory().add(0, new ItemStack(SmashMobs.GOAT_DASH.get()));
        player.getInventory().add(1, new ItemStack(SmashMobs.GOAT_SWALLOW.get()));
        player.getInventory().add(8, new ItemStack(SmashMobs.GOAT_SUPREME.get()));
 
        player.setData(ModAttachments.MORPH_DATA, "minecraft:goat");
 
        player.level().playSound(null, player.blockPosition(), SoundEvents.GOAT_AMBIENT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    public void unequip(Player player) {
        player.getInventory().clearContent();
        var scaleAttr = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.SCALE);
        if (scaleAttr != null) scaleAttr.setBaseValue(1.0D);
    }
}