package com.max.mobstackermod.event;

import com.max.mobstackermod.config.ServerConfig;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.checkerframework.checker.nullness.qual.NonNull;

import static com.max.mobstackermod.data.StackMobComponents.STACKED_ENTITIES;

public class ChickenEvent {
    /**
     * Called when a chicken lays an egg. Handles item drops based on the config.
     *
     * @param parent The chicken that laid the egg
     */
    public static void onChickenLayEgg(@NonNull Chicken parent) {
        if (!ServerConfig.applyToLiveDrops) {
            return;
        }

        int stackSize = parent.getData(STACKED_ENTITIES).getSize();
        int itemStackSize = Items.EGG.getDefaultMaxStackSize();

        while (stackSize > 0){
            parent.spawnAtLocation(new ItemStack(Items.EGG, itemStackSize));
            stackSize -= itemStackSize;
        }
    }


}
