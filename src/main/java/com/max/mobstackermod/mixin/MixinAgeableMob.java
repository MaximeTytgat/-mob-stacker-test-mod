package com.max.mobstackermod.mixin;

import com.max.mobstackermod.event.AgeableMobEvent;
import net.minecraft.world.entity.AgeableMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AgeableMob.class)
public class MixinAgeableMob {
    @Inject(
            at = @At("TAIL"),
            method = "ageBoundaryReached()V"
    )
    public void injectAgeBoundaryReached(CallbackInfo ci) {
        AgeableMobEvent.onEntityGrow((AgeableMob) (Object) this);
    }

}
