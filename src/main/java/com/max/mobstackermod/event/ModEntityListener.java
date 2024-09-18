package com.max.mobstackermod.event;

import com.max.mobstackermod.MobStackerMod;
import com.max.mobstackermod.data.StackMobComponents;
import com.max.mobstackermod.data.EntityContainer;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;


@EventBusSubscriber(modid = MobStackerMod.MOD_ID)
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



//    @SubscribeEvent
//    public static void onEndTick(ServerTickEvent.Post event) {
////        scheduler.tick();
////
////        ServerLevel world = event.getServer().overworld();
////
////        ArrayList<Entity> applicableEntities = getApplicableEntities(world);
////
////        if (applicableEntities.isEmpty()) {
////            return;
////        }
////
////        // Selects a number of entities randomly based on the config's processing rate
////        ThreadLocalRandom.current().ints(/*config.processingRate*/ 10, 0, applicableEntities.size())
////                .forEach(i -> {
////                    Entity mainEntity = applicableEntities.get(i);
////                    EntityContainer mainEntityContainer = mainEntity.getData(StackMobComponents.STACKED_ENTITIES.get());
//////                    MobStackerMod.LOGGER.info("Main Entity Container of {}: {}", mainEntity.getType().getDescription().getString(), mainEntityContainer.getCount());
////
////                    if (mainEntity.hasData(StackMobComponents.MANA.get())) {
////                        int mana = mainEntity.getData(StackMobComponents.MANA.get());
////                        mainEntity.setCustomNameVisible(true);
////                        mainEntity.setCustomName(Component.empty().append(mana + "x " + mainEntity.getType().getDescription().getString()));
////                    }
////
////                    int searchRadius = /*config.stackSearchRadius*/ 5;
////
////                    if (mainEntity instanceof Slime slime) {
////                        searchRadius = Math.max(1, slime.getSize() / 5) * /*config.stackSearchRadius*/ 5;
////                    }
////
////                    // Finds nearby entities that are of the same type
////                    List<Entity> nearby = world.getEntities(
////                            mainEntity,
////                            // create AABB centered on entity with radius of searchRadius
////                            new AABB(mainEntity.getX() - searchRadius,
////                                    mainEntity.getY() - searchRadius,
////                                    mainEntity.getZ() - searchRadius,
////
////                                    mainEntity.getX() + searchRadius,
////                                    mainEntity.getY() + searchRadius,
////                                    mainEntity.getZ() + searchRadius),
////                            e -> e.getClass().isInstance(mainEntity) && applicableEntities.contains(e)
////                    );
////
////                    if (nearby.size() < 2 && mainEntity.getData(StackMobComponents.STACKED_ENTITIES.get()).getCount() == 0 && !(mainEntity instanceof ItemEntity)) {
////                        return;
////                    }
////
////                    EntityContainer entityContainer = mainEntity.getData(StackMobComponents.STACKED_ENTITIES.get());
////                    int initialCount = entityContainer.getEntityTagList().size();
////
////                    nearby.forEach(e -> {
////                        if (e.tickCount < /*config.processDelay*/ 200 && !(e instanceof ItemEntity)) {
////                            return;
////                        }
////
////                        if (e == mainEntity || e instanceof ItemEntity ) {
////                            return;
////                        }
////
////                        if (e instanceof LivingEntity nearbyEntity) {
////                            if (isEntityApplicable(nearbyEntity, mainEntity)) {
////                                MobStackerMod.LOGGER.info("mainEntity custom name tag: {}", mainEntity.getCustomName());
////                                MobStackerMod.LOGGER.info("Initial count: {}", initialCount);
////
////                                EntityContainer nearbyEntityEntityContainer = nearbyEntity.getData(StackMobComponents.STACKED_ENTITIES.get());
////
////                                NonNullList<CompoundTag> nearbyEntityStackedEntitiesList = nearbyEntityEntityContainer.getEntityTagList();
////                                if (!nearbyEntityStackedEntitiesList.isEmpty()) {
////                                    entityContainer.addEntityTags(nearbyEntityStackedEntitiesList);
////                                    nearbyEntityStackedEntitiesList.forEach(tag -> {
////                                        mainEntity.setData(StackMobComponents.MANA.get(), mainEntity.getData(StackMobComponents.MANA.get()) + 1);
////                                    });
////                                }
////                                nearbyEntityEntityContainer.reset();
////                                nearbyEntity.setData(StackMobComponents.STACKED_ENTITIES.get(), nearbyEntityEntityContainer);
////
////                                entityContainer.addEntity(nearbyEntity);
////                                mainEntity.setData(StackMobComponents.MANA.get(), mainEntity.getData(StackMobComponents.MANA.get()) + 1);
////
////                                nearbyEntity.remove(Entity.RemovalReason.DISCARDED);
////
////                                MobStackerMod.LOGGER.info("count: {}", entityContainer.getEntityTagList().size());
////                            }
////                        }
////                    });
////
////
////                    if (initialCount == entityContainer.getEntityTagList().size()) {
////                        return;
////                    }
////
////                    MobStackerMod.LOGGER.info("Stacked entities after: {}", entityContainer.getEntityTagList());
////
////                    // Update entity name
////                });
//
//    }

//    private static boolean isEntityApplicable(Entity nearbyEntity, Entity mainEntity) {
//        int mainEntityCount = mainEntity.getData(StackMobComponents.STACKED_ENTITIES.get()).getEntityTagListLength();
//        int nearbyEntityCount = nearbyEntity.getData(StackMobComponents.STACKED_ENTITIES.get()).getEntityTagListLength();
//        return nearbyEntity.getType() == mainEntity.getType() &&
//                nearbyEntityCount <= mainEntityCount;
//    }

    @SubscribeEvent
    public static void test(LivingDamageEvent.Post event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }

        LivingEntity mainEntity = event.getEntity();
        Level level = mainEntity.level();
        mainEntity.setHealth(mainEntity.getMaxHealth());

        List<CompoundTag> tags = mainEntity.getData(StackMobComponents.TAGS.get());
        MobStackerMod.LOGGER.info("Tags: {}", tags.size());
        CompoundTag tag = EntityContainer.serializeEntity(mainEntity);
        MobStackerMod.LOGGER.info("test tag: {}", tag.isEmpty());
        tags.add(tag);
        mainEntity.setData(StackMobComponents.TAGS.get(), tags);

//        int mana = mainEntity.getData(StackMobComponents.MANA.get());
//        MobStackerMod.LOGGER.info("Mana: {}", mana);
//        mainEntity.setData(StackMobComponents.MANA.get(), mana + 1);
//
//        EntityContainer mainEntityContainer = mainEntity.getData(StackMobComponents.STACKED_ENTITIES.get());
//        mainEntityContainer.addEntity(mainEntity);
//        mainEntity.setData(StackMobComponents.STACKED_ENTITIES.get(), mainEntityContainer);

//        int mana = mainEntity.getData(StackMobComponents.MANA.get());
//        MobStackerMod.LOGGER.info("Mana: {}", mana);
//        mainEntity.setData(StackMobComponents.MANA.get(), mana + 1);


//        EntityType<?> entityType = EntityType.byString(EntityType.getKey(mainEntity.getType()).toString()).orElse(null);
//        if (entityType != null && entityType.create(level) instanceof LivingEntity newEntity) {
//            EntityContainer mainEntityContainer = mainEntity.getData(StackMobComponents.STACKED_ENTITIES.get());
//            MobStackerMod.LOGGER.info("Main Entity has no data Container of {}: {}", EntityType.getKey(mainEntity.getType()), mainEntityContainer.getEntityTagListLength());
//
////            MobStackerMod.LOGGER.info("mainEntityContainer size: {}", mainEntityContainer.getEntityTagList().size());
//            mainEntityContainer.addEntity(newEntity);
//            mainEntity.setData(StackMobComponents.STACKED_ENTITIES.get(), mainEntityContainer);
//        }

//        if (mainEntity.hasData(StackMobComponents.STACKED_ENTITIES.get())) {
//            EntityContainer mainEntityContainer2 = mainEntity.getData(StackMobComponents.STACKED_ENTITIES.get());
//            mainEntity.setCustomName(Component.empty().append(mainEntityContainer2.getEntityTagListSize() + "x " + mainEntity.getType().getDescription().getString()));
//        }
    }

//    @SubscribeEvent
//    public static void onLivingDeath(LivingDeathEvent event) {
//        if (event.getEntity().level().isClientSide) {
//            return;
//        }
//
//        LivingEntity mainEntity = event.getEntity();
//        Level level = mainEntity.level();
//
//
//        List<CompoundTag> tags = mainEntity.getData(StackMobComponents.TAGS.get());
//        MobStackerMod.LOGGER.info("Tags: {}", tags);
//        tags.add(EntityContainer.serializeEntity(mainEntity));
//        mainEntity.setData(StackMobComponents.TAGS.get(), tags);
//
//        if (mainEntity.hasData(StackMobComponents.STACKED_ENTITIES.get())) {
//            EntityContainer mainEntityContainer = mainEntity.getData(StackMobComponents.STACKED_ENTITIES.get());
////            MobStackerMod.LOGGER.info("Main Entity Container of {}: {}", mainEntity.getType().getDescription().getString(), mainEntityContainer.getCount());
//
//            CompoundTag mainEntityTag = EntityContainer.serializeEntity(mainEntity);
//
//            EntityType<?> entityType = EntityType.byString(mainEntityTag.getString("id")).orElse(null);
//            if (entityType != null && entityType.create(level) instanceof LivingEntity newEntity) {
//                newEntity.setHealth(mainEntity.getMaxHealth());
//                newEntity.setPos(mainEntity.getX(), mainEntity.getY(), mainEntity.getZ());
//
//                // Add the mainEntity to the new entity stack
//                EntityContainer newEntityContainer = newEntity.getData(StackMobComponents.STACKED_ENTITIES.get());
//
//                // Add the rest of the entities from the main entity stack to the new entity stack
//                if (!mainEntityContainer.getEntityTagList().isEmpty()) {
////                    newEntityContainer.addEntityTags(mainEntityContainer.getEntityTagList());
//                    mainEntityContainer.getEntityTagList().forEach(tag -> {
//                        CompoundTag newTag = new CompoundTag();
//                        newTag.putString("uuid", UUID.randomUUID().toString());
//                        newEntityContainer.addEntityTag(newTag);
//                        mainEntity.setData(StackMobComponents.MANA.get(), mainEntity.getData(StackMobComponents.MANA.get()) + 1);
//                    });
//
//                    // Remove the main entity stack
//                    mainEntityContainer.reset();
//                }
//
////                CompoundTag newTag = new CompoundTag();
////                newTag.putString("uuid", UUID.randomUUID().toString());
////                newEntityContainer.addEntityTag(newTag);
////                mainEntity.setData(StackMobComponents.MANA.get(), mana + 1);
////
////                newEntity.setData(StackMobComponents.STACKED_ENTITIES.get(), newEntityContainer);
////                newEntity.setData(StackMobComponents.MANA.get(), mana);
//
//                // Spawn the new entity
//                mainEntity.level().addFreshEntity(newEntity);
//
//                // Remove the main entity
//                mainEntity.remove(Entity.RemovalReason.KILLED);
//            }
//        }
//    }

//    @SubscribeEvent
//    public static void onLivingDeath(LivingDeathEvent event) {
//        if (event.getEntity().level().isClientSide) {
//            return;
//        }
//
//        LivingEntity mainEntity = event.getEntity();
//        Level level = mainEntity.level();
//
//        int mana = mainEntity.getData(StackMobComponents.MANA.get());
//        MobStackerMod.LOGGER.info("Mana: {}", mana);
//
//        if (mainEntity.hasData(StackMobComponents.STACKED_ENTITIES.get())) {
//            EntityContainer mainEntityContainer = mainEntity.getData(StackMobComponents.STACKED_ENTITIES.get());
//            MobStackerMod.LOGGER.info("Main Entity Container of {}: {}", mainEntity.getType().getDescription().getString(), mainEntityContainer.getEntityTagList().size());
//
//            CompoundTag mainEntityTag = EntityContainer.serializeEntity(mainEntity);
//
//            EntityType<?> entityType = EntityType.byString(mainEntityTag.getString("id")).orElse(null);
//            if (entityType != null && entityType.create(level) instanceof LivingEntity newEntity) {
//                newEntity.setHealth(mainEntity.getMaxHealth());
//                newEntity.setPos(mainEntity.getX(), mainEntity.getY(), mainEntity.getZ());
//
//                // Add the mainEntity to the new entity stack
//                EntityContainer newEntityContainer = newEntity.getData(StackMobComponents.STACKED_ENTITIES.get());
//
//                // Add the rest of the entities from the main entity stack to the new entity stack
//                if (!mainEntityContainer.getEntityTagList().isEmpty()) {
//                    newEntityContainer.addEntityTags(mainEntityContainer.getEntityTagList());
//                    mainEntityContainer.getEntityTagList().forEach(tag -> {
//                        mainEntity.setData(StackMobComponents.MANA.get(), mainEntity.getData(StackMobComponents.MANA.get()) + 1);
//                    });
//
//                    // Remove the main entity stack
//                    mainEntityContainer.reset();
//                }
//
//                newEntityContainer.addEntityTag(EntityContainer.serializeEntity(mainEntity));
//                mainEntity.setData(StackMobComponents.MANA.get(), mainEntity.getData(StackMobComponents.MANA.get()) + 1);
//
//                newEntity.setData(StackMobComponents.STACKED_ENTITIES.get(), newEntityContainer);
//                newEntity.setData(StackMobComponents.MANA.get(), mana);
//
//                // Spawn the new entity
//                mainEntity.level().addFreshEntity(newEntity);
//
//                // Remove the main entity
//                mainEntity.remove(Entity.RemovalReason.KILLED);
//
//
////                MobStackerMod.LOGGER.info("Entity count: {}", newEntityContainer.getCount());
////                MobStackerMod.LOGGER.info("Tags: {}", newEntityContainer.getEntityTagList());
////                MobStackerMod.LOGGER.info("mainEntityContainer tags: {}", mainEntityContainer.getEntityTagList());
//            }
//        }
//
//
////        entity.serializeNBT()
////
////        if (!result.isEmpty()) {
////            EntityType.create(result, entity.level()).ifPresent(newEntity -> {
////                newEntity.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
////                entity.level().addFreshEntity(newEntity);
////            });
////        }
////
////        Integer count = entity.getData(StackMobComponents.STACKED_ENTITIES.get()).getCount();
////
////        MobStackerMod.LOGGER.info("Entity count: " + count);
////        MobStackerMod.LOGGER.info("Tags: " + entity.getData(StackMobComponents.STACKED_ENTITIES.get()).getTags());
////
////        if (count > 0) {
////            StackedEntities stackedEntities = entity.getData(StackMobComponents.STACKED_ENTITIES.get());
////            CompoundTag nextEntity = stackedEntities.getNextTag();
////            final CompoundTag tag = nextEntity.copy();
////
////            MobStackerMod.LOGGER.info("Next Entity: " + tag);
////
////            Entity newEntity = EntityType.create(tag, entity.level()).orElse(null);
////
////            MobStackerMod.LOGGER.info("Optional Entity: " + newEntity);
////
////            if (newEntity == null) {
////                return;
////            }
////
////            newEntity.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
////            entity.level().addFreshEntity(newEntity);
////
////            stackedEntities.sliceOne();
////            entity.setData(StackMobComponents.STACKED_ENTITIES.get(), stackedEntities);
////        }
//    }



    /**
     * Gets all the entities that are applicable for stacking. Only applies to entities that are in loaded
     * and simulating chunks. Abides by parameters set by the user in the config.
     *
     * @param world The world to get the entities from
     * @return A list of entities that are applicable for stacking
     */
    public static ArrayList<Entity> getApplicableEntities(ServerLevel world) {
        ArrayList<Entity> applicableEntities;

        if (/*config.stackMobs*/ true) {
            applicableEntities = new ArrayList<>(
                    world.getEntities(EntityTypeTest.forClass(LivingEntity.class),
                            e -> e.isAlive() &&
                                    e.getType() != EntityType.PLAYER
                                    /*&& config.stackVillagers || !(e instanceof Villager)*/
                    )
            );
        } else {
            applicableEntities = new ArrayList<>();
        }

        return applicableEntities;
    }

}
