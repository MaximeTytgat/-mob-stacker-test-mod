package com.max.mobstackermod.event;

import com.max.mobstackermod.MobStackerMod;
import com.max.mobstackermod.config.ServerConfig;
import com.max.mobstackermod.data.StackedEntityHandler;
import com.max.mobstackermod.data.StackedEntityNameHandler;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
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
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static com.max.mobstackermod.config.EnumDeathHandlingAction.*;
import static com.max.mobstackermod.data.StackMobComponents.STACKED_ENTITIES;
import static com.max.mobstackermod.data.StackMobComponents.STACKED_NAMEABLE;


@EventBusSubscriber(value = Dist.DEDICATED_SERVER, modid = MobStackerMod.MOD_ID)
public class ModEntityListener  {

    private static Scheduler scheduler;


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

        ArrayList<Entity> applicableEntities = getApplicableEntities(world);

        if (applicableEntities.isEmpty()) {
            return;
        }

        // Selects a number of entities randomly based on the config's processing rate
        ThreadLocalRandom.current().ints(ServerConfig.processingRate, 0, applicableEntities.size())
                .forEach(i -> {
                    Entity entity = applicableEntities.get(i);
                    mergeEntities(world, entity, applicableEntities);
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
    private static void mergeEntities(ServerLevel world, Entity entity, ArrayList<Entity> applicableEntities) {
        if (entity.tickCount < ServerConfig.processDelay && !(entity instanceof ItemEntity)) {
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

        StackedEntityHandler mainEntityContainer = entity.getData(STACKED_ENTITIES);
        StackedEntityNameHandler mainEntityNameHandler = entity.getData(STACKED_NAMEABLE);
        if (!mainEntityNameHandler.isInitialized()) {
            mainEntityNameHandler.setProvider(entity);
        }

        if (
                !mainEntityNameHandler.customName.isEmpty()
                        && mainEntityContainer.isEmpty()
                        && !(entity instanceof ItemEntity)
        ) {
            return;
        }

        int searchRadius = ServerConfig.stackSearchRadius;

        if (entity instanceof Slime slime) {
            searchRadius = Math.max(1, slime.getSize() / 5) * ServerConfig.stackSearchRadius;
        }

        // Finds nearby entities that are of the same type
        List<Entity> nearby = world.getEntities(
                entity,
                // create AABB centered on entity with radius 10
                new AABB(entity.getX() - searchRadius,
                        entity.getY() - searchRadius,
                        entity.getZ() - searchRadius,

                        entity.getX() + searchRadius,
                        entity.getY() + searchRadius,
                        entity.getZ() + searchRadius),
                e -> e.getClass().isInstance(entity) && applicableEntities.contains(e)
        );

        if (nearby.size() < 2 && mainEntityContainer.isEmpty() && !(entity instanceof ItemEntity)) {
            return;
        }


        // Processes nearby entities and merges them if eligible
        nearby.forEach(nearbyEntity -> {
            if (nearbyEntity.tickCount < ServerConfig.processDelay && !(nearbyEntity instanceof ItemEntity)) {
                return;
            }

            // Serialize entity

            StackedEntityHandler nearbyContainer = nearbyEntity.getData(STACKED_ENTITIES);
            StackedEntityNameHandler nearbyNameHandler = nearbyEntity.getData(STACKED_NAMEABLE);
            if (!nearbyNameHandler.isInitialized()) {
                nearbyNameHandler.setProvider(nearbyEntity);
            }

            if (
                    !nearbyNameHandler.customName.isEmpty() &&
                            nearbyContainer.isEmpty() &&
                            !(nearbyEntity instanceof ItemEntity)
            ) {
                return;
            }

            // Check entity eligibility
            if (nearbyEntity instanceof LivingEntity nearbyLiving && entity instanceof LivingEntity parentLiving) {
                if (nearbyLiving.isBaby() != parentLiving.isBaby()) {
                    return;
                } else if (!ServerConfig.stackBabies && nearbyLiving.isBaby() && parentLiving.isBaby()) {
                    return;
                } else if (!ServerConfig.stackNonBabies && !nearbyLiving.isBaby() && !parentLiving.isBaby()) {
                    return;
                } else if (ServerConfig.requireLineOfSight &&
                        (!nearbyLiving.hasLineOfSight(parentLiving) || !parentLiving.hasLineOfSight(nearbyLiving))
                ) {
                    return;
                } else if (nearbyLiving.isDeadOrDying() || parentLiving.isDeadOrDying()) {
                    return;
                } else if (!ServerConfig.stackTamed &&
                        (nearbyEntity instanceof TamableAnimal tamableAnimalNearby && tamableAnimalNearby.isTame())
                ) {
                    return;
                } else if (
                        nearbyEntity instanceof TamableAnimal tamableAnimalNearby &&
                                entity instanceof TamableAnimal tamableAnimal &&
                                tamableAnimalNearby.isTame() != tamableAnimal.isTame()
                ) {
                    return;
                } else if (!parentLiving.getPassengers().isEmpty() || !nearbyLiving.getPassengers().isEmpty()) {
                    return;
                } else if (nearbyEntity instanceof Slime nearSlime && entity instanceof Slime parentSlime) {
                    if (nearSlime.getSize() != parentSlime.getSize()) {
                        return;
                    }
                } else if (
                        nearbyEntity instanceof Sheep nearbySheep &&
                                entity instanceof Sheep parentSheep &&
                                nearbySheep.isSheared() != parentSheep.isSheared()
                ) {
                    return;
                } else if (!ServerConfig.stackBees && nearbyEntity instanceof Bee) {
                    return;
                } else if (nearbyEntity instanceof Bee nearbyBee && nearbyBee.hasNectar()) {
                    return;
                }
            }

            if (nearbyEntity.getClass() != entity.getClass()) {
                throw new IllegalStateException("Entities are not of the same class");
            }

            // Merges same size slimes and increases their size
            if (entity instanceof Slime slime && nearbyEntity instanceof Slime nearbySlime && ServerConfig.increaseSlimeSize) {
                if (Math.abs(slime.getSize() - nearbySlime.getSize()) <= 1) {
                    if (slime.getSize() < 40) {
                        if (slime.getSize() < nearbySlime.getSize()) {
                            slime.setSize(Math.min(40, nearbySlime.getSize() + 1), true);
                        } else {
                            slime.setSize(Math.min(40, slime.getSize() + 1), true);
                        }
                        nearbySlime.remove(Entity.RemovalReason.DISCARDED);
                        return;
                    }
                } else {
                    return;
                }
            }

            // Merge already stored entities
            mainEntityContainer.getStackedEntityTags().addAll(nearbyContainer.getStackedEntityTags());
            nearbyContainer.getStackedEntityTags().clear();

            CompoundTag newTag = new CompoundTag();
            nearbyEntity.save(newTag);
            mainEntityContainer.getStackedEntityTags().add(newTag);

            nearbyNameHandler.setStackSize(1);

            // Delete the entity
            nearbyEntity.remove(Entity.RemovalReason.DISCARDED);
        });

        if (entity instanceof ItemEntity) {
            return;
        }

        int stackSize = mainEntityContainer.getStackedEntityTags().size() + 1;

        if (stackSize < 2) {
            return;
        }

        // Set the entity name and displayed stack size
        mainEntityNameHandler.setStackSize(stackSize);
    }

    /**
     * Gets all the entities that are applicable for stacking. Only applies to entities that are in loaded
     * and simulating chunks. Abides by parameters set by the user in the ServerConfig.
     *
     * @param world The world to get the entities from
     * @return A list of entities that are applicable for stacking
     */
    public static ArrayList<Entity> getApplicableEntities(ServerLevel world) {
        ArrayList<Entity> applicableEntities;


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

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {

        MobStackerMod.LOGGER.info("onEntityDeath");

        LivingEntity died = event.getEntity();
        DamageSource source = event.getSource();
        StackedEntityHandler diedStackedEntityHandler = died.getData(STACKED_ENTITIES);
        if (source.equals(died.damageSources().genericKill()) || diedStackedEntityHandler.shouldSkipDeathEvents()) {
            return;
        }

        int countEntity = diedStackedEntityHandler.getSize();
        if (countEntity < 1) {
            return;
        }

        if (ServerConfig.deathAction == ALL) {
            diedStackedEntityHandler.applyConsumerToAll(
                    e -> {
                        e.getData(STACKED_ENTITIES).setSkipDeathEvents(true);
                        e.setPos(died.getX(), died.getY(), died.getZ());
                        died.level().addFreshEntity(e);
                        e.hurt(source, Float.MAX_VALUE);
                    },
                    (ServerLevel) died.level(),
                    died
            );
            diedStackedEntityHandler.getStackedEntityTags().clear();
        } else if (ServerConfig.deathAction == RANDOM) {
            int randomIndex = ThreadLocalRandom.current().nextInt(Math.max(countEntity - 1, 1));
            LivingEntity slicedEntity = (LivingEntity) diedStackedEntityHandler.sliceOne(died.level(), died);

            StackedEntityHandler slicedEntityStackedEntityHandler = slicedEntity.getData(STACKED_ENTITIES);
            slicedEntityStackedEntityHandler.applyConsumerToAll(
                    (Entity e) -> e.getData(STACKED_ENTITIES).setSkipDeathEvents(true),
                    (ServerLevel) died.level(),
                    slicedEntity
            ); // Prevents infinite death event recursion

            NonNullList<CompoundTag> entityTags = slicedEntityStackedEntityHandler.getStackedEntityTags();
            for (int i = 0; i < randomIndex - 1; i++) {
                if (slicedEntityStackedEntityHandler.getStackedEntityTags().isEmpty()) {
                    break;
                }
                Optional<Entity> entityWrapper = EntityType.create(entityTags.getFirst(), died.level());
                entityWrapper.ifPresent(entity -> {
                    entity.getData(STACKED_ENTITIES).setSkipDeathEvents(true);
                    entity.setPos(died.getX(), died.getY(), died.getZ());
                    died.level().addFreshEntity(entity);
                    entity.hurt(source, Float.MAX_VALUE);
                });
                slicedEntityStackedEntityHandler.getStackedEntityTags().removeFirst();
            }

            slicedEntityStackedEntityHandler.applyConsumerToAll(
                    e -> e.getData(STACKED_ENTITIES).setSkipDeathEvents(false),
                    (ServerLevel) died.level(),
                    slicedEntity
            ); // Re-enable events

            slicedEntity.getData(STACKED_NAMEABLE).setStackSize(slicedEntityStackedEntityHandler.getSize()+1);
        } else if (ServerConfig.deathAction == SLICE) {
            diedStackedEntityHandler.sliceOne(died.level(), died);
        }
//        else if (ServerConfig.deathAction == BY_DAMAGE) {
//            float damage = diedStackedEntityHandler.getLastHurtValue();
//            damage -= diedStackedEntityHandler.getLastHpValue();
//
//            LivingEntity slicedEntity = (LivingEntity) diedStackedEntityHandler.sliceOne(died.level(), died);
//            slicedEntity.getData(STACKED_ENTITIES).applyConsumerToAll(
//                    e -> e.getData(STACKED_ENTITIES).setSkipDeathEvents(true),
//                    (ServerLevel) died.level(),
//                    slicedEntity
//            ); // Prevents infinite death event recursion
//
//            while (damage > 0f) {
//                float hpLeft = slicedEntity.getHealth();
//                slicedEntity.hurt(source, damage);
//                damage -= hpLeft;
//                if (slicedEntity.isDeadOrDying() && !slicedEntity.getComponent(STACKED_ENTITIES).getStackedEntities().isEmpty()) {
//                    slicedEntity = (LivingEntity) slicedEntity.getComponent(STACKED_ENTITIES).sliceOne(slicedEntity.level());
//                }
//
//                if (slicedEntity.getComponent(STACKED_ENTITIES).getStackedEntities().isEmpty()) {
//                    break;
//                }
//            }
//
//            slicedEntity.getComponent(STACKED_ENTITIES).applyConsumerToAll(
//                    e -> e.getComponent(STACKED_ENTITIES).setSkipDeathEvents(false),
//                    (ServerLevel) died.level()
//            ); // Re-enable events
//        }
    }


}
