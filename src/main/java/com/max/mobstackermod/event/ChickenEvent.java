package com.max.mobstackermod.event;

import com.max.mobstackermod.config.ServerConfig;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.max.mobstackermod.data.StackMobComponents.STACKED_ENTITIES;

public class ChickenEvent {
    /**
     * Called when a chicken lays an egg. Handles item drops based on the config.
     * Called by {@link com.max.mobstackermod.mixin.MixinChicken#injectAiStep(CallbackInfo)}
     *
     * @param parent The chicken that laid the egg
     */

    public static void onChickenLayEgg(@NonNull Chicken parent) {
        if (!ServerConfig.applyToLiveDrops) {
            return;
        }

        if (!parent.hasData(STACKED_ENTITIES) || parent.getData(STACKED_ENTITIES).isEmpty()) {
            return;
        }

        int stackSize = parent.getData(STACKED_ENTITIES).getSize();
        int itemStackSize = Items.EGG.getDefaultMaxStackSize();

        while (stackSize > 0){
            parent.spawnAtLocation(new ItemStack(Items.EGG, Math.min(stackSize, itemStackSize)));
            stackSize -= Math.min(stackSize, itemStackSize);
        }
    }


}
