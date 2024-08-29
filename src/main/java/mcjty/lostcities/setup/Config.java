package mcjty.lostcities.setup;



import dev.isxander.yacl3.api.ConfigCategory;

import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.YetAnotherConfigLib.Builder;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;

import dev.isxander.yacl3.api.controller.DropdownStringControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.config.ProfileSetup;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.*;

public class Config {

    public static final String CATEGORY_PROFILES = "profiles";
    public static final String CATEGORY_GENERAL = "general";
    public static final boolean DEBUG = false;

    public static String SPECIAL_BED_BLOCK;// = "minecraft:diamond_block";

    private static final String[] DEFAULT_DIMENSION_PROFILES = new String[] {
            "lostcities:lostcity=default"
    };
    private static List<String> DIMENSION_PROFILES;
    private static Map<RegistryKey<World>, String> dimensionProfileCache = null;

    // Profile as selected by the client
    public static String profileFromClient = null;
    public static String jsonFromClient = null;
    public static String SELECTED_PROFILE;
    public static String SELECTED_CUSTOM_JSON;
    public static int TODO_QUEUE_SIZE;
    public static boolean FORCE_SAPLING_GROWTH;

    private static final String[] DEF_AVOID_STRUCTURES = new String[] {
            "minecraft:mansion",
            "minecraft:jungle_pyramid",
            "minecraft:desert_pyramid",
            "minecraft:igloo",
            "minecraft:swamp_huts",
            "minecraft:pillager_outpost"
    };
    private static List<String> AVOID_STRUCTURES;
    private static final Set<Identifier> AVOID_STRUCTURES_SET = new HashSet<>();
    public static boolean AVOID_STRUCTURES_ADJACENT;
    public static boolean AVOID_VILLAGES_ADJACENT;
    public static boolean AVOID_FLATTENING;

    public static void reset() {
        profileFromClient = null;
        jsonFromClient = null;
        dimensionProfileCache = null;
    }

    public static void resetProfileCache() {
        dimensionProfileCache = null;
    }

    // @todo BAD
    public static void registerLostCityDimension(RegistryKey<World> type, String profile) {
        String profileForDimension = getProfileForDimension(type);
        if (profileForDimension == null) {
            dimensionProfileCache.put(type, profile);
        }
    }

    public static String getProfileForDimension(RegistryKey<World> type) {
        if (dimensionProfileCache == null) {
            dimensionProfileCache = new HashMap<>();
            for (String dp : DIMENSION_PROFILES) {
                String[] split = dp.split("=");
                if (split.length != 2) {
                    LostCities.getLogger().error("Bad format for config value: '{}'!", dp);
                } else {
                    RegistryKey<World> dimensionType = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(split[0]));
                    String profileName = split[1];
                    LostCityProfile profile = ProfileSetup.STANDARD_PROFILES.get(profileName);
                    if (profile != null) {
                        dimensionProfileCache.put(dimensionType, profileName);
                    } else {
                        LostCities.getLogger().error("Cannot find profile: {} for dimension {}!", profileName, split[0]);
                    }
                }
            }
            String selectedProfile = Config.SELECTED_PROFILE;
            if ("<CHECK>".equals(selectedProfile)) {
                if (Config.profileFromClient != null && !Config.profileFromClient.isEmpty()) {
                    Config.SELECTED_PROFILE = Config.profileFromClient;
                    if (Config.jsonFromClient != null && !Config.jsonFromClient.isEmpty()) {
                        Config.SELECTED_CUSTOM_JSON = Config.jsonFromClient;
                    } else {
                        Config.SELECTED_CUSTOM_JSON = "";
                    }
                    selectedProfile = Config.profileFromClient;
                } else {
                    Config.SELECTED_PROFILE = "";
                    selectedProfile = "";
                }
            }
            if (!selectedProfile.isEmpty()) {
                dimensionProfileCache.put(World.OVERWORLD, selectedProfile);
                String json = Config.SELECTED_CUSTOM_JSON;
                if (json != null && !json.isEmpty()) {
                    LostCityProfile profile = new LostCityProfile("customized", json);
                    if (!ProfileSetup.STANDARD_PROFILES.containsKey("customized")) {
                        ProfileSetup.STANDARD_PROFILES.put("customized", new LostCityProfile("customized", false));
                    }
                    ProfileSetup.STANDARD_PROFILES.get("customized").copyFrom(profile);
                }
            }

            String profile = getProfileForDimension(World.OVERWORLD);
            if (profile != null && !profile.isEmpty()) {
                if (ProfileSetup.STANDARD_PROFILES.get(profile).GENERATE_NETHER) {
                    dimensionProfileCache.put(World.NETHER, "cavern");
                }
            }
        }
        return dimensionProfileCache.get(type);
    }

    public static boolean isAvoidedStructure(Identifier id) {
        if (AVOID_STRUCTURES_SET.isEmpty()) {
            for (String s : AVOID_STRUCTURES) {
                AVOID_STRUCTURES_SET.add(Identifier.of(s));
            }
        }
        return AVOID_STRUCTURES_SET.contains(id);
    }



    public static Builder getBuiler() {
        return YetAnotherConfigLib.createBuilder()
            .title(Text.literal("Settings for Lost Cities"))

            // general settings page
            .category(ConfigCategory.createBuilder()
                .name(Text.literal(CATEGORY_GENERAL))
                // profiles section of settings
                .group(OptionGroup.createBuilder()
                    .name(Text.literal(CATEGORY_PROFILES))

                    // List and binding of the profile setting
                    .option(Option.<String>createBuilder()
                        .name(null)
                        .binding(
                            "<CHECK>", // the default value
                            () -> SELECTED_PROFILE, // a getter to get the current value from
                            newVal -> SELECTED_PROFILE = newVal)
                        .controller(DropdownStringControllerBuilder::create)
                        .build())
                    
                    // special bed block
                    .option(Option.<String>createBuilder()
                        .name(Text.literal("Special Bed block"))
                        .description(OptionDescription.of(Text.literal("Block to put underneath a bed so that it qualifies as a teleporter bed")))
                        .binding(
                            "minecraft:diamond_block", // the default value
                            () -> SPECIAL_BED_BLOCK, // a getter to get the current value from
                            newVal -> SPECIAL_BED_BLOCK = newVal) // setter
                        .controller(StringControllerBuilder::create)
                        .build())

                    // Selected Profile
                    .option(Option.<String>createBuilder()
                        .name(Text.literal("Selected Profile"))
                        .description(OptionDescription.of(Text.literal("The selected profile")))
                        .binding(
                            "<CHECK>", // the default value
                            () -> SELECTED_PROFILE, // a getter to get the current value from
                            newVal -> SELECTED_PROFILE = newVal) // setter
                        .controller(StringControllerBuilder::create)
                        .build())

                    // Selected custom JSON
                    .option(Option.<String>createBuilder()
                        .name(Text.literal("Selected custom JSON"))
                        .description(OptionDescription.of(Text.literal("The selected custom JSON")))
                        .binding(
                            "", // the default value
                            () -> SELECTED_CUSTOM_JSON, // a getter to get the current value from
                            newVal -> SELECTED_CUSTOM_JSON = newVal) // setter
                        .controller(StringControllerBuilder::create)
                        .build())


                    // to do queue size 
                    .option(Option.<Integer>createBuilder()
                        .name(Text.literal("ToDo queue size"))
                        .description(OptionDescription.of(Text.literal("The size of the todo queues for the lost city generator")))
                        .binding(
                            20, // the default value
                            () -> TODO_QUEUE_SIZE, // a getter to get the current value from
                            newVal -> TODO_QUEUE_SIZE = newVal) // setter
                        .controller(opt -> IntegerFieldControllerBuilder.create(opt)
                            .min(1)
                            .max(100000))
                        .build())

                    .option(Option.<Boolean>createBuilder()
                        .name(Text.literal("Force sapling grouth"))
                        .description(OptionDescription.of(Text.literal("If this is true then saplings will grow into trees during generation. This is more expensive")))
                        .binding(
                            true, // the default value
                            () -> FORCE_SAPLING_GROWTH, // a getter to get the current value from
                            newVal -> FORCE_SAPLING_GROWTH = newVal) // setter
                        .controller(BooleanControllerBuilder::create)
                        .build())

                    .option(Option.<Boolean>createBuilder()
                        .name(Text.literal("Avoid Structures adjacent"))
                        .description(OptionDescription.of(Text.literal("If true then also avoid generating the structures mentioned in 'avoidStructures' in chunks adjacent to the chunk with the structure")))
                        .binding(
                            true, // the default value
                            () -> AVOID_STRUCTURES_ADJACENT, // a getter to get the current value from
                            newVal -> AVOID_STRUCTURES_ADJACENT = newVal) // setter
                        .controller(BooleanControllerBuilder::create)
                        .build())
                    
                    .option(Option.<Boolean>createBuilder()
                        .name(Text.literal("Avoid Villages adjacent"))
                        .description(OptionDescription.of(Text.literal("If true then also avoid generating cities in chunks adjacent to the chunks with villages")))
                        .binding(
                            true, // the default value
                            () -> AVOID_VILLAGES_ADJACENT, // a getter to get the current value from
                            newVal -> AVOID_VILLAGES_ADJACENT = newVal) // setter
                        .controller(BooleanControllerBuilder::create)
                        .build())

                    .option(Option.<Boolean>createBuilder()
                        .name(Text.literal("Avoid Flattening"))
                        .description(OptionDescription.of(Text.literal("If true then avoid flattening the terrain around the city in case there was a structure that was avoided")))
                        .binding(
                            true, // the default value
                            () -> AVOID_FLATTENING, // a getter to get the current value from
                            newVal -> AVOID_FLATTENING = newVal) // setter
                        .controller(BooleanControllerBuilder::create)
                        .build())

                    
                    // Default dimenstion list option
                    .option(ListOption.<String>createBuilder()
                        .name(Text.literal("Default dimension profiles"))
                        .description(OptionDescription.of(Text.literal("A list of dimensions with associated city generation profiles (format <dimensionid>=<profilename>")))
                        .binding(
                            Arrays.asList(DEFAULT_DIMENSION_PROFILES), // the default value
                            () -> DIMENSION_PROFILES, // a getter to get the current value from
                            newVal -> DIMENSION_PROFILES = newVal)
                        .controller(StringControllerBuilder::create) // usual controllers, passed to every entry
                        .initial("") // when adding a new entry to the list, this is the initial value it has
                        .build())

                    // Avoid stuctures list option
                    .option(ListOption.<String>createBuilder()
                        .name(Text.literal("Default dimension profiles"))
                        .description(OptionDescription.of(Text.literal("List of structures to avoid when generating cities (for example to avoid generating a city in a woodland mansion)")))
                        .binding(
                            Arrays.asList(DEF_AVOID_STRUCTURES), // the default value
                            () -> AVOID_STRUCTURES, // a getter to get the current value from
                            newVal -> AVOID_STRUCTURES = newVal)
                        .controller(StringControllerBuilder::create) // usual controllers, passed to every entry
                        .initial("") // when adding a new entry to the list, this is the initial value it has
                        .build())
                    .build())
                .build());


        // OLD FORGE CODE FOR REFERANCE

        // DIMENSION_PROFILES = COMMON_BUILDER
        //         .comment("A list of dimensions with associated city generation profiles (format <dimensionid>=<profilename>")
        //         .defineList("dimensionsWithProfiles", Lists.newArrayList(Config.DEFAULT_DIMENSION_PROFILES), s -> s instanceof String);

        // SPECIAL_BED_BLOCK = SERVER_BUILDER
        //         .comment("Block to put underneath a bed so that it qualifies as a teleporter bed")
        //         .define("specialBedBlock", "minecraft:diamond_block");

        // SELECTED_PROFILE = SERVER_BUILDER.define("selectedProfile", "<CHECK>"); // Default is dummy value that tells the system to check in profileFromClient
        // SELECTED_CUSTOM_JSON = SERVER_BUILDER.define("selectedCustomJson", "");
        // TODO_QUEUE_SIZE = SERVER_BUILDER.comment("The size of the todo queues for the lost city generator").defineInRange("todoQueueSize", 20, 1, 100000);
        // FORCE_SAPLING_GROWTH = SERVER_BUILDER.comment("If this is true then saplings will grow into trees during generation. This is more expensive").define("forceSaplingGrowth", true);
        // AVOID_STRUCTURES = SERVER_BUILDER
        //         .comment("List of structures to avoid when generating cities (for example to avoid generating a city in a woodland mansion)")
        //         .defineList("avoidStructures", Lists.newArrayList(DEF_AVOID_STRUCTURES), s -> s instanceof String);
        // AVOID_STRUCTURES_ADJACENT = SERVER_BUILDER
        //         .comment("If true then also avoid generating the structures mentioned in 'avoidStructures' in chunks adjacent to the chunk with the structure")
        //         .define("avoidStructuresAdjacent", true);
        // AVOID_VILLAGES_ADJACENT = SERVER_BUILDER
        //         .comment("If true then also avoid generating cities in chunks adjacent to the chunks with villages")
        //         .define("avoidVillagesAdjacent", true);
        // AVOID_FLATTENING = SERVER_BUILDER
        //         .comment("If true then avoid flattening the terrain around the city in case there was a structure that was avoided")
        //         .define("avoidFlattening", true);

    }

}
