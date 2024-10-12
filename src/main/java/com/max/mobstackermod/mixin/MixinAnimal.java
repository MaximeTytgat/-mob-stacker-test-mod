package com.max.mobstackermod.mixin;

import com.max.mobstackermod.event.AnimalEvent;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Animal.class)
public class MixinAnimal {
    @Inject(
            at = @At("TAIL"),
            method = "setInLove(Lnet/minecraft/world/entity/player/Player;)V"
    )
    public void injectSetInLove(Player player, CallbackInfo ci) {
        AnimalEvent.onEntityLoveMode((Animal) (Object) this, player);
    }
}
