package com.max.mobstackermod.mixin;

import com.max.mobstackermod.event.SheepEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.common.IShearable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Sheep.class)
public class MixinSheep implements IShearable {
    @Inject(
            at = @At("TAIL"),
            method = "shear(Lnet/minecraft/sounds/SoundSource;)V"
    )
    public void injectMobInteract(SoundSource category, CallbackInfo ci) {
        SheepEvent.onSheepShear((Sheep) (Object) this);
    }

    @Inject(
            at = @At("TAIL"),
            method = "setSheared(Z)V"
    )
    public void injectSetSheared(boolean sheared, CallbackInfo ci) {
        if (!sheared) {
            SheepEvent.onSheepGrowWool((Sheep) (Object) this);
        }
    }

    @Inject(
            at = @At("TAIL"),
            method = "setColor(Lnet/minecraft/world/item/DyeColor;)V"
    )
    public void injectSetColor(DyeColor color, CallbackInfo ci) {
        SheepEvent.onDyeSheep((Sheep) (Object) this, color);
    }

}
