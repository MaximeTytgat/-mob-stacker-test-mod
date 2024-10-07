package com.max.mobstackermod.config;

import com.max.mobstackermod.MobStackerMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = MobStackerMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ServerConfig
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // MOB STACKER MOB SERVER CONFIG OPTIONS
    private static final ModConfigSpec.BooleanValue STACK_MOBS = BUILDER
            .comment("Whether to stack mobs")
            .define("stackMobs", true);

    private static final ModConfigSpec.BooleanValue STACK_ITEMS = BUILDER
            .comment("Whether to stack items")
            .define("stackItems", true);

    private static final ModConfigSpec.BooleanValue REQUIRE_LINE_OF_SIGHT = BUILDER
            .comment("Whether to require line of sight to stack mobs")
            .define("requireLineOfSight", true);

    private static final ModConfigSpec.BooleanValue APPLY_TO_SPLIT_SLIMES = BUILDER
            .comment("Whether to apply stacking to split slimes")
            .define("applyToSplitSlimes", true);

    private static final ModConfigSpec.BooleanValue INCREASE_SLIME_SIZE = BUILDER
            .comment("Whether to increase the size of stacked slimes")
            .define("increaseSlimeSize", true);

    private static final ModConfigSpec.BooleanValue APPLY_TO_LIVE_DROPS = BUILDER
            .comment("Whether to apply stacking to live drops")
            .define("applyToLiveDrops", true);

    private static final ModConfigSpec.BooleanValue STACK_VILLAGERS = BUILDER
            .comment("Whether to stack villagers")
            .define("stackVillagers", false);

    private static final ModConfigSpec.BooleanValue STACK_TAMED = BUILDER
            .comment("Whether to stack tamed mobs")
            .define("stackTamed", false);

    private static final ModConfigSpec.BooleanValue STACK_BEES = BUILDER
            .comment("Whether to stack bees")
            .define("stackBees", true);

    private static final ModConfigSpec.BooleanValue STACK_BABIES = BUILDER
            .comment("Whether to stack baby mobs")
            .define("stackBabies", true);

    private static final ModConfigSpec.BooleanValue STACK_NON_BABIES = BUILDER
            .comment("Whether to stack non-baby mobs")
            .define("stackNonBabies", true);

    private static final ModConfigSpec.IntValue MOB_STACK_LIMIT = BUILDER
            .comment("The maximum number of mobs to stack")
            .defineInRange("mobStackLimit", 10, 1, 100);

    private static final ModConfigSpec.IntValue MOB_NEEDED_TO_STACK = BUILDER
            .comment("The number of mobs needed to stack")
            .defineInRange("mobNeededToStack", 2, 1, 100);

    private static final ModConfigSpec.IntValue PROCESSING_RATE = BUILDER
            .comment("The rate at which to process entities")
            .defineInRange("processingRate", 10, 1, 1000);

    private static final ModConfigSpec.IntValue PROCESS_DELAY = BUILDER
            .comment("The delay between processing entities")
            .defineInRange("processDelay", 200, 1, 10000);

    private static final ModConfigSpec.IntValue STACK_SEARCH_RADIUS = BUILDER
            .comment("The radius to search for entities to stack")
            .defineInRange("stackSearchRadius", 5, 0, 100);

    private static final ModConfigSpec.EnumValue<EnumDeathHandlingAction> DEATH_ACTION = BUILDER
            .comment("The action to take when a mob dies")
            .defineEnum("deathAction", EnumDeathHandlingAction.SLICE);

    private static final ModConfigSpec.EnumValue<EnumModifyHandlingAction> DYE_ACTION = BUILDER
            .comment("The action to take when a mob is dyed")
            .defineEnum("dyeAction", EnumModifyHandlingAction.SLICE);

    private static final ModConfigSpec.EnumValue<EnumModifyHandlingAction> RENAME_ACTION = BUILDER
            .comment("The action to take when a mob is renamed")
            .defineEnum("renameAction", EnumModifyHandlingAction.SLICE);

    private static final ModConfigSpec.EnumValue<EnumModifyHandlingAction> SHEAR_ACTION = BUILDER
            .comment("The action to take when a mob is sheared")
            .defineEnum("shearAction", EnumModifyHandlingAction.ALL);

    private static final ModConfigSpec.EnumValue<EnumModifyHandlingAction> BREED_ACTION = BUILDER
            .comment("The action to take when a mob breeds")
            .defineEnum("breedAction", EnumModifyHandlingAction.ALL);

    private static final ModConfigSpec.EnumValue<EnumModifyHandlingAction> TAMING_ACTION = BUILDER
            .comment("The action to take when a mob is tamed")
            .defineEnum("tamingAction", EnumModifyHandlingAction.SLICE);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> LIVING_ENTITY_STRINGS = BUILDER
            .comment("A list of living entities to stack")
            .defineListAllowEmpty(
                    "livingEntityToStack",
                    List.of("minecraft:cow", "minecraft:pig", "minecraft:sheep", "minecraft:chicken", "minecraft:bee"),
                    () -> "",
                    ServerConfig::validateLivingEntityName
            );

    private static boolean validateLivingEntityName(Object o) {
        if (o instanceof String string) {
            if (ResourceLocation.tryParse(string) != null) {
                return BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(string)) instanceof EntityType<?>;
            }
        }
        return false;
    }

    public static boolean stackMobs;
    public static boolean stackItems;
    public static boolean requireLineOfSight;
    public static boolean applyToSplitSlimes;
    public static boolean increaseSlimeSize;
    public static boolean applyToLiveDrops;
    public static boolean stackVillagers;
    public static boolean stackTamed;
    public static boolean stackBees;
    public static boolean stackBabies;
    public static boolean stackNonBabies;
    public static int mobStackLimit;
    public static int mobNeededToStack;
    public static int processingRate;
    public static int processDelay;
    public static int stackSearchRadius;
    public static EnumDeathHandlingAction deathAction;
    public static EnumModifyHandlingAction dyeAction;
    public static EnumModifyHandlingAction renameAction;
    public static EnumModifyHandlingAction shearAction;
    public static EnumModifyHandlingAction breedAction;
    public static EnumModifyHandlingAction tamingAction;
    public static Set<EntityType<?>> livingEntityToStack;

    public static void register(ModContainer modContainer) {
        ModConfigSpec config = BUILDER.build();
        modContainer.registerConfig(ModConfig.Type.SERVER, config);
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        stackMobs = STACK_MOBS.get();
        stackItems = STACK_ITEMS.get();
        requireLineOfSight = REQUIRE_LINE_OF_SIGHT.get();
        applyToSplitSlimes = APPLY_TO_SPLIT_SLIMES.get();
        increaseSlimeSize = INCREASE_SLIME_SIZE.get();
        applyToLiveDrops = APPLY_TO_LIVE_DROPS.get();
        stackVillagers = STACK_VILLAGERS.get();
        stackTamed = STACK_TAMED.get();
        stackBees = STACK_BEES.get();
        stackBabies = STACK_BABIES.get();
        stackNonBabies = STACK_NON_BABIES.get();
        mobStackLimit = MOB_STACK_LIMIT.get();
        mobNeededToStack = MOB_NEEDED_TO_STACK.get();
        processingRate = PROCESSING_RATE.get();
        processDelay = PROCESS_DELAY.get();
        stackSearchRadius = STACK_SEARCH_RADIUS.get();
        deathAction = DEATH_ACTION.get();
        dyeAction = DYE_ACTION.get();
        renameAction = RENAME_ACTION.get();
        shearAction = SHEAR_ACTION.get();
        breedAction = BREED_ACTION.get();
        tamingAction = TAMING_ACTION.get();
        livingEntityToStack = LIVING_ENTITY_STRINGS.get().stream()
                .map(entityName -> BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(entityName)))
                .collect(Collectors.toSet());
    }
}
