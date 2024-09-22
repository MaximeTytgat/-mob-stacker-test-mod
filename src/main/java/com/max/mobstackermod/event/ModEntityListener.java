package com.max.mobstackermod.event;

import com.max.mobstackermod.MobStackerMod;
import com.max.mobstackermod.data.LivingEntityHandler;
import com.max.mobstackermod.data.StackMobComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.ArrayList;


@EventBusSubscriber(value = Dist.DEDICATED_SERVER, modid = MobStackerMod.MOD_ID)
public class ModEntityListener  {

    private static Scheduler scheduler;


//    /**
//     * Called when the server has started. Initializes the scheduler.
//     *
//     * @param event The ServerStartedEvent
//     */
//    @SubscribeEvent
//    public static void onServerStarted(ServerStartedEvent event) {
//        scheduler = new Scheduler(event.getServer());
//    }


// SERVER EVENT
//    @SubscribeEvent
//    public static void onEndTick(ServerTickEvent.Post event) {
//        scheduler.tick();
//    }
//    private static boolean isEntityApplicable(Entity nearbyEntity, Entity mainEntity) {
//        int mainEntityCount = mainEntity.getData(StackMobComponents.STACKED_ENTITIES.get()).getEntityTagListLength();
//        int nearbyEntityCount = nearbyEntity.getData(StackMobComponents.STACKED_ENTITIES.get()).getEntityTagListLength();
//        return nearbyEntity.getType() == mainEntity.getType() &&
//                nearbyEntityCount <= mainEntityCount;
//    }

    @SubscribeEvent
    public static void test(LivingDamageEvent.Post event) {
        MobStackerMod.LOGGER.info("LivingDamageEvent");

        LivingEntity mainEntity = event.getEntity();
        Level level = mainEntity.level();

        LivingEntityHandler mainEntityContainer = mainEntity.getData(StackMobComponents.HANDLER_COPY.get());
        MobStackerMod.LOGGER.info("Main Entity has no data Container of {}: {}",
                EntityType.getKey(mainEntity.getType()),
                mainEntityContainer.getEntityInSlot(0));

        boolean result = mainEntityContainer.insertLivingEntity(0, mainEntity);
        MobStackerMod.LOGGER.info("Inserting Entity: {}", result);
        mainEntity.setData(StackMobComponents.HANDLER_COPY.get(), mainEntityContainer);
    }

//                            @SubscribeEvent
//    public static void onLivingDeath(LivingDeathEvent event) {
//        if (event.getEntity().level().isClientSide) {
//            return;
//        }
//
//        LivingEntity mainEntity = event.getEntity();
//        Level level = mainEntity.level();
//
//        EntityContainer mainEntityContainer = mainEntity.getData(StackMobComponents.STACKED_ENTITIES.get());
//        MobStackerMod.LOGGER.info("Main Entity has no data Container of {}: {}", EntityType.getKey(mainEntity.getType()), mainEntityContainer.getEntityTagListLength());
//        mainEntityContainer.addEntity(mainEntity);
//        mainEntity.setData(StackMobComponents.STACKED_ENTITIES.get(), mainEntityContainer);
//
//
//        mainEntity.revive();
//        mainEntity.setHealth(mainEntity.getMaxHealth());
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
