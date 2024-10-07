package com.max.mobstackermod.mixin;

import com.max.mobstackermod.event.ChickenEvent;
import net.minecraft.world.entity.animal.Chicken;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Chicken.class)
public class MixinChicken {
    @Inject(
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/entity/animal/Chicken;spawnAtLocation(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;"),
            method = "aiStep()V"
    )
    public void injectAiStep(CallbackInfo ci) {
        ChickenEvent.onChickenLayEgg((Chicken) (Object) this);
    }
}
