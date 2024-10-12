package com.max.mobstackermod.mixin;

import com.max.mobstackermod.event.LivingEntityEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {
    @Inject(at = @At("TAIL"), method = "die(Lnet/minecraft/world/damagesource/DamageSource;)V")
    public void injectDie(DamageSource source, CallbackInfo ci) {
        LivingEntityEvent.onEntityDeath((LivingEntity) (Object) this, source);
    }

    @Inject(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isDeadOrDying()Z", ordinal = 1),
            method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
    )
    public void injectHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntityEvent.onEntityDamaged((LivingEntity) (Object) this, source, amount);
    }

}
