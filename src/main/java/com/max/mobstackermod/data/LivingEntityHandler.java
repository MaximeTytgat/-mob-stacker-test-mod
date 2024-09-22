package com.max.mobstackermod.data;

import com.max.mobstackermod.MobStackerMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.Collections;
import java.util.List;

public class LivingEntityHandler implements INBTSerializable<CompoundTag> {
    protected List<LivingEntity> livingEntities;
    public static final LivingEntity EMPTY = null;

    public LivingEntityHandler() {
        this(1);
    }

    public LivingEntityHandler(int size) {
        this.livingEntities = Collections.singletonList((LivingEntity) null);
    }

    public LivingEntityHandler(List<LivingEntity> livingEntity) {
        this.livingEntities = livingEntity;
    }

    public void setSize(int size) {
        this.livingEntities = Collections.singletonList((LivingEntity) null);
    }

    public void setEntityInSlot(int slot, LivingEntity entity) {
        this.validateSlotIndex(slot);
        MobStackerMod.LOGGER.info("Setting entity in slot: {}", entity);
        this.livingEntities.set(slot, entity);
        this.onContentsChanged(slot);
    }

    public int getSlots() {
        return this.livingEntities.size();
    }

    public LivingEntity getEntityInSlot(int slot) {
        this.validateSlotIndex(slot);
        return (LivingEntity)this.livingEntities.get(slot);
    }

    public boolean insertLivingEntity(int slot, LivingEntity livingEntity) {
        // Valid slot, try to insert the entity into the slot if possible
        if (this.isEntityValid(slot, livingEntity)) {
            LivingEntity existingEntity = this.getEntityInSlot(slot);

            if (existingEntity == EMPTY) {
                this.setEntityInSlot(slot, livingEntity);
                return true;
            }
        }

        return false;
    }

    public LivingEntity extractLivingEntity(int slot) {
        if (this.livingEntities.get(slot) == EMPTY) {
            return null;
        } else {
            LivingEntity livingEntity = this.livingEntities.get(slot);
            this.livingEntities.set(slot, (LivingEntity) EMPTY);
            return livingEntity;
        }

    }

    public int getSlotLimit(int slot) {
        return 99;
    }

    public boolean isEntityValid(int slot, LivingEntity entity) {
        return true;
    }

    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        ListTag nbtTagList = new ListTag();

        CompoundTag nbt = new CompoundTag();
        nbt.put("entities", nbtTagList);
        nbt.putInt("Size", this.livingEntities.size());
        return nbt;
    }

    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        this.setSize(nbt.contains("Size", 3) ? nbt.getInt("Size") : this.livingEntities.size());
        ListTag tagList = nbt.getList("entities", 10);


        this.onLoad();
    }

    protected void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= this.livingEntities.size()) {
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + this.livingEntities.size() + ")");
        }
    }

    protected void onLoad() {
    }

    protected void onContentsChanged(int slot) {
    }
}
