package mcjty.lostcities.gui;

import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.ChunkHeightmap;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.LostCityTerrainFeature;
import mcjty.lostcities.worldgen.lost.cityassets.WorldStyle;
import mcjty.lostcities.worldgen.lost.regassets.WorldStyleRE;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.util.math.random.CheckedRandom;

import java.util.Collections;
import java.util.Optional;
import java.util.Random;

public class NullDimensionInfo implements IDimensionInfo {

    public static final int PREVIEW_WIDTH = 62;
    public static final int PREVIEW_HEIGHT = 58;

    private final String[] biomeMap = new String[] {
            "ddddddddddddddddddddddppppppppppppppp==ppppppppppppppppppppppp",
            "ddddddddddddddddddddpppppppppppppppp==pppppppppppppppppppppppp",
            "ddddddddddddddddddddpppppppppppppp===ppppppppppppppppppppppppp",
            "pddddddddddddddddpppppppppppppppppp==ppppppppppppppppppppppppp",
            "pppdddddddppppppppppppppppppppppppp==ppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppp==pppppppppp----------pppp",
            "ppppppppppppppppppppppppppppppppppppp==ppppppp--------------pp",
            "pppppppppppppppppppppppppppppppppppppp==ppppp-----------------",
            "pppppppppppppppppppppppppppppppppppppp===pppp-----------------",
            "ppppppppppppppppppppppppppppppppppppppp===ppppp---------------",
            "pppppppppppppppppppppppppppppppppppppppp==--pp----------------",
            "pppppppppppppppppppppppppppppppppppppppp*---------------------",
            "pppppppppppppppppppppppppppppppppppppp****--------------------",
            "ppppppppppppppppppppppppppppppppppppp***----------------------",
            "pppppppppppppppppppppppppppppppppppp**------------------------",
            "ppppppppppppppppppppppppppppppppppppp**-----------------------",
            "ppppppppppppppppppppppppppppppppppppppp*----------------------",
            "pppppppppppppppppppppppppppppppppppppp**----------------------",
            "ppppp###pppppppppppppppppppppppppppppp**----------------------",
            "ppppp####ppppppp#####pppppppppppppppppp*----------------------",
            "pppppp#####pp##+++#####ppppppppppppp*****---------------------",
            "pppppppp#####++++####pppppppppppppp**------pp----p------------",
            "ppppppppp##++++++###pppppppppppppppp***---pppp--ppp-----------",
            "ppppppppp###+++++++#####ppppppppppppp---pppppppppppp---------p",
            "pppppppp##p##+++++++###ppppppppppppppppppppppppppppp---------p",
            "pppppppppp#####++++####ppppppppppppppppppppppppppppppppp----pp",
            "pppppppppppp###+++++###ppppppppppppppppppppppppppppppppppppppp",
            "ppppppppppppp####++++####ppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppp####++######pppppppppppppppppppppppppppppppppppp",
            "ppppppppppppppp#+++####ppppppppppppppppppppppppppppppppppppppp",
            "ppppppppppppp####pp#####pppppppppppppppppppppppppppppppppppppp",
            "pppppppppp#####ppppppppppppppppppppppppppppppppppppppppppppppp",
            "ppppppppppp###pppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp"
    };

    private final LostCityProfile profile;
    private final WorldStyle style;
    private final Random random;
    private final long seed;

    private final Registry<Biome> biomeRegistry;
    private final LostCityTerrainFeature feature;

    public NullDimensionInfo(LostCityProfile profile, long seed) {
        this.profile = profile;
        style = new WorldStyle(new WorldStyleRE(
                "standard",
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Collections.emptyList(),
                Optional.empty()
        ));
        this.seed = seed;
        random = new Random(seed);
        CheckedRandom Random = new CheckedRandom(seed);
        feature = new LostCityTerrainFeature(this, profile, Random);
        feature.setupStates(profile);
        // @todo 1.19.3
//        biomeRegistry = ServerLifecycleHooks.getCurrentServer().getRegistryManager().get(RegistryKeys.BIOME);
//        biomeRegistry = RegistryAccess.builtinCopy().registry(Registry.BIOME_REGISTRY).get();
        biomeRegistry = null;
    }

    @Override
    public void setWorld(StructureWorldAccess world) {
    }

    @Override
    public long getSeed() {
        return seed;
    }

    @Override
    public StructureWorldAccess getWorld() {
        return null;
    }

    @Override
    public RegistryKey<World> getType() {
        return World.OVERWORLD;
    }

    @Override
    public LostCityProfile getProfile() {
        return profile;
    }

    @Override
    public LostCityProfile getOutsideProfile() {
        return  profile;
    }

    @Override
    public WorldStyle getWorldStyle() {
        return style;
    }

    @Override
    public Random getRandom() {
        return random;
    }

    @Override
    public LostCityTerrainFeature getFeature() {
        return feature;
    }

    @Override
    public ChunkHeightmap getHeightmap(ChunkCoord coord) {
        int chunkX = coord.chunkX();
        int chunkZ = coord.chunkZ();
        ChunkHeightmap heightmap = new ChunkHeightmap(profile.LANDSCAPE_TYPE, profile.GROUNDLEVEL);
        char b = getBiomeChar(chunkX, chunkZ);
        int y = switch (b) {
            case 'p' -> 65;
            case '-' -> 60;
            case '=' -> 65;
            case '#' -> 95;
            case '+' -> 125;
            case '*' -> 65;
            case 'd' -> 65;
            default -> 65;
        };
        heightmap.update(y);
        return heightmap;
    }

    @Override
    public ChunkHeightmap getHeightmap(int chunkX, int chunkZ) {
        ChunkCoord coord = new ChunkCoord(getType(), chunkX, chunkZ);
        return getHeightmap(coord);
    }

    public char getBiomeChar(int chunkX, int chunkZ) {
        if (chunkX >= 0 && chunkX < PREVIEW_WIDTH && chunkZ >= 0 && chunkZ < PREVIEW_HEIGHT) {
            return biomeMap[chunkZ].charAt(chunkX);
        } else {
            return 'p';
        }
    }

//    @Override
//    public Biome[] getBiomes(int chunkX, int chunkZ) {
//        Biome[] biomes = new Biome[10*10];
//        Biome biome = Biomes.PLAINS;
//        char b = getBiomeChar(chunkX, chunkZ);
//        switch (b) {
//            case 'p': biome = Biomes.PLAINS; break;
//            case '-': biome = Biomes.OCEAN; break;
//            case '=': biome = Biomes.RIVER; break;
//            case '#': biome = Biomes.MOUNTAIN_EDGE; break;
//            case '+': biome = Biomes.MOUNTAINS; break;
//            case '*': biome = Biomes.BEACH; break;
//            case 'd': biome = Biomes.DESERT; break;
//        }
//        for (int i = 0 ; i < biomes.length ; i++) {
//            biomes[i] = biome;
//        }
//        return biomes;
//    }

    @Override
    public RegistryEntry<Biome> getBiome(BlockPos pos) {
        ChunkPos cp = new ChunkPos(pos);
        char b = getBiomeChar(cp.x, cp.z);
        RegistryKey<Biome> biome = switch (b) {
            case 'p' -> BiomeKeys.PLAINS;
            case '-' -> BiomeKeys.OCEAN;
            case '=' -> BiomeKeys.RIVER;
            case '#' -> BiomeKeys.STONY_PEAKS;
            // @todo 1.18
            case '+' -> BiomeKeys.JAGGED_PEAKS;
            // @todo 1.18
            case '*' -> BiomeKeys.BEACH;
            case 'd' -> BiomeKeys.DESERT;
            default -> BiomeKeys.PLAINS;
        };
        return biomeRegistry.getEntry(biome).get();
    }

    @Override
    public RegistryKey<World> dimension() {
        return null;
    }
}
