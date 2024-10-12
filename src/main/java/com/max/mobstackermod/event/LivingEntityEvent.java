package com.max.mobstackermod.event;

import com.max.mobstackermod.MobStackerMod;
import com.max.mobstackermod.config.EnumModifyHandlingAction;
import com.max.mobstackermod.config.ServerConfig;
import com.max.mobstackermod.data.StackedEntityHandler;
import com.max.mobstackermod.data.StackedEntityNameHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.ThreadLocalRandom;

import static com.max.mobstackermod.config.EnumDeathHandlingAction.*;
import static com.max.mobstackermod.data.StackMobComponents.STACKED_ENTITIES;
import static com.max.mobstackermod.data.StackMobComponents.STACKED_NAMEABLE;

public class LivingEntityEvent {

    public static void onEntityDamaged(LivingEntity livingEntity, DamageSource source, float amount) {
        MobStackerMod.LOGGER.info("Entity damaged, damage amount: {}", amount);
        MobStackerMod.LOGGER.info("Entity damaged, HP: {}", livingEntity.getHealth());
        if (livingEntity.hasData(STACKED_ENTITIES)) {
            livingEntity.getData(STACKED_ENTITIES).setLastHurtValue(amount);
            livingEntity.getData(STACKED_ENTITIES).setLastHpValue(livingEntity.getHealth());
        }
    }

    public static void onEntityDeath(LivingEntity died, DamageSource source) {
        if (!died.hasData(STACKED_ENTITIES)) return;
        StackedEntityHandler diedStackedEntityHandler = died.getData(STACKED_ENTITIES);

        if (source.equals(died.damageSources().genericKill()) || diedStackedEntityHandler.shouldSkipDeathEvents()) {
            return;
        }

        int countEntity = diedStackedEntityHandler.getSize();
        if (countEntity < 1) {
            return;
        }

        MobStackerMod.LOGGER.info("Entity died EVENT, entity name: {}", died.getName().getString());
        if (ServerConfig.deathAction == ALL) {
            diedStackedEntityHandler.dropLootAndRemoveManyEntity((ServerLevel) died.level(), source, diedStackedEntityHandler.getSize(), died.position());
        } else if (ServerConfig.deathAction == RANDOM) {
            LivingEntity slicedEntity = diedStackedEntityHandler.sliceOne(died.level(), died.position());

            if (slicedEntity.hasData(STACKED_ENTITIES)) {
                StackedEntityHandler slicedEntityStackedEntityHandler = slicedEntity.getData(STACKED_ENTITIES);

                int randomIndex = ThreadLocalRandom.current().nextInt(Math.max(slicedEntityStackedEntityHandler.getSize() - 1, 1));
                slicedEntityStackedEntityHandler.dropLootAndRemoveManyEntity((ServerLevel) slicedEntity.level(), source, randomIndex, slicedEntity.position());

                StackedEntityNameHandler nameHandler = StackedEntityNameHandler.getOnInitEntityNameHandler(slicedEntity);
                nameHandler.setStackSize(slicedEntityStackedEntityHandler.getSize() + 1);
            }

        } else if (ServerConfig.deathAction == SLICE) {
            diedStackedEntityHandler.sliceOne(died.level(), died.position());
        } else if (ServerConfig.deathAction == BY_DAMAGE) {
            LivingEntity slicedEntity = diedStackedEntityHandler.sliceOne(died.level(), died.position(), true);

            if (slicedEntity.hasData(STACKED_ENTITIES)) {
                MobStackerMod.LOGGER.info("Sliced entity has stacked entities");
                float damage = slicedEntity.getData(STACKED_ENTITIES).getLastHurtValue();
                MobStackerMod.LOGGER.info("First Damage: {}", damage);
                damage -= slicedEntity.getData(STACKED_ENTITIES).getLastHpValue();

                MobStackerMod.LOGGER.info("Stack size before: {}", slicedEntity.getData(STACKED_ENTITIES).getSize());
                while (damage > 0f) {
                    MobStackerMod.LOGGER.info("Damage: {}", damage);
                    MobStackerMod.LOGGER.info("Stack size: {}", slicedEntity.getData(STACKED_ENTITIES).getSize());
                    float hpLeft = slicedEntity.getHealth();
                    slicedEntity.getData(STACKED_ENTITIES).setSkipDeathEvents(true);
                    slicedEntity.hurt(source, damage);
                    damage -= hpLeft;
                    if (slicedEntity.isDeadOrDying() && !slicedEntity.getData(STACKED_ENTITIES).getStackedEntityTags().isEmpty()) {
                        MobStackerMod.LOGGER.info("Sliced entity is dead");
                        slicedEntity = slicedEntity.getData(STACKED_ENTITIES).sliceOne(slicedEntity.level(), slicedEntity.position(), true);
                    }
                    MobStackerMod.LOGGER.info("Stack size after slice: {}", slicedEntity.getData(STACKED_ENTITIES).getSize());
                    slicedEntity.getData(STACKED_ENTITIES).setSkipDeathEvents(false);
                    if (slicedEntity.getData(STACKED_ENTITIES).isEmpty()) {
                        MobStackerMod.LOGGER.info("No more entities to slice");
                        break;
                    }
                }

                if (slicedEntity.hasData(STACKED_ENTITIES)) {
                    slicedEntity.getData(STACKED_ENTITIES).setSkipDeathEvents(false);
                    StackedEntityNameHandler nameHandler = StackedEntityNameHandler.getOnInitEntityNameHandler(slicedEntity);
                    nameHandler.setStackSize(slicedEntity.getData(STACKED_ENTITIES).getSize() + 1);
                }
            }
        }
    }

    /**
     * Called when an entity is renamed. Sets the custom name component of the entity.
     * Called by {@link com.max.mobstackermod.mixin.MixinNameTagItem}
     *
     * @param entity  The entity that was renamed
     * @param newName The text Component for the new name of the entity
     */
    public static void onEntityRename(@NonNull Entity entity, @NonNull Component newName) {
        if (entity instanceof LivingEntity livingEntity) {
            StackedEntityNameHandler nameHandler = StackedEntityNameHandler.getOnInitEntityNameHandler(livingEntity);
            nameHandler.setCustomName(newName.getString());
            nameHandler.setNameTagged(true);

            if (livingEntity.hasData(STACKED_ENTITIES)) {
                if (ServerConfig.renameAction == EnumModifyHandlingAction.SLICE) {
                    entity.getData(STACKED_ENTITIES).sliceOne(entity.level(), entity.position());
                } else if (ServerConfig.renameAction == EnumModifyHandlingAction.ALL) {
                    entity.getData(STACKED_ENTITIES).applyConsumerToAll(
                            e -> e.getData(STACKED_NAMEABLE).setCustomName(newName.getString()), (ServerLevel) entity.level()
                    );
                }
            }
        }
    }

}
