package com.max.mobstackermod.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.max.mobstackermod.event.SlimeEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Slime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Slime.class)
public class MixinSlime {
    @Inject(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Slime;moveTo(DDDFF)V"),
            method = "remove(Lnet/minecraft/world/entity/Entity$RemovalReason;)V"
    )
    public void injectRemove(Entity.RemovalReason reason, CallbackInfo ci, @Local(ordinal = 1) Slime slime) {
        SlimeEvent.onSlimeSplit((Slime) (Object) this, slime, reason);
    }
}
