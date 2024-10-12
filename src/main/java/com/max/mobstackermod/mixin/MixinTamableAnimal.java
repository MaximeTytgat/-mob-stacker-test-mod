package com.max.mobstackermod.mixin;

import com.max.mobstackermod.event.TamableAnimalEvent;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TamableAnimal.class)
public class MixinTamableAnimal {
    @Inject(
            at = @At("TAIL"),
            method = "tame(Lnet/minecraft/world/entity/player/Player;)V"
    )
    public void injectTame(Player player, CallbackInfo ci) {
        TamableAnimalEvent.onEntityTame((TamableAnimal) (Object) this, player);
    }
}
