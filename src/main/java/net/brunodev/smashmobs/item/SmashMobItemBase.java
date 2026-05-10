package net.brunodev.smashmobs.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SmashMobItemBase extends Item {
    public SmashMobItemBase(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, Player player) {
        // IMPEDE QUE QUALQUER ITEM DO MOD SEJA DROPADO! Ele nunca sairá do inventário/slot.
        return false;
    }
}
