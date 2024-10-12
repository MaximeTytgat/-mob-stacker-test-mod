package com.max.mobstackermod.event;

import com.max.mobstackermod.config.ServerConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Slime;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

import static com.max.mobstackermod.data.StackMobComponents.STACKED_ENTITIES;

public class SlimeEvent {

    /**
     * Called when a slime splits into smaller slimes.
     * Handles the split based on the config.
     * This is called for each slime that is created from the split.
     * Called by {@link com.max.mobstackermod.mixin.MixinSlime#injectRemove(Entity.RemovalReason, CallbackInfo, Slime)}
     *
     * @param split   The slime that died and was split
     * @param created The slime that was created from the split
     * @param reason  The reason the parent slime was removed
     */
    public static void onSlimeSplit(Slime split, Slime created, Entity.RemovalReason reason) {
        if (!ServerConfig.applyToSplitSlimes) {
            return;
        }

        if (Objects.equals(split.getLastDamageSource(), split.damageSources().genericKill())) {
            return;
        }

        if (reason.equals(Entity.RemovalReason.DISCARDED)) {
            return;
        }

        if (!split.hasData(STACKED_ENTITIES) || split.getData(STACKED_ENTITIES).isEmpty()) {
            return;
        }

        if (split.level().isClientSide()) {
            return;
        }

        created.getData(STACKED_ENTITIES).addAll(split.getData(STACKED_ENTITIES).getStackedEntityTags());

        created.getData(STACKED_ENTITIES)
                .applyConsumerToAll(
                        e -> ((Slime) e).setSize(created.getSize(), true),
                        (ServerLevel) created.level()
                );
    }

}
