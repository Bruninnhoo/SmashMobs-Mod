package net.brunodev.smashmobs.mobs;
 
import net.brunodev.smashmobs.SmashMobs;
import net.brunodev.smashmobs.registration.ModAttachments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
 
public class SkeletonClass implements ISmashMob {
    @Override
    public void unequip(Player player) {
        player.getInventory().clearContent();
        var scaleAttr = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.SCALE);
        if (scaleAttr != null) scaleAttr.setBaseValue(1.0D);
    }
 
    @Override
    public void equip(Player player) {
        player.getInventory().clearContent();
 
        player.getInventory().setItem(0, new ItemStack(SmashMobs.SKELETON_SNIPER.get()));
        net.minecraft.world.item.Item[] startRewards = {
            SmashMobs.SKELETON_PREDATOR_MISSILE.get(),
            SmashMobs.SKELETON_AIRSTRIKE.get(),
            SmashMobs.SKELETON_SENTRY.get()
        };
        java.util.Random rnd = new java.util.Random();
        net.minecraft.world.item.Item startUlt = startRewards[rnd.nextInt(startRewards.length)];
        player.getInventory().setItem(1, new ItemStack(startUlt));
 
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.SKELETON_SKULL));
        player.containerMenu.broadcastChanges(); // SUPER IMPORTANTE: Força o cliente a ver os itens novos!
 
        player.setData(ModAttachments.MORPH_DATA, "minecraft:skeleton");
 
        player.level().playSound(null, player.blockPosition(), SoundEvents.SKELETON_AMBIENT, 
            SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
