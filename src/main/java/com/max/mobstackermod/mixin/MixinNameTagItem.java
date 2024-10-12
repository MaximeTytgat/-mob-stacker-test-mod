package com.max.mobstackermod.mixin;

import com.max.mobstackermod.event.LivingEntityEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.NameTagItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NameTagItem.class)
public class MixinNameTagItem {

    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;setCustomName(Lnet/minecraft/network/chat/Component;)V",
                    shift = At.Shift.AFTER
            ),
            method = "interactLivingEntity(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"
    )
    public void injectInteractLivingEntity(
            ItemStack stack,
            Player user,
            LivingEntity entity,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        LivingEntityEvent.onEntityRename(entity, stack.getHoverName());
    }
}
