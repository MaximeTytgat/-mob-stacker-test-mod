package com.max.mobstackermod.data;

import com.max.mobstackermod.MobStackerMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.UUID;

public class EntityContainer implements INBTSerializable<CompoundTag> {
    private static final int MAX_ENTITIES = 100;
    public static ResourceKey<Level> levelKey;
    private static NonNullList<LivingEntity> entityTagList;

    private static final IllegalStateException FULL_EXCEPTION = new IllegalStateException("EntityContainer is full");

    public EntityContainer(IAttachmentHolder iAttachmentHolder) {
        entityTagList = NonNullList.createWithCapacity(MAX_ENTITIES);
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
        MobStackerMod.LOGGER.info("Entity: {}", serializeEntity(entity));
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
        CompoundTag nbt = new CompoundTag();
        entity.setUUID(UUID.randomUUID());
        entity.save(nbt);
        return nbt;
    }

    public static LivingEntity deserializeEntity(CompoundTag nbt, Level level) {
        EntityType<?> entityType = EntityType.byString(nbt.getString("id")).orElse(null);
        if (entityType != null && entityType.create(level) instanceof LivingEntity entity) {
            entity.load(nbt);
            return entity;
        }
        return null;
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        ListTag nbtTagList = new ListTag();

        for (LivingEntity livingEntity : entityTagList) {
            livingEntity.level();
            nbtTagList.add(livingEntity.saveWithoutId(new CompoundTag()));
        }

        CompoundTag nbt = new CompoundTag();
        nbt.put("Entities", nbtTagList);
        nbt.putInt("Size", entityTagList.size());
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, CompoundTag nbt) {
        this.setSize(nbt.contains("Size", 3) ? nbt.getInt("Size") : entityTagList.size());
        ListTag tagList = nbt.getList("Entities", 10);

        for(int i = 0; i < tagList.size(); ++i) {
            CompoundTag itemTags = tagList.getCompound(i);
            this.addEntityTag(itemTags);
        }
    }
}
