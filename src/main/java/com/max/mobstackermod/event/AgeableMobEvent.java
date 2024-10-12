package com.max.mobstackermod.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.max.mobstackermod.data.StackMobComponents.STACKED_ENTITIES;

public class AgeableMobEvent {

    /**
     * Called when a baby entity grows into an adult.
     * Automatically makes all stacked baby entities grow into adults.
     * Called by {@link com.max.mobstackermod.mixin.MixinAgeableMob#injectAgeBoundaryReached(CallbackInfo)}
     *
     * @param ageableMob The entity that grew
     */
    public static void onEntityGrow(AgeableMob ageableMob) {
        if (!ageableMob.hasData(STACKED_ENTITIES) || ageableMob.getData(STACKED_ENTITIES).isEmpty()) {
            return;
        }

        ageableMob.getData(STACKED_ENTITIES).applyConsumerToAll(
            e -> {
                e.setPos(ageableMob.position());
                ((AgeableMob) e).setAge(ageableMob.getAge());
            }, (ServerLevel) ageableMob.level()
        );
    }

}
