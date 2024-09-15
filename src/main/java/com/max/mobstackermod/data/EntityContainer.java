package com.max.mobstackermod.data;

import com.max.mobstackermod.MobStackerMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.UUID;

public class EntityContainer implements INBTSerializable<CompoundTag> {
    private static final int MAX_ENTITIES = 10;
    private static NonNullList<CompoundTag> entityTagList;
    protected byte cursor = 0;
    public static final CompoundTag EMPTY = new CompoundTag();

    private static final IllegalStateException FULL_EXCEPTION = new IllegalStateException("EntityContainer is full");

    public EntityContainer(NonNullList<LivingEntity> entities) {
        addEntities(entities);
    }

    public EntityContainer(IAttachmentHolder IAttachmentHolder) {
        entityTagList = NonNullList.createWithCapacity(MAX_ENTITIES);
    }

    public NonNullList<CompoundTag> getEntityTagList() {
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

    public CompoundTag getNextTag() {
        return entityTagList.getFirst();
    }

    public void sliceOne() {
        entityTagList.removeFirst();

    }

    private boolean isFull() {
        return cursor >= (MAX_ENTITIES+1);
    }

    public void addEntity(LivingEntity entity) {
        if (isFull()) throw FULL_EXCEPTION;
        entityTagList.add(serializeEntity(entity));
    }

    public void addEntities(NonNullList<LivingEntity> entities) {
        if (isFull()) throw FULL_EXCEPTION;
        entities.forEach(entity -> {
            entityTagList.add(serializeEntity(entity));
        });

    }

    public void addEntityTag(CompoundTag tag) {
        if (isFull()) throw FULL_EXCEPTION;
        entityTagList.add(tag);
    }

    public void addEntityTags(NonNullList<CompoundTag> tags) {
        if (isFull()) throw FULL_EXCEPTION;
        entityTagList.addAll(tags);
    }

    public LivingEntity removeLastEntity(Level level, Vec3 position) {
        if (cursor == 0) return null;
        return deserializeEntity(entityTagList.get(--cursor), level, position);
    }

    public void reset() {
        entityTagList.clear();
        cursor = 0;
    }

    public static CompoundTag serializeEntity(LivingEntity entity) {
        CompoundTag nbt = new CompoundTag();
        entity.setUUID(UUID.randomUUID());
        entity.save(nbt);
        return nbt;
    }

    public static LivingEntity deserializeEntity(CompoundTag nbt, Level level, Vec3 position) {
        EntityType<?> entityType = EntityType.byString(nbt.getString("id")).orElse(null);
        if (entityType != null && entityType.create(level) instanceof LivingEntity entity) {
            entity.load(nbt);
            entity.setPos(position.x, position.y, position.z);
            return entity;
        }
        return null;
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag compoundTag = new CompoundTag();
//        compoundTag.putInt("count", count);
//        ListTag listTag = new ListTag();
//        listTag.addAll(entityTagList);
//        compoundTag.put("tags", listTag);
        return compoundTag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
//        count = 0;
//        MobStackerMod.LOGGER.info("deserializeNBT tag: {}", compoundTag.get("tags"));
//        entityTagList.add(compoundTag.getCompound("tags"));
    }
}
