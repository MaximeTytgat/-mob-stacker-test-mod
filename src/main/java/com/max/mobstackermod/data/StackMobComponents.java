package com.max.mobstackermod.data;

import com.max.mobstackermod.MobStackerMod;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;
import java.util.function.Supplier;

public class StackMobComponents {


    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MobStackerMod.MOD_ID);

    private static Codec<EntityContainer> EntityContainer;
//    public static final Supplier<AttachmentType<EntityContainer>> STACKED_ENTITIES = ATTACHMENT_TYPES.register(
//            "stacked_entities", () -> AttachmentType.builder(EntityContainer::new).serialize(EntityContainer).build()
//    );


    public static final Supplier<AttachmentType<List<CompoundTag>>> TAGS = ATTACHMENT_TYPES.register(
            "tags", () -> AttachmentType.<List<CompoundTag>>builder(() -> List.of()).serialize(CompoundTag.CODEC.listOf()).build()
            );


    public static final Supplier<AttachmentType<Integer>> MANA = ATTACHMENT_TYPES.register(
            "mana", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build()
    );


    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}