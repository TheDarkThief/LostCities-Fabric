package mcjty.lostcities.worldgen;

import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.lost.cityassets.WorldStyle;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;

import org.jetbrains.annotations.Nullable;
import java.util.Random;

public interface IDimensionInfo {
    void setWorld(StructureWorldAccess world);

    long getSeed();

    StructureWorldAccess getWorld();

    RegistryKey<World> getType();

    LostCityProfile getProfile();

    LostCityProfile getOutsideProfile();

    WorldStyle getWorldStyle();

    Random getRandom();

    LostCityTerrainFeature getFeature();

    ChunkHeightmap getHeightmap(int chunkX, int chunkZ);

    ChunkHeightmap getHeightmap(ChunkCoord coord);

//    Biome[] getBiomes(int chunkX, int chunkZ);

    RegistryEntry<Biome> getBiome(BlockPos pos);

    @Nullable
    RegistryKey<World> dimension();
}
