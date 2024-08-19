package mcjty.lostcities.worldgen;

import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistryKeys;
import mcjty.lostcities.worldgen.lost.cityassets.WorldStyle;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.world.World;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.chunk.ChunkManager;
import org.jetbrains.annotations.Nullable;

//import java.util.Random;

public class DefaultDimensionInfo implements IDimensionInfo {

    private StructureWorldAccess world;
    private final LostCityProfile profile;
    private final LostCityProfile profileOutside;
    private final WorldStyle style;

    private final java.util.Random random;

    private final Registry<Biome> biomeRegistry;
    private final LostCityTerrainFeature feature;

    public DefaultDimensionInfo(StructureWorldAccess world, LostCityProfile profile, LostCityProfile profileOutside) {
        this.world = world;
        this.profile = profile;
        this.profileOutside = profileOutside;
        style = AssetRegistryKeys.WORLDSTYLES.get(world, profile.getWorldStyle());
        random = new java.util.Random(world.getSeed());
        Random Random = new CheckedRandom(world.getSeed());
        feature = new LostCityTerrainFeature(this, profile, Random);
        feature.setupStates(profile);
        biomeRegistry = world.getRegistryManager().get(RegistryKeys.BIOME);
    }

    @Override
    public void setWorld(StructureWorldAccess world) {
        this.world = world;
    }

    @Override
    public long getSeed() {
        return world.getSeed();
    }

    @Override
    public StructureWorldAccess getWorld() {
        return world;
    }

    @Override
    public RegistryKey<World> getType() {
        return world.toServerWorld().getRegistryKey();
    }

    @Override
    public LostCityProfile getProfile() {
        return profile;
    }

    @Override
    public LostCityProfile getOutsideProfile() {
        return profileOutside;
    }

    @Override
    public WorldStyle getWorldStyle() {
        return style;
    }

    @Override
    public java.util.Random getRandom() {
        return random;
    }

    @Override
    public LostCityTerrainFeature getFeature() {
        return feature;
    }

    @Override
    public ChunkHeightmap getHeightmap(int chunkX, int chunkZ) {
        ChunkCoord coord = new ChunkCoord(getType(), chunkX, chunkZ);
        return feature.getHeightmap(coord, getWorld());
    }

    @Override
    public ChunkHeightmap getHeightmap(ChunkCoord coord) {
        return feature.getHeightmap(coord, getWorld());
    }

    //    @Override
//    public Biome[] getBiomes(int chunkX, int chunkZ) {
//        AbstractChunkProvider chunkProvider = getWorld().getChunkProvider();
//        if (chunkProvider instanceof ServerChunkProvider) {
//            BiomeProvider biomeProvider = ((ServerChunkProvider) chunkProvider).getChunkGenerator().getBiomeProvider();
//            return biomeProvider.getBiomes((chunkX - 1) * 4 - 2, chunkZ * 4 - 2, 10, 10, false);
//        }
//    }
//
    @Override
    public RegistryEntry<Biome> getBiome(BlockPos pos) {
        ChunkManager chunkProvider = getWorld().getChunkManager();
        if (chunkProvider instanceof ServerChunkManager) {
            ChunkGenerator generator = ((ServerChunkManager) chunkProvider).getChunkGenerator();
            BiomeSource biomeProvider = generator.getBiomeSource();
            MultiNoiseUtil.MultiNoiseSampler sampler = ((ServerChunkManager) chunkProvider).getNoiseConfig().getMultiNoiseSampler();
            return biomeProvider.getBiome(pos.getX() >> 2, pos.getY() >> 2, pos.getZ() >> 2, sampler);
        }
        return biomeRegistry.getEntry(BiomeKeys.PLAINS).orElseThrow();
    }

    @Nullable
    @Override
    public RegistryKey<World> dimension() {
        return world.toServerWorld().getRegistryKey();
    }
}
