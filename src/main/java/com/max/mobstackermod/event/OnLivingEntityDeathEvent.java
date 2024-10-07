package com.max.mobstackermod.event;

import com.max.mobstackermod.MobStackerMod;
import com.max.mobstackermod.config.ServerConfig;
import com.max.mobstackermod.data.StackedEntityHandler;
import com.max.mobstackermod.data.StackedEntityNameHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.concurrent.ThreadLocalRandom;

import static com.max.mobstackermod.config.EnumDeathHandlingAction.*;
import static com.max.mobstackermod.data.StackMobComponents.STACKED_ENTITIES;

@EventBusSubscriber(value = Dist.DEDICATED_SERVER, modid = MobStackerMod.MOD_ID)
public class OnLivingEntityDeathEvent {

    @SubscribeEvent
    public static void handleEvent(LivingDeathEvent event) {
        LivingEntity died = event.getEntity();
        DamageSource source = event.getSource();
        int partialTicks = died.tickCount - 5;

        if (!died.hasData(STACKED_ENTITIES)) return;
        StackedEntityHandler diedStackedEntityHandler = died.getData(STACKED_ENTITIES);

        if (source.equals(died.damageSources().genericKill()) || diedStackedEntityHandler.shouldSkipDeathEvents()) {
            return;
        }

        int countEntity = diedStackedEntityHandler.getSize();
        if (countEntity < 1) {
            return;
        }

        MobStackerMod.LOGGER.info("Entity died EVENT, entity name: {}", event.getEntity().getName().getString());
        if (ServerConfig.deathAction == ALL) {
            diedStackedEntityHandler.dropLootAndRemoveManyEntity((ServerLevel) died.level(), source, diedStackedEntityHandler.getSize(), died.getPosition(partialTicks));
        } else if (ServerConfig.deathAction == RANDOM) {
            LivingEntity slicedEntity = diedStackedEntityHandler.sliceOne(died.level(), died.getPosition(partialTicks));

            if (slicedEntity.hasData(STACKED_ENTITIES)) {
                StackedEntityHandler slicedEntityStackedEntityHandler = slicedEntity.getData(STACKED_ENTITIES);

                int randomIndex = ThreadLocalRandom.current().nextInt(Math.max(slicedEntityStackedEntityHandler.getSize() - 1, 1));
                slicedEntityStackedEntityHandler.dropLootAndRemoveManyEntity((ServerLevel) slicedEntity.level(), source, randomIndex, slicedEntity.getPosition(partialTicks));

                StackedEntityNameHandler nameHandler = StackedEntityNameHandler.getOnInitEntityNameHandler(slicedEntity);
                nameHandler.setStackSize(slicedEntityStackedEntityHandler.getSize() + 1);
            }

        } else if (ServerConfig.deathAction == SLICE) {
            diedStackedEntityHandler.sliceOne(died.level(), died.getPosition(partialTicks));
        } else if (ServerConfig.deathAction == BY_DAMAGE) {
            LivingEntity slicedEntity = diedStackedEntityHandler.sliceOne(died.level(), died.getPosition(partialTicks), true);

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
                        slicedEntity = slicedEntity.getData(STACKED_ENTITIES).sliceOne(slicedEntity.level(), died.getPosition(partialTicks), true);
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
}
