package com.max.mobstackermod;

import com.max.mobstackermod.config.ServerConfig;
import com.max.mobstackermod.data.StackMobComponents;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(MobStackerMod.MOD_ID)
public class MobStackerMod
{
    public static final String MOD_ID = "mobstackermod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MobStackerMod(IEventBus modEventBus, ModContainer modContainer)
    {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        ServerConfig.register(modContainer);

        StackMobComponents.register(modEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

    }

    @EventBusSubscriber(modid = MOD_ID, value = Dist.DEDICATED_SERVER)
    public static class ServerModEvents
    {
        @SubscribeEvent
        public static void serverAboutToStartEvent(ServerAboutToStartEvent event)
        {
            // Do something when the server starts
            LOGGER.info("HELLO from server starting");

            if (ServerConfig.stackMobs)
            {
                LOGGER.info("Stacking mobs is enabled");
            }
            else
            {
                LOGGER.info("Stacking mobs is disabled");
            }
        }
    }

    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
