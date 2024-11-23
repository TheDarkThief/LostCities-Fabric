package mcjty.lostcities.worldgen.lost.cityassets;

import mcjty.lostcities.api.ILostCityAsset;
import mcjty.lostcities.worldgen.lost.regassets.ScatteredRE;
import mcjty.lostcities.worldgen.lost.regassets.data.DataTools;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

import org.jetbrains.annotations.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ScatteredBuilding implements ILostCityAsset {

    private final Identifier name;
    private final List<String> buildings;
    private final String multibuilding;
    private final ScatteredBuilding.TerrainHeight terrainheight;
    private final ScatteredBuilding.TerrainFix terrainfix;
    private final int heightoffset;

    public ScatteredBuilding(ScatteredRE object) {
        name = object.getRegistryName();
        this.buildings = object.getBuildings();
        this.multibuilding = object.getMultibuilding();
        this.terrainheight = object.getTerrainheight();
        this.terrainfix = object.getTerrainfix();
        this.heightoffset = object.getHeightoffset();
    }

    @Nullable
    public List<String> getBuildings() {
        return buildings;
    }

    @Nullable
    public String getMultibuilding() {
        return multibuilding;
    }

    public TerrainHeight getTerrainheight() {
        return terrainheight;
    }

    public TerrainFix getTerrainfix() {
        return terrainfix;
    }

    public int getHeightoffset() {
        return heightoffset;
    }

    @Override
    public String getName() {
        return DataTools.toName(name);
    }

    @Override
    public Identifier getId() {
        return name;
    }

    public enum TerrainHeight implements StringIdentifiable {
        LOWEST("lowest"),
        AVERAGE("average"),
        HIGHEST("highest"),
        OCEAN("ocean")
        ;

        private static final Map<String, TerrainHeight> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(TerrainHeight::asString, (v) -> v));

        TerrainHeight(String name) {
            this.name = name;
        }

        private final String name;

        public static final TerrainHeight byName(String name) {
            return BY_NAME.get(name);
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public String asString() {
            return name;
        }
    }

    public enum TerrainFix implements StringIdentifiable {
        NONE("none"),               // Do nothing with the terrain
        CLEAR("clear"),             // Clear from generation point upwards
        REPEATSLICE("repeatslice")  // Repeat the bottom slice downwards until it hits a solid block
        ;

        private static final Map<String, TerrainFix> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(TerrainFix::asString, (v) -> v));

        TerrainFix(String name) {
            this.name = name;
        }

        private final String name;

        public static final TerrainFix byName(String name) {
            return BY_NAME.get(name);
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public String asString() {
            return name;
        }
    }
}
