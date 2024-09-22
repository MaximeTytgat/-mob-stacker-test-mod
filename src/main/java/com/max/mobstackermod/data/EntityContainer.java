package com.max.mobstackermod.data;

import com.max.mobstackermod.MobStackerMod;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.UUID;

public class EntityContainer implements INBTSerializable<CompoundTag> {
    private static final int MAX_ENTITIES = 10;
    public static ResourceKey<Level> levelKey;
    private static NonNullList<LivingEntity> entityTagList;

    private static final IllegalStateException FULL_EXCEPTION = new IllegalStateException("EntityContainer is full");

    public EntityContainer(int size) {
        entityTagList = NonNullList.createWithCapacity(size);
    }

    public void setSize(int size) {
        entityTagList = NonNullList.withSize(size, (LivingEntity) null);
    }

    public void setLevelKey(ResourceKey<Level> levelKey) {
        EntityContainer.levelKey = levelKey;
    }

    public NonNullList<LivingEntity> getEntityTagList() {
        return entityTagList;
    }

    public int getEntityTagListSize() {
        return entityTagList.size();
    }

    public int getEntityTagListLength() {
        entityTagList.forEach(tag -> {
            MobStackerMod.LOGGER.info("Tag: {}", tag);
        });

        return entityTagList.size();
    }

    public LivingEntity getNextTag() {
        return entityTagList.getFirst();
    }

    public void sliceOne() {
        entityTagList.removeFirst();

    }

    private boolean isFull() {
        return entityTagList.size() >= MAX_ENTITIES;
    }

    public void addEntity(LivingEntity entity) {
        if (isFull()) throw FULL_EXCEPTION;
        entity.setUUID(UUID.randomUUID());
        entityTagList.add(entity);
        setLevelKey(entity.level().dimension());
    }

    public void addEntities(NonNullList<LivingEntity> entities) {
        if (isFull()) throw FULL_EXCEPTION;
        entities.forEach(entity -> entity.setUUID(UUID.randomUUID()));
        setLevelKey(entities.getFirst().level().dimension());
        entityTagList.addAll(entities);

    }

    public void addEntityTag(CompoundTag initialTag) {
        if (isFull()) throw FULL_EXCEPTION;
        MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
        assert server != null;
        Level level = server.getLevel(levelKey);
        entityTagList.add(deserializeEntity(initialTag, level));
    }

    public void addEntityTags(NonNullList<LivingEntity> livingEntities) {
        if (isFull()) throw FULL_EXCEPTION;
        entityTagList.addAll(livingEntities);
    }

    public LivingEntity removeLastEntity(Level level, Vec3 position) {
        if (entityTagList.isEmpty()) return null;
        LivingEntity entity = entityTagList.removeLast();
        if (entity == null) {
            MobStackerMod.LOGGER.error("Failed to deserialize entity");
        }
        return entity;
    }

    public void reset() {
        entityTagList.clear();
    }

    public static CompoundTag serializeEntity(LivingEntity entity) {
        entity.setUUID(UUID.randomUUID());
        return entity.saveWithoutId(new CompoundTag());
    }

    public static LivingEntity deserializeEntity(CompoundTag nbt, Level level) {
        EntityType<?> entityType = EntityType.by(nbt).orElse(null);
        if (entityType != null && entityType.create(level) instanceof LivingEntity entity) {
            entity.load(nbt);
            return entity;
        }
        return null;
    }

//    private static final Codec<CompoundTag> entityCodec = CompoundTag.CODEC.flatXmap(
//            nbt -> {
//                if (EntityType.by(nbt).isPresent()) {
//                    return DataResult.success(nbt);
//                } else {
//                    return DataResult.error(() -> "The nbt " + nbt + " does not contain a valid entity id.");
//                }
//            },
//            nbt -> DataResult.success(nbt)
//    );

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        ListTag nbtTagList = new ListTag();

//        for (int i = 0; i < entityTagList.size(); i++) {
//            LivingEntity livingEntity = entityTagList.get(i);
//            CompoundTag nbt = serializeEntity(livingEntity);
//            nbtTagList.set(i, nbt);
//        }

        CompoundTag nbt = new CompoundTag();
        nbt.put("Entities", nbtTagList);
        nbt.putInt("Size", entityTagList.size());
        nbt.putString("LevelKey", levelKey.location().toString());
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, CompoundTag nbt) {
        this.setSize(nbt.contains("Size", 3) ? nbt.getInt("Size") : entityTagList.size());
        ListTag tagList = nbt.getList("Entities", 10);

//        for(int i = 0; i < tagList.size(); ++i) {
//            CompoundTag itemTags = tagList.getCompound(i);
//            this.addEntityTag(itemTags);
//        }
    }
}
