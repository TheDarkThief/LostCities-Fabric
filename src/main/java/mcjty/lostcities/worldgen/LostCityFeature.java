package mcjty.lostcities.worldgen;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.config.ProfileSetup;
import mcjty.lostcities.setup.Config;
import mcjty.lostcities.setup.FabricEventHandlers;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.ChunkRegion;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;

import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;

public class LostCityFeature extends Feature<DefaultFeatureConfig> {

    /**
     * On dedicated servers the dimensionInfo cache is no problem. The server starts only once
     * and will have the correct dimension info and for the clients it doesn't matter.
     * However, to make sure that on a single player world this cache is cleared when the player
     * exits the world and creates a new one we keep a static flag which is incremented whenever
     * the player exits the world. That is then used to help clear this cache
     */
    private final Map<RegistryKey<World>, IDimensionInfo> dimensionInfo = new HashMap<>();
    public static int globalDimensionInfoDirtyCounter = 0;
    private int dimensionInfoDirtyCounter = -1;

    public LostCityFeature() {
        super(DefaultFeatureConfig.CODEC);
    }

    private static final long[] times = new long[1000];
    private static long totalCnt = 0;

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess level = context.getWorld();
        if (level instanceof ChunkRegion) {
            IDimensionInfo diminfo = getDimensionInfo(level);
            if (diminfo != null) {
                ChunkRegion region = (ChunkRegion) level;
                ChunkPos center = region.getCenterPos();
                RegistryEntry<Biome> biome = region.getBiome(center.getCenterAtY(60));
                if (biome.isIn(BiomeTags.IS_END)) {
                    return false;
                }

                int chunkX = center.x;
                int chunkZ = center.z;
                diminfo.setWorld(level);
                try {
                    diminfo.getFeature().generate(region, region.getChunk(chunkX, chunkZ));
                } catch (Exception e) {
                    LostCities.getLogger().error("Error generating chunk {},{}: {}", chunkX, chunkZ, e.getMessage(), e);
                    e.printStackTrace();
                    ErrorLogger.logChunkInfo(chunkX, chunkZ, diminfo);
                    ErrorLogger.report("There was an error generating a chunk. See log for details!");
                }
                return true;
            }
        }
        return false;
    }

    @Nullable
    public IDimensionInfo getDimensionInfo(StructureWorldAccess world) {
        if (globalDimensionInfoDirtyCounter != dimensionInfoDirtyCounter) {
            // Force clear of cache
            cleanUp();
        }
        RegistryKey<World> type = world.toServerWorld().getRegistryKey();
        String profileName = Config.getProfileForDimension(type);
        if (profileName != null) {
            if (!dimensionInfo.containsKey(type)) {
                LostCityProfile profile = ProfileSetup.STANDARD_PROFILES.get(profileName);
                if (profile == null) {
                    return null;
                }
                LostCityProfile outsideProfile = profile.CITYSPHERE_OUTSIDE_PROFILE == null ? null : ProfileSetup.STANDARD_PROFILES.get(profile.CITYSPHERE_OUTSIDE_PROFILE);
                IDimensionInfo diminfo = new DefaultDimensionInfo(world, profile, outsideProfile);
                dimensionInfo.put(type, diminfo);
            }
            return dimensionInfo.get(type);
        }
        return null;
    }

    public void cleanUp() {
        LostCities.lostCitiesImp.cleanUp();
        FabricEventHandlers.cleanUp();
        AssetRegistryKeys.reset();
        dimensionInfo.clear();
        dimensionInfoDirtyCounter = globalDimensionInfoDirtyCounter;
    }
}
