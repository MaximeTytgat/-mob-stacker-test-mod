package com.max.mobstackermod.event;

import com.max.mobstackermod.config.EnumModifyHandlingAction;
import com.max.mobstackermod.config.ServerConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.max.mobstackermod.data.StackMobComponents.STACKED_ENTITIES;

public class SheepEvent {



    /**
     * Called when a sheep is sheared. Handles shearing based on the ServerConfig.
     * Called by {@link com.max.mobstackermod.mixin.MixinSheep#injectMobInteract(SoundSource, CallbackInfo)}
     *
     * @param sheared The sheep that was sheared
     */
    public static void onSheepShear(Sheep sheared) {
        if (!sheared.hasData(STACKED_ENTITIES) || sheared.getData(STACKED_ENTITIES).isEmpty()) {
            return;
        }

        if (sheared.level().isClientSide()) {
            return;
        }

        if (ServerConfig.shearAction == EnumModifyHandlingAction.ALL) {
            sheared.getData(STACKED_ENTITIES).applyConsumerToAll(
                    e -> {
                        e.setPos(sheared.getEyePosition());
                        ((Sheep) e).shear(SoundSource.PLAYERS);
                    }, (ServerLevel) sheared.level()
            );
        } else if (ServerConfig.shearAction == EnumModifyHandlingAction.SLICE) {
            sheared.getData(STACKED_ENTITIES).sliceOne(sheared.level(), sheared.position());
        }
    }


    /**
     * Called when a sheep changes color. Handles the change based on the ServerConfig.
     * Called by {@link com.max.mobstackermod.mixin.MixinSheep#injectSetColor(DyeColor, CallbackInfo)}
     *
     * @param dyed  The sheep that was dyed
     * @param color The color the sheep was dyed
     */
    public static void onDyeSheep(Sheep dyed, DyeColor color) {
        if (!dyed.hasData(STACKED_ENTITIES) || dyed.getData(STACKED_ENTITIES).isEmpty()) {
            return;
        }

        if (dyed.level().isClientSide()) {
            return;
        }

        if (ServerConfig.dyeAction == EnumModifyHandlingAction.ALL) {
            dyed.getData(STACKED_ENTITIES).applyConsumerToAll(
                    e -> ((Sheep) e).setColor(color), (ServerLevel) dyed.level()
            );
        } else if (ServerConfig.dyeAction == EnumModifyHandlingAction.SLICE) {
            dyed.getData(STACKED_ENTITIES).sliceOne(dyed.level(), dyed.position());
        }
    }


    /**
     * Called when a sheep grows back their wool.
     * Regrows the wool on all sheep in the stack.
     * Called by {@link com.max.mobstackermod.mixin.MixinSheep#injectSetSheared(boolean, CallbackInfo)}
     *
     * @param sheep The sheep that regrew their wool
     */
    public static void onSheepGrowWool(Sheep sheep) {
        if (!sheep.hasData(STACKED_ENTITIES) || sheep.getData(STACKED_ENTITIES).isEmpty()) {
            return;
        }

        if (sheep.level().isClientSide()) {
            return;
        }

        sheep.getData(STACKED_ENTITIES).applyConsumerToAll(
                e -> ((Sheep) e).setSheared(false), (ServerLevel) sheep.level()
        );
    }


}
