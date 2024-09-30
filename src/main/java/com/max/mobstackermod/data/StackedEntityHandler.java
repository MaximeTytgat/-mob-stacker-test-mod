package com.max.mobstackermod.data;

import com.max.mobstackermod.MobStackerMod;
import com.max.mobstackermod.config.ServerConfig;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static com.max.mobstackermod.data.StackMobComponents.STACKED_ENTITIES;
import static com.max.mobstackermod.data.StackMobComponents.STACKED_NAMEABLE;

public class StackedEntityHandler implements INBTSerializable<CompoundTag> {
    protected NonNullList<CompoundTag> stackedEntityTags;
    private static boolean skipDeathEvents = false;

    public StackedEntityHandler(int size) {
        stackedEntityTags = NonNullList.create();
    }

    public void addEntity(Entity entity) {
        entity.setUUID(UUID.randomUUID());
        CompoundTag entityTag = saveWithoutAttachment(new CompoundTag(), (LivingEntity) entity);
        stackedEntityTags.add(entityTag);
    }

    public Object sliceOne(Level level, LivingEntity provider) {
        if (stackedEntityTags.isEmpty()) {
            return provider;
        }

        CompoundTag tag = stackedEntityTags.removeFirst();
        Optional<Entity> optionalEntity = EntityType.create(tag, level);
        if (optionalEntity.isPresent()) {
            // Create the entity
            Entity entity = optionalEntity.get();
            entity.setPos(provider.getX(), provider.getY(), provider.getZ());
            entity.tickCount = 0;
            entity.invulnerableTime = 5;
            StackedEntityNameHandler nameHandler = entity.getData(STACKED_NAMEABLE);
            if (!nameHandler.isInitialized()) {
                nameHandler.setProvider(entity);
            }
            nameHandler.setStackSize(stackedEntityTags.size() + 1);

            // Move the stored entities to the new entity
            entity.getData(STACKED_ENTITIES)
                    .getStackedEntityTags()
                    .addAll(provider.getData(STACKED_ENTITIES).getStackedEntityTags());

            // Clear the provider's stored entities
            provider.getData(STACKED_ENTITIES).getStackedEntityTags().clear();

            // Spawn the new entity
            level.addFreshEntity(entity);

            // Set the new entity's stack size

            return entity;
        }

        return null;

    }

    public int getSize() {
        return stackedEntityTags.size();
    }

    public boolean isEmpty() {
        return stackedEntityTags.isEmpty();
    }


    public CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        ListTag nbtTagList = new ListTag();
        nbtTagList.addAll(stackedEntityTags);
        CompoundTag nbt = new CompoundTag();
        nbt.put("entities", nbtTagList);
        return nbt;
    }

    public void deserializeNBT(HolderLookup.@NotNull Provider provider, CompoundTag nbt) {
        ListTag tagList = nbt.getList("entities", 10);
        for(int i = 0; i < tagList.size(); ++i) {
            CompoundTag nbtTag = tagList.getCompound(i);
            stackedEntityTags.add(nbtTag);
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

    protected boolean validateStackLimit() {
        return stackedEntityTags.size() <= ServerConfig.mobStackLimit;
    }

    protected void onLoad() {
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
            compound.putShort("Fire", (short) entity.getRemainingFireTicks());
            compound.putShort("Air", (short) entity.getAirSupply());
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

    public NonNullList<CompoundTag> getStackedEntityTags() {
        return stackedEntityTags;
    }

    public void applyConsumerToAllWithoutProvider(Consumer<Entity> function, ServerLevel world) {
        HashMap<CompoundTag, CompoundTag> overwriteMap = new HashMap<>();

        stackedEntityTags.forEach(stackTag -> {
            Optional<Entity> entityWrapper = EntityType.create(stackTag, world);
            entityWrapper.ifPresent(entity -> {
                function.accept(entity);
                CompoundTag newTag = new CompoundTag();
                entity.save(newTag);
                overwriteMap.put(stackTag, newTag);
                entity.remove(Entity.RemovalReason.DISCARDED);
            });
        });

        overwriteMap.forEach((oldTag, newTag) -> {
            int insertIndex = stackedEntityTags.indexOf(oldTag);
            stackedEntityTags.set(insertIndex, newTag);
        });
    }

    public void applyConsumerToAll(Consumer<Entity> function, ServerLevel world, LivingEntity provider) {
        applyConsumerToAllWithoutProvider(function, world);

        function.accept(provider);
    }

    public void setSkipDeathEvents(boolean b) {
        skipDeathEvents = b;
    }

    public boolean shouldSkipDeathEvents() {
        return skipDeathEvents;
    }

}
