package com.max.mobstackermod.event;

import com.max.mobstackermod.config.EnumModifyHandlingAction;
import com.max.mobstackermod.config.ServerConfig;
import com.max.mobstackermod.data.StackedEntityNameHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;

import static com.max.mobstackermod.data.StackMobComponents.STACKED_ENTITIES;
import static com.max.mobstackermod.data.StackMobComponents.STACKED_NAMEABLE;

public class TamableAnimalEvent {
    public static void onEntityTame(TamableAnimal tamableAnimal, Player player) {
        if (!tamableAnimal.hasData(STACKED_ENTITIES) || tamableAnimal.getData(STACKED_ENTITIES).isEmpty()) {
            return;
        }

        if (tamableAnimal.level().isClientSide()) {
            return;
        }

        if (ServerConfig.tamingAction == EnumModifyHandlingAction.ALL) {
            tamableAnimal.getData(STACKED_ENTITIES).applyConsumerToAll(
                    e -> ((TamableAnimal) e).tame(player), (ServerLevel) tamableAnimal.level()
            );
        } else if (ServerConfig.tamingAction == EnumModifyHandlingAction.SLICE) {
            tamableAnimal.getData(STACKED_ENTITIES).sliceOne(tamableAnimal.level(), tamableAnimal.position());
            tamableAnimal.removeData(STACKED_ENTITIES);
            StackedEntityNameHandler.getOnInitEntityNameHandler(tamableAnimal).clearCustomName();
            tamableAnimal.removeData(STACKED_NAMEABLE);
        }

    }
}
