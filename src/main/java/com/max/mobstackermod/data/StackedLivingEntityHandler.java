package com.max.mobstackermod.data;

import com.max.mobstackermod.MobStackerMod;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.UUID;

public class StackedLivingEntityHandler implements INBTSerializable<CompoundTag> {
    protected NonNullList<LivingEntity> stackedLivingEntities;
    protected NonNullList<CompoundTag> livingEntityTags;
    private static final int MAX_ENTITIES = 10;

    public StackedLivingEntityHandler() {
        this(1);
    }

    public StackedLivingEntityHandler(int size) {
        livingEntityTags = NonNullList.create();
        stackedLivingEntities = NonNullList.create();
    }

    public StackedLivingEntityHandler(NonNullList<CompoundTag> livingEntityTag) {
        livingEntityTags = livingEntityTag;
    }

    public void setSize(int size) {
        livingEntityTags = NonNullList.create();
        stackedLivingEntities = NonNullList.create();
    }

    public void addEntity(LivingEntity entity) {
//        this.validateSlotIndex(slot);
        entity.setUUID(UUID.randomUUID());
        stackedLivingEntities.add(entity);
        MobStackerMod.LOGGER.info("livingEntities: {}", stackedLivingEntities);
        MobStackerMod.LOGGER.info("size: {}", stackedLivingEntities.size());
    }

    public int getSize() {
        return stackedLivingEntities.size();
    }

    public CompoundTag getEntityInSlot(int slot) {
//        this.validateSlotIndex(slot);
        return (CompoundTag) livingEntityTags.get(slot);
    }

    public boolean insertLivingEntity(LivingEntity livingEntity) {
        if (this.isTagValid(livingEntity)) {
            this.addEntity(livingEntity);
            return true;
        }

        return false;
    }

    public CompoundTag extractLivingEntity(int slot) {
        return (CompoundTag)livingEntityTags.get(slot);
    }

    public int getSlotLimit(int slot) {
        return 99;
    }

    public boolean isTagValid(LivingEntity entity) {
        return true;
    }

    public CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        ListTag nbtTagList = new ListTag();
        for (LivingEntity entity : stackedLivingEntities) {
            nbtTagList.add(saveWithoutAttachment(new CompoundTag(), entity));
        }
        CompoundTag nbt = new CompoundTag();
        nbt.put("entities", nbtTagList);
        return nbt;
    }

    public void deserializeNBT(HolderLookup.@NotNull Provider provider, CompoundTag nbt) {
        ListTag tagList = nbt.getList("entities", 10);
        for(int i = 0; i < tagList.size(); ++i) {
            CompoundTag nbtTag = tagList.getCompound(i);
            livingEntityTags.add(nbtTag);
        }
        this.onLoad();
    }

    public static LivingEntity deserializeEntity(CompoundTag nbt, Level level) {
        EntityType<?> entityType = EntityType.byString(nbt.getString("id")).orElse(null);
        if (entityType != null && entityType.create(level) instanceof LivingEntity entity) {
            entity.load(nbt);
            return entity;
        }
        MobStackerMod.LOGGER.error("Failed to deserialize entity");
        return null;
    }

    public void parseLivingEntityTags(Level level) {
        for (CompoundTag tag : livingEntityTags) {
            LivingEntity entity = deserializeEntity(tag, level);
            stackedLivingEntities.add(entity);
        }
        livingEntityTags.clear();
    }

    public boolean hasDataToParse() {
        return !livingEntityTags.isEmpty();
    }

    protected boolean validateStackLimit() {
        return livingEntityTags.size() <= MAX_ENTITIES;
    }

    protected void onLoad() {
    }

    protected void onContentsChanged(int slot) {
    }

    public CompoundTag saveWithoutAttachment(CompoundTag compound, LivingEntity entity) {
        try {
            compound.putString("id", EntityType.getKey(entity.getType()).toString());

            if (entity.isVehicle()) {
                compound.put("Pos", newDoubleList(entity.getVehicle().getX(), entity.getY(), entity.getVehicle().getZ()));
            } else {
                compound.put("Pos", newDoubleList(entity.getX(), entity.getY(), entity.getZ()));
            }

            Vec3 vec3 = entity.getDeltaMovement();
            compound.put("Motion", newDoubleList(vec3.x, vec3.y, vec3.z));
            compound.put("Rotation", newFloatList(entity.getYRot(), entity.getXRot()));
            compound.putFloat("FallDistance", entity.fallDistance);
            compound.putShort("Fire", (short)entity.getRemainingFireTicks());
            compound.putShort("Air", (short)entity.getAirSupply());
            compound.putBoolean("OnGround", entity.onGround());
            compound.putBoolean("Invulnerable", entity.isInvulnerable());
            compound.putInt("PortalCooldown", entity.getPortalCooldown());
            compound.putUUID("UUID", entity.getUUID());
            Component component = entity.getCustomName();
            if (component != null) {
                compound.putString("CustomName", Component.Serializer.toJson(component, entity.registryAccess()));
            }

            if (entity.isCustomNameVisible()) {
                compound.putBoolean("CustomNameVisible", entity.isCustomNameVisible());
            }

            if (entity.isSilent()) {
                compound.putBoolean("Silent", entity.isSilent());
            }

            if (entity.isNoGravity()) {
                compound.putBoolean("NoGravity", entity.isNoGravity());
            }

            if (entity.hasGlowingTag()) {
                compound.putBoolean("Glowing", true);
            }

            int i = entity.getTicksFrozen();
            if (i > 0) {
                compound.putInt("TicksFrozen", entity.getTicksFrozen());
            }

            if (!entity.getTags().isEmpty()) {
                ListTag listtag = new ListTag();
                Iterator var6 = entity.getTags().iterator();

                while(var6.hasNext()) {
                    String s = (String)var6.next();
                    listtag.add(StringTag.valueOf(s));
                }

                compound.put("Tags", listtag);
            }

            entity.addAdditionalSaveData(compound);
            if (entity.isVehicle()) {
                ListTag listTag1 = new ListTag();

                for (Entity entity2 : entity.getPassengers()) {
                    CompoundTag compoundtag = new CompoundTag();
                    if (entity2.saveAsPassenger(compoundtag)) {
                        listTag1.add(compoundtag);
                    }
                }

                if (!listTag1.isEmpty()) {
                    compound.put("Passengers", listTag1);
                }
            }

            return compound;
        } catch (Throwable var10) {
            Throwable throwable = var10;
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Saving entity NBT");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being saved");
            entity.fillCrashReportCategory(crashreportcategory);
            throw new ReportedException(crashreport);
        }
    }

    protected ListTag newDoubleList(double... numbers) {
        ListTag listtag = new ListTag();
        double[] var3 = numbers;
        int var4 = numbers.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            double d0 = var3[var5];
            listtag.add(DoubleTag.valueOf(d0));
        }

        return listtag;
    }

    protected ListTag newFloatList(float... numbers) {
        ListTag listtag = new ListTag();
        float[] var3 = numbers;
        int var4 = numbers.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            float f = var3[var5];
            listtag.add(FloatTag.valueOf(f));
        }

        return listtag;
    }
}
