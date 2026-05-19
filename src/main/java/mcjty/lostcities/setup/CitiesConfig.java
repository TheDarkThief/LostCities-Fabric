package mcjty.lostcities.setup;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.config.ProfileSetup;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.*;

@Modmenu(modId = "lostcities")
@Config(name = "my-config", wrapperName = "MyConfig")
public class CitiesConfig {

    public static final String CATEGORY_PROFILES = "profiles";
    public static final String CATEGORY_GENERAL = "general";
    public static final boolean DEBUG = false;

    public static String SPECIAL_BED_BLOCK; // = "minecraft:diamond_block";

    private static final String[] DEFAULT_DIMENSION_PROFILES =
            new String[] {
                "lostcities:lostcity=biosphere", "lostworlds:abyss=biosphere_caves",
            };
    private static List<? extends String> DIMENSION_PROFILES;
    private static Map<ResourceKey<Level>, String> dimensionProfileCache = null;

    // Profile as selected by the client
    public static String profileFromClient = null;
    public static String jsonFromClient = null;
    public static String SELECTED_PROFILE;
    public static String SELECTED_CUSTOM_JSON;
    public static int TODO_QUEUE_SIZE;
    public static boolean FORCE_SAPLING_GROWTH;
    public static int CACHE_CLEANUP_SECONDS;

    private static final String[] DEF_AVOID_STRUCTURES =
            new String[] {
                "minecraft:mansion",
                "minecraft:jungle_pyramid",
                "minecraft:desert_pyramid",
                "minecraft:igloo",
                "minecraft:swamp_huts",
                "minecraft:pillager_outpost"
            };
    private static List<? extends String> AVOID_STRUCTURES;
    private static Set<ResourceLocation> AVOID_STRUCTURES_SET = null;
    public static boolean AVOID_STRUCTURES_ADJACENT;
    public static boolean AVOID_VILLAGES;
    public static boolean AVOID_VILLAGES_ADJACENT;
    public static boolean AVOID_FLATTENING;
    public static boolean OPTIMIZED_HEIGHTMAP;
    public static int HEIGHT_SAMPLE_SIZE;

    public static void reset() {
        profileFromClient = null;
        jsonFromClient = null;
        dimensionProfileCache = null;
    }

    public static void resetProfileCache() {
        dimensionProfileCache = null;
    }

    // @todo BAD
    public static void registerLostCityDimension(ResourceKey<Level> type, String profile) {
        String profileForDimension = getProfileForDimension(type);
        if (profileForDimension == null) {
            dimensionProfileCache.put(type, profile);
        }
    }

    public static String getProfileForDimension(ResourceKey<Level> type) {
        if (dimensionProfileCache == null) {
            dimensionProfileCache = new HashMap<>();
            for (String dp : DIMENSION_PROFILES) {
                String[] split = dp.split("=");
                if (split.length != 2) {
                    LostCities.getLogger().error("Bad format for config value: '{}'!", dp);
                } else {
                    ResourceKey<Level> dimensionType =
                            ResourceKey.create(
                                    Registries.DIMENSION, ResourceLocation.parse(split[0]));
                    String profileName = split[1];
                    LostCityProfile profile = ProfileSetup.STANDARD_PROFILES.get(profileName);
                    if (profile != null) {
                        dimensionProfileCache.put(dimensionType, profileName);
                    } else {
                        LostCities.getLogger()
                                .error(
                                        "Cannot find profile: {} for dimension {}!",
                                        profileName,
                                        split[0]);
                    }
                }
            }
            String selectedProfile = CitiesConfig.SELECTED_PROFILE;
            if ("<CHECK>".equals(selectedProfile)) {
                if (CitiesConfig.profileFromClient != null
                        && !CitiesConfig.profileFromClient.isEmpty()) {
                    CitiesConfig.SELECTED_PROFILE = (CitiesConfig.profileFromClient);
                    if (CitiesConfig.jsonFromClient != null
                            && !CitiesConfig.jsonFromClient.isEmpty()) {
                        CitiesConfig.SELECTED_CUSTOM_JSON = (CitiesConfig.jsonFromClient);
                    } else {
                        CitiesConfig.SELECTED_CUSTOM_JSON = ("");
                    }
                    selectedProfile = CitiesConfig.profileFromClient;
                } else {
                    CitiesConfig.SELECTED_PROFILE = ("");
                    selectedProfile = "";
                }
            }
            if (!selectedProfile.isEmpty()) {
                dimensionProfileCache.put(Level.OVERWORLD, selectedProfile);
                String json = CitiesConfig.SELECTED_CUSTOM_JSON;
                if (json != null && !json.isEmpty()) {
                    LostCityProfile profile = new LostCityProfile("customized", json);
                    if (!ProfileSetup.STANDARD_PROFILES.containsKey("customized")) {
                        ProfileSetup.STANDARD_PROFILES.put(
                                "customized", new LostCityProfile("customized", false));
                    }
                    ProfileSetup.STANDARD_PROFILES.get("customized").copyFrom(profile);
                }
            }

            String profile = getProfileForDimension(Level.OVERWORLD);
            if (profile != null && !profile.isEmpty()) {
                if (ProfileSetup.STANDARD_PROFILES.get(profile).GENERATE_NETHER) {
                    dimensionProfileCache.put(Level.NETHER, "cavern");
                }
            }
        }
        return dimensionProfileCache.get(type);
    }

    public static boolean isAvoidedStructure(ResourceLocation id) {
        cacheAvoidedStructures();
        return AVOID_STRUCTURES_SET.contains(id);
    }

    public static boolean hasAvoidedStructures() {
        cacheAvoidedStructures();
        return !AVOID_STRUCTURES_SET.isEmpty();
    }

    private static void cacheAvoidedStructures() {
        if (AVOID_STRUCTURES_SET == null) {
            AVOID_STRUCTURES_SET = new HashSet<>();
            for (String s : AVOID_STRUCTURES) {
                AVOID_STRUCTURES_SET.add(ResourceLocation.parse(s));
            }
        }
    }

    // static {
    //     COMMON_BUILDER.comment("General settings").push(CATEGORY_PROFILES);
    //     CLIENT_BUILDER.comment("General settings").push(CATEGORY_PROFILES);
    //     SERVER_BUILDER.comment("General settings").push(CATEGORY_PROFILES);

    //     DIMENSION_PROFILES = COMMON_BUILDER
    //             .comment("A list of dimensions with associated city generation profiles (format
    // <dimensionid>=<profilename>")
    //             .defineList("dimensionsWithProfiles",
    // Lists.newArrayList(CitiesConfig.DEFAULT_DIMENSION_PROFILES), s -> s instanceof String);

    //     OPTIMIZED_HEIGHTMAP = COMMON_BUILDER
    //             .comment("If true then a different heightmap generation algorithm is used which
    // should be slightly more efficient. Be careful with this as it might not be 100% compatible
    // with some other terrain generation mods!")
    //             .define("optimizedHeightmap", false);
    //     HEIGHT_SAMPLE_SIZE = COMMON_BUILDER
    //             .comment("The size of the chunk grid used for heightmap sampling. Default is 1
    // which means every chunk is sampled. Higher values will sample less chunks and thus be faster
    // but also less accurate")
    //             .defineInRange("heightSampleSize", 3, 1, 100);

    //     SPECIAL_BED_BLOCK = SERVER_BUILDER
    //             .comment("Block to put underneath a bed so that it qualifies as a teleporter
    // bed")
    //             .define("specialBedBlock", "minecraft:diamond_block");

    //     SELECTED_PROFILE = SERVER_BUILDER.define("selectedProfile", "<CHECK>"); // Default is
    // dummy value that tells the system to check in profileFromClient
    //     SELECTED_CUSTOM_JSON = SERVER_BUILDER.define("selectedCustomJson", "");
    //     TODO_QUEUE_SIZE = SERVER_BUILDER.comment("The size of the todo queues for the lost city
    // generator").defineInRange("todoQueueSize", 20, 1, 100000);
    //     FORCE_SAPLING_GROWTH = SERVER_BUILDER.comment("If this is true then saplings will grow
    // into trees during generation. This is more expensive").define("forceSaplingGrowth", true);
    //     CACHE_CLEANUP_SECONDS = SERVER_BUILDER.comment("Time in seconds after which cached chunk
    // data is evicted").defineInRange("cacheCleanupSeconds", 300, 1, 86400);
    //     AVOID_STRUCTURES = SERVER_BUILDER
    //             .comment("List of structures to avoid when generating cities (for example to
    // avoid generating a city in a woodland mansion)")
    //             .defineList("avoidStructures", Lists.newArrayList(DEF_AVOID_STRUCTURES), s -> s
    // instanceof String);
    //     AVOID_STRUCTURES_ADJACENT = SERVER_BUILDER
    //             .comment("If true then also avoid generating the structures mentioned in
    // 'avoidStructures' in chunks adjacent to the chunk with the structure")
    //             .define("avoidStructuresAdjacent", false);
    //     AVOID_VILLAGES_ADJACENT = SERVER_BUILDER
    //             .comment("If true then also avoid generating cities in chunks adjacent to the
    // chunks with villages")
    //             .define("avoidVillagesAdjacent", false);
    //     AVOID_VILLAGES = SERVER_BUILDER
    //             .comment("If true then avoid generating cities in chunks with villages")
    //             .define("avoidVillages", true);
    //     AVOID_FLATTENING = SERVER_BUILDER
    //             .comment("If true then avoid flattening the terrain around the city in case there
    // was a structure that was avoided")
    //             .define("avoidFlattening", true);

    //     SERVER_BUILDER.pop();
    //     COMMON_BUILDER.pop();
    //     CLIENT_BUILDER.pop();

    //     COMMON_CONFIG = COMMON_BUILDER.build();
    //     CLIENT_CONFIG = CLIENT_BUILDER.build();
    //     SERVER_CONFIG = SERVER_BUILDER.build();
    // }

}
