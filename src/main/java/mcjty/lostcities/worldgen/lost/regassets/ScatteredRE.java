package mcjty.lostcities.worldgen.lost.regassets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostcities.worldgen.lost.cityassets.Scattered;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class ScatteredRE implements IAsset<ScatteredRE> {

    public static final Codec<ScatteredRE> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(Codec.STRING).optionalFieldOf("buildings").forGetter(l -> Optional.ofNullable(l.buildings)),
                    Codec.STRING.optionalFieldOf("multibuilding").forGetter(l -> Optional.ofNullable(l.multibuilding)),
                    StringIdentifiable.fromEnum(Scattered.TerrainHeight::values).fieldOf("terrainheight").forGetter(l -> l.terrainheight),
                    StringIdentifiable.fromEnum(Scattered.TerrainFix::values).fieldOf("terrainfix").forGetter(l -> l.terrainfix),
                    Codec.INT.optionalFieldOf("heightoffset", 0).forGetter(l -> l.heightoffset)
            ).apply(instance, ScatteredRE::new));

    private Identifier name;
    private final Scattered.TerrainHeight terrainheight;
    private final Scattered.TerrainFix terrainfix;
    private final int heightoffset;
    private final List<String> buildings;
    private final String multibuilding;

    public ScatteredRE(Optional<List<String>> buildings, Optional<String> multibuilding, Scattered.TerrainHeight terrainheight, Scattered.TerrainFix terrainfix,
                       int heightoffset) {
        this.buildings = buildings.orElse(null);
        this.multibuilding = multibuilding.orElse(null);
        this.terrainheight = terrainheight;
        this.terrainfix = terrainfix;
        this.heightoffset = heightoffset;
    }

    @Nullable
    public List<String> getBuildings() {
        return buildings;
    }

    @Nullable
    public String getMultibuilding() {
        return multibuilding;
    }

    public Scattered.TerrainHeight getTerrainheight() {
        return terrainheight;
    }

    public Scattered.TerrainFix getTerrainfix() {
        return terrainfix;
    }

    public int getHeightoffset() {
        return heightoffset;
    }

    @Override
    public ScatteredRE setRegistryName(Identifier name) {
        this.name = name;
        return this;
    }

    @Nullable
    public Identifier getRegistryName() {
        return name;
    }
}
