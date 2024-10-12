package com.max.mobstackermod.event;

import com.max.mobstackermod.config.EnumModifyHandlingAction;
import com.max.mobstackermod.config.ServerConfig;
import com.max.mobstackermod.data.StackedEntityNameHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;

import static com.max.mobstackermod.data.StackMobComponents.STACKED_ENTITIES;
import static com.max.mobstackermod.data.StackMobComponents.STACKED_NAMEABLE;
import static com.max.mobstackermod.event.ModEntityListener.scheduler;

public class AnimalEvent {

    /**
     * Called when an entity starts love mode.
     * Handles the breeding of the entity based on the config.
     * Creates a scheduler task to spawn the babies if needed.
     * Called by
     * {@link com.max.mobstackermod.mixin.MixinAnimal#injectSetInLove(Player, org.spongepowered.asm.mixin.injection.callback.CallbackInfo)}
     *
     * @param parent      The entity that started love mode
     * @param triggeredBy The player that triggered the love mode
     */
    public static void onEntityLoveMode(Animal parent, Player triggeredBy) {
        if (parent.getData(STACKED_ENTITIES).isEmpty()) {
            return;
        }

        if (parent.level().isClientSide()) {
            return;
        }

        if (ServerConfig.breedAction == EnumModifyHandlingAction.ALL) {
            int count = triggeredBy.getMainHandItem().getCount();
            int stackSize = parent.getData(STACKED_ENTITIES).getSize();
            int babies = Math.min(stackSize, count) / 2;

            scheduler.schedule(new Scheduler.Task(() -> {
                for (int i = 0; i < babies; i++) {
                    parent.spawnChildFromBreeding((ServerLevel) parent.level(), parent);
                }

                parent.getData(STACKED_ENTITIES).applyConsumerToAll(
                        e -> ((Animal) e).setAge(Animal.PARENT_AGE_AFTER_BREEDING), (ServerLevel) parent.level()
                );
            }, 40L));

            triggeredBy.getMainHandItem()
                    .setCount(triggeredBy.getMainHandItem().getCount() - Math.min(count, stackSize) + 1);
        } else if (ServerConfig.breedAction == EnumModifyHandlingAction.SLICE) {
            parent.getData(STACKED_ENTITIES).sliceOne(parent.level(), parent.position());
            parent.removeData(STACKED_ENTITIES);
            StackedEntityNameHandler.getOnInitEntityNameHandler(parent).clearCustomName();
            parent.removeData(STACKED_NAMEABLE);
        }

    }

}
