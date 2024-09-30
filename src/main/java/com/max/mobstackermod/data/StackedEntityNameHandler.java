package com.max.mobstackermod.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class StackedEntityNameHandler implements INBTSerializable<CompoundTag> {
    private LivingEntity provider;

    @NonNull
    public String customName = "";

    private int stackSize = 1;

    public StackedEntityNameHandler() {
    }

    public StackedEntityNameHandler(LivingEntity entity) {
        this.provider = entity;
    }

    public void setProvider(@NonNull LivingEntity provider) {
        this.provider = provider;
    }

    public boolean isInitialized() {
        return provider != null;
    }

    public String getDefaultName() {
//        if (provider instanceof ItemEntity item) {
//            return capitalizeName(item.getItem().getDisplayName().getString());
//        }

        return capitalizeName(provider.getType().toShortString());
    }


    public String getName() {
        if (StringUtils.isBlank(customName)) {
            return stackSize > 1 ? getDefaultName() + " x" + stackSize : getDefaultName();
        } else {
            return stackSize > 1 ? customName + " x" + stackSize : customName;
        }
    }

    public void setCustomName(String customName) {
        this.customName = Objects.requireNonNullElse(customName, "");
        provider.setCustomName(
                MutableComponent.create(new PlainTextContents.LiteralContents(
                        getName()
                ))
        );
    }

    public void setStackSize(int stackSize) {
        this.stackSize = Math.max(stackSize, 1);
        setCustomName(customName);
    }


    public void readFromNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        tag.putString("StackMobCustomName", customName);
        tag.putInt("StackMobStackSize", stackSize);
    }


    public void writeToNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        customName = Objects.requireNonNullElse(tag.getString("StackMobCustomName"), "");
        stackSize = tag.getInt("StackMobStackSize");
    }

    public static String capitalizeName(String name) {
        return Arrays.stream(name.split(" "))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                .collect(Collectors.joining(" "));
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("customName", customName);
        compoundTag.putInt("stackSize", stackSize);
        return compoundTag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        customName = compoundTag.getString("customName");
        stackSize = compoundTag.getInt("stackSize");
    }
}
