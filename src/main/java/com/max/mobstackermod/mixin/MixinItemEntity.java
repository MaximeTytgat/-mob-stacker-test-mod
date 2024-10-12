package com.max.mobstackermod.mixin;

import com.max.mobstackermod.config.ServerConfig;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(ItemEntity.class)
public class MixinItemEntity {
    @Shadow
    private int age;

    @Shadow
    private int pickupDelay;

    @Shadow
    private UUID target;

    /**
     * Allows stacks to merge to infinite size
     */
    @Inject(
            at = @At(value = "HEAD"),
            method = "isMergable()Z",
            cancellable = true)
    public void injectIsMergable(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(
                ((ItemEntity) (Object) this).isAlive()
                        && this.pickupDelay != 32767
                        && this.age != -32768
                        && this.age < 6000
        );
    }

    /**
     * Allows stacks to merge to infinite size
     */
    @Inject(
            at = @At("HEAD"),
            method = "areMergable(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z",
            cancellable = true
    )
    private static void injectAreMergable(ItemStack stack1, ItemStack stack2, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(ItemStack.isSameItemSameComponents(stack1, stack2));
    }

    /**-
     * Allows stacks to merge to infinite size
     */
    @Inject(
            at = @At("HEAD"),
            method = "merge(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;I)Lnet/minecraft/world/item/ItemStack;",
            cancellable = true
    )
    private static void injectMerge(
            ItemStack stack1,
            ItemStack stack2,
            int maxCount,
            CallbackInfoReturnable<ItemStack> cir
    ) {
        maxCount = (ServerConfig.stackItems ? 36 : 1) * stack1.getMaxStackSize();
        int i = Math.min(maxCount - stack1.getCount(), stack2.getCount());
        ItemStack itemStack = stack1.copyWithCount(stack1.getCount() + i);
        stack2.shrink(i);
        cir.setReturnValue(itemStack);
    }

    @Inject(
            at = @At(value = "HEAD"),
            method = "playerTouch(Lnet/minecraft/world/entity/player/Player;)V",
            cancellable = true
    )
    public void injectPlayerTouch(Player player, CallbackInfo ci) {
        ItemEntity objThis = ((ItemEntity) (Object) this);
        if (!objThis.level().isClientSide) {
            ItemStack itemStack = objThis.getItem();
            Item item = itemStack.getItem();
            while (itemStack.getCount() > 0
                    && this.pickupDelay == 0
                    && (target == null || target.equals(player.getUUID()))
                    && player.getInventory().add(itemStack.copyWithCount(
                    Math.min(itemStack.getCount(), item.getDefaultMaxStackSize())
            ))
            ) {
                int decrement = Math.min(itemStack.getCount(), item.getDefaultMaxStackSize());
                player.take(objThis, decrement);

                itemStack.setCount(itemStack.getCount() - decrement);

                if (itemStack.isEmpty()) {
                    objThis.discard();
                }

                player.awardStat(Stats.ITEM_PICKED_UP.get(item), decrement);
                player.onItemPickup(objThis);
            }
        }

        ci.cancel();
    }
}
