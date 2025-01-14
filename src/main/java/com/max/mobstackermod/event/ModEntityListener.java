package com.max.mobstackermod.event;

import com.max.mobstackermod.MobStackerMod;
import com.max.mobstackermod.config.ServerConfig;
import com.max.mobstackermod.data.StackedEntityHandler;
import com.max.mobstackermod.data.StackedEntityNameHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static com.max.mobstackermod.data.StackMobComponents.STACKED_ENTITIES;
import static com.max.mobstackermod.data.StackMobComponents.STACKED_NAMEABLE;


@EventBusSubscriber(value = Dist.DEDICATED_SERVER, modid = MobStackerMod.MOD_ID)
public class ModEntityListener  {

    public static Scheduler scheduler;


    /**
     * Called when the server has started. Initializes the scheduler.
     *
     * @param event The ServerStartedEvent
     */
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        scheduler = new Scheduler(event.getServer());
    }


    // SERVER EVENT
    @SubscribeEvent
    public static void onEndTick(ServerTickEvent.Post event) {
        scheduler.tick();

        ServerLevel world = event.getServer().getLevel(Level.OVERWORLD);

        ArrayList<LivingEntity> applicableEntities = getApplicableEntities(world);

        if (applicableEntities.isEmpty()) {
            return;
        }

        // Selects a number of entities randomly based on the config's processing rate
        ThreadLocalRandom.current().ints(ServerConfig.processingRate, 0, applicableEntities.size())
                .forEach(i -> {
                    LivingEntity entity = applicableEntities.get(i);
                    findEntitiesAndTryToMerge(world, entity, applicableEntities);
                });
    }

    /**
     * Merges entities together if they are applicable. This is the main event logic for the mod.
     * This checks various entity properties to determine if they are eligible for stacking, as determined
     * by the ServerConfig.
     *
     * @param world              The currently loaded world
     * @param entity             The entity to check for stacking
     * @param applicableEntities A list of entities that are applicable for stacking
     */
    private static void findEntitiesAndTryToMerge(ServerLevel world, LivingEntity entity, ArrayList<LivingEntity> applicableEntities) {
        if (entity.tickCount < ServerConfig.processDelay) {
            return;
        } else if (entity instanceof TamableAnimal tamableAnimalNearby && tamableAnimalNearby.isTame()) {
            return;
        } else if (!ServerConfig.stackBees && entity instanceof Bee) {
            return;
        } else if (entity instanceof Bee bee && bee.hasNectar()) {
            return;
        } else if (entity.isRemoved()) {
            return;
        }

        if (!entity.hasData(STACKED_ENTITIES) && entity.hasData(STACKED_NAMEABLE)) {
            return;
        }

        StackedEntityHandler mainEntityContainer = StackedEntityHandler.getOrInitStackedEntityHandler(entity);

        List<Entity> nearby = findEntitiesAroundMainEntity(world, entity, applicableEntities);

        if (nearby.size() <= ServerConfig.mobNeededToStack && !entity.hasData(STACKED_ENTITIES)) {
            return;
        }

        AtomicInteger nearbyAdded = new AtomicInteger();

        nearby.forEach((Entity nearbyEntity) -> {
            if (nearbyEntity.tickCount < ServerConfig.processDelay && !(nearbyEntity instanceof ItemEntity)) {
                return;
            }

            // Serialize entity
            if (
                    !entity.hasData(STACKED_ENTITIES) &&
                    entity.hasData(STACKED_NAMEABLE) &&
                    !(nearbyEntity instanceof ItemEntity)
            ) return;

            if (checkEntityEligibility(entity, nearbyEntity)) return;

            if (mergesAndIncreasesSlimeSize(entity, nearbyEntity)) return;

            mergeEntity(nearbyEntity, mainEntityContainer);

            // Delete the entity
            nearbyEntity.remove(Entity.RemovalReason.DISCARDED);
            nearbyAdded.getAndIncrement();
        });

        if (nearbyAdded.get() == 0) {
            return;
        }

        entity.setData(STACKED_ENTITIES, mainEntityContainer);

        StackedEntityNameHandler mainEntityNameHandler = StackedEntityNameHandler.getOnInitEntityNameHandler(entity);
        entity.setData(STACKED_NAMEABLE, mainEntityNameHandler);

        int stackSize = mainEntityContainer.getStackedEntityTags().size() + 1;

        if (stackSize < 2) {
            return;
        }

        // Set the entity name and displayed stack size
        mainEntityNameHandler.setStackSize(stackSize);
    }

    private static @NotNull List<Entity> findEntitiesAroundMainEntity(ServerLevel world, LivingEntity entity, ArrayList<LivingEntity> applicableEntities) {
        int searchRadius = ServerConfig.stackSearchRadius;

        if (entity instanceof Slime slime) {
            searchRadius = Math.max(1, slime.getSize() / 5) * ServerConfig.stackSearchRadius;
        }

        return world.getEntities(
                entity,
                // create AABB centered on entity with radius 10
                new AABB(entity.getX() - searchRadius,
                        entity.getY() - searchRadius,
                        entity.getZ() - searchRadius,

                        entity.getX() + searchRadius,
                        entity.getY() + searchRadius,
                        entity.getZ() + searchRadius),
                e -> e.getClass().isInstance(entity) && e instanceof LivingEntity && applicableEntities.contains(e)
        );
    }

    private static void mergeEntity(Entity nearbyEntity, StackedEntityHandler mainEntityContainer) {
        if (nearbyEntity.hasData(STACKED_ENTITIES) && nearbyEntity.hasData(STACKED_NAMEABLE)) {
            StackedEntityHandler nearbyContainer = nearbyEntity.getData(STACKED_ENTITIES);
            mainEntityContainer.getStackedEntityTags().addAll(nearbyContainer.getStackedEntityTags());
            nearbyEntity.removeData(STACKED_ENTITIES);
            nearbyEntity.removeData(STACKED_NAMEABLE);
        }

        CompoundTag newTag = new CompoundTag();
        nearbyEntity.save(newTag);
        mainEntityContainer.getStackedEntityTags().add(newTag);
    }

    private static boolean mergesAndIncreasesSlimeSize(LivingEntity entity, Entity nearbyEntity) {
        if (entity instanceof Slime slime && nearbyEntity instanceof Slime nearbySlime && ServerConfig.increaseSlimeSize) {
            if (Math.abs(slime.getSize() - nearbySlime.getSize()) <= 1) {
                if (slime.getSize() < 40) {
                    if (slime.getSize() < nearbySlime.getSize()) {
                        slime.setSize(Math.min(40, nearbySlime.getSize() + 1), true);
                    } else {
                        slime.setSize(Math.min(40, slime.getSize() + 1), true);
                    }
                    nearbySlime.remove(Entity.RemovalReason.DISCARDED);
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    private static boolean checkEntityEligibility(LivingEntity entity, Entity nearbyEntity) {
        if (nearbyEntity instanceof LivingEntity nearbyLiving && entity instanceof LivingEntity parentLiving) {
            if (nearbyLiving.isBaby() != parentLiving.isBaby()) {
                return true;
            } else if (!ServerConfig.stackBabies && nearbyLiving.isBaby() && parentLiving.isBaby()) {
                return true;
            } else if (!ServerConfig.stackNonBabies && !nearbyLiving.isBaby() && !parentLiving.isBaby()) {
                return true;
            } else if (ServerConfig.requireLineOfSight &&
                    (!nearbyLiving.hasLineOfSight(parentLiving) || !parentLiving.hasLineOfSight(nearbyLiving))
            ) {
                return true;
            } else if (nearbyLiving.isDeadOrDying() || parentLiving.isDeadOrDying()) {
                return true;
            } else if (!ServerConfig.stackTamed &&
                    (nearbyEntity instanceof TamableAnimal tamableAnimalNearby && tamableAnimalNearby.isTame())
            ) {
                return true;
            } else if (
                    nearbyEntity instanceof TamableAnimal tamableAnimalNearby &&
                            entity instanceof TamableAnimal tamableAnimal &&
                            (tamableAnimalNearby.isTame() != tamableAnimal.isTame() ||
                            tamableAnimalNearby.getOwnerUUID() != tamableAnimal.getOwnerUUID())
            ) {
                return true;
            } else if (!parentLiving.getPassengers().isEmpty() || !nearbyLiving.getPassengers().isEmpty()) {
                return true;
            } else if (nearbyEntity instanceof Slime nearSlime && entity instanceof Slime parentSlime) {
                return nearSlime.getSize() != parentSlime.getSize();
            } else if (
                    nearbyEntity instanceof Sheep nearbySheep &&
                            entity instanceof Sheep parentSheep &&
                            (nearbySheep.isSheared() != parentSheep.isSheared() ||
                                    (ServerConfig.stackOnlySheepWithSameColor && nearbySheep.getColor() != parentSheep.getColor()))
            ) {
                return true;
            } else if (!ServerConfig.stackBees && nearbyEntity instanceof Bee) {
                return true;
            } else if (
                    (entity instanceof Animal animal &&
                            (animal.isInLove() || !animal.canFallInLove())) ||
                    (nearbyEntity instanceof Animal nearbyAnimal &&
                            (nearbyAnimal.isInLove() || !nearbyAnimal.canFallInLove()))
            ) {
                return true;
            } else return nearbyEntity instanceof Bee nearbyBee && nearbyBee.hasNectar();
        }
        if (nearbyEntity.getClass() != entity.getClass()) {
            throw new IllegalStateException("Entities are not of the same class");
        }
        return false;
    }

    /**
     * Gets all the entities that are applicable for stacking. Only applies to entities that are in loaded
     * and simulating chunks. Abides by parameters set by the user in the ServerConfig.
     *
     * @param world The world to get the entities from
     * @return A list of entities that are applicable for stacking
     */
    public static ArrayList<LivingEntity> getApplicableEntities(ServerLevel world) {
        ArrayList<LivingEntity> applicableEntities;

        if (ServerConfig.stackMobs) {
            applicableEntities = new ArrayList<>(
                    world.getEntities(EntityTypeTest.forClass(LivingEntity.class),
                            (LivingEntity e) -> e.isAlive() &&
                                    e.getType() != EntityType.PLAYER &&
                                    ServerConfig.livingEntityToStack.contains(e.getType()) &&
                                    (ServerConfig.stackVillagers || !(e instanceof Villager))
                    )
            );
        } else {
            applicableEntities = new ArrayList<>();
        }

        return applicableEntities;
    }
}
